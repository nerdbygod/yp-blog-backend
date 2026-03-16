package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.persistence.AbstractModel;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.Page;
import org.practicum.yandex.repository.PostRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<PostModel> findById(Long id) {
        final var query = """
                SELECT p.id AS post_id,
                       p.title,
                       p.content,
                       t.name AS tag_name,
                       p.created_at,
                       p.updated_at,
                       COALESCE((SELECT l.lcount FROM blog.likes l WHERE l.post_id = p.id), 0) AS like_count,
                       (SELECT COUNT(*) FROM blog.comments c WHERE c.post_id = p.id) AS comment_count
                FROM blog.posts p
                LEFT JOIN blog.post_tags pt ON p.id = pt.post_id
                LEFT JOIN blog.tags t ON pt.tag_id = t.id
                WHERE p.id = ?
                """;

        try {
            final var postModel = jdbcTemplate.query(query, PostRepositoryImpl::mapToPostModelWithTags, id);

            return Optional.ofNullable(postModel);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public PostModel save(PostModel entity) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO blog.posts(title, content) VALUES (?, ?) RETURNING *",
                (rs, rowNum) -> mapToPostModel(rs),
                entity.getTitle(),
                entity.getContent()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM blog.posts WHERE id = ?", id);
    }

    @Override
    public PostModel update(Long id, PostModel updatedEntity) {
        final var query = """
                UPDATE blog.posts
                SET title = ?, content = ?, updated_at = ?
                WHERE id = ?
                RETURNING *
                """;

        return jdbcTemplate.queryForObject(
                query, (rs, rowNum) -> mapToPostModel(rs),
                updatedEntity.getTitle(),
                updatedEntity.getContent(),
                LocalDateTime.now(),
                id
        );
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM blog.posts WHERE id = ?)",
                Boolean.class, id
        );
    }

    @Override
    public List<Long> getOrInsertTags(List<String> tagNames) {
        final var params = new MapSqlParameterSource();
        params.addValue("tagNames", tagNames.toArray(new String[0]));

        final var query = """
                WITH input_tags AS (
                    SELECT unnest(:tagNames::varchar[]) AS name
                ),
                inserted_tags AS (
                    INSERT INTO blog.tags (name)
                    SELECT name FROM input_tags
                    ON CONFLICT (name) DO NOTHING
                    RETURNING id, name
                )
                SELECT id FROM inserted_tags
                UNION ALL
                SELECT t.id FROM blog.tags t
                JOIN input_tags it ON t.name = it.name
                WHERE NOT EXISTS (SELECT 1 FROM inserted_tags WHERE name = it.name)
                """;

        return namedParameterJdbcTemplate.query(
                query, params,
                (rs, rowNum) -> rs.getLong(AbstractModel.Fields.id)
        );
    }

    @Override
    public void saveTags(final Long postId, final List<Long> tagIds) {
        final var query = "INSERT INTO blog.post_tags (post_id, tag_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(query, getPostTagsBatchPreparedStatementSetter(postId, tagIds));
    }

    @Override
    public Page<PostModel> findPaged(long page, long size, String query, Set<String> tags) {
        final var params = new MapSqlParameterSource();

        params.addValue("limit", size);
        params.addValue("offset", (page - 1) * size);

        final var whereClause = new StringBuilder("WHERE 1=1 ");

        if (StringUtils.isNotBlank(query)) {
            whereClause.append("AND p.title ILIKE :searchQuery ");
            params.addValue("searchQuery", "%" + query + "%");
        }

        String havingClause = "";
        if (CollectionUtils.isNotEmpty(tags)) {
            whereClause.append("AND t.name IN (:tags) ");
            params.addValue("tags", tags);
            params.addValue("tagCount", tags.size());
            havingClause = "HAVING COUNT(DISTINCT t.name) = :tagCount";
        }

        final var sql = String.format("""
                        WITH filtered_posts AS (
                            SELECT p.id, p.title, p.content, p.updated_at, p.created_at,
                            COUNT(*) OVER() AS total_matches
                            FROM blog.posts p
                            JOIN blog.post_tags pt ON p.id = pt.post_id
                            JOIN blog.tags t ON pt.tag_id = t.id
                            %s
                            GROUP by p.id
                            %s
                            ORDER BY p.updated_at DESC
                            LIMIT :limit OFFSET :offset
                        )
                        SELECT fp.id AS post_id,
                               fp.title as title,
                               CASE
                                   WHEN LENGTH(fp.content) > 128 THEN LEFT(fp.content, 128) || '...'
                                   ELSE fp.content
                               END AS preview,
                               fp.total_matches,
                               fp.updated_at,
                               fp.created_at,
                               t.name as tag_name,
                               (SELECT COALESCE(l.lcount, 0) FROM blog.likes l WHERE l.post_id = fp.id) AS like_count,
                               (SELECT COUNT(*) FROM blog.comments c WHERE c.post_id = fp.id) AS comment_count
                        FROM filtered_posts fp
                        LEFT JOIN blog.post_tags pt ON fp.id = pt.post_id
                        LEFT JOIN blog.tags t ON pt.tag_id = t.id
                        ORDER BY fp.updated_at DESC;
                        """,
                whereClause, havingClause
        );

        return namedParameterJdbcTemplate.query(
                sql, params, rs -> {
                    final var results = new LinkedHashMap<Long, PostModel>();
                    long totalMatches = 0L;

                    while (rs.next()) {
                        if (results.isEmpty()) {
                            totalMatches = rs.getLong("total_matches");
                        }

                        final var postId = rs.getLong("post_id");
                        final var postModel = results.computeIfAbsent(postId, id -> {
                            try {
                                return mapToPreviewPostModel(rs);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        final var tagName = rs.getString("tag_name");
                        if (Objects.nonNull(tagName)) {
                            postModel.getTags().add(tagName);
                        }
                    }

                    return Page.<PostModel>builder()
                            .data(new ArrayList<>(results.values()))
                            .count(totalMatches)
                            .build();
                });
    }

    @Override
    public void removePostTags(Long postId) {
        jdbcTemplate.update("DELETE FROM blog.post_tags WHERE post_id = ?", postId);
    }

    @Override
    public Long incrementLikes(Long postId) {
        final var query = """
                INSERT INTO blog.likes (post_id, lcount)
                VALUES (?, 1)
                ON CONFLICT (post_id)
                DO UPDATE SET lcount = blog.likes.lcount + 1
                RETURNING lcount
                """;

        return jdbcTemplate.queryForObject(
                query,
                Long.class,
                postId
        );
    }

    protected static PostModel mapToPostModelWithTags(final ResultSet rs) throws SQLException {
        PostModel postModel = null;

        while (rs.next()) {
            if (postModel == null) {
                postModel = mapToPostModelWithTransientFields(rs);
            }

            final var tagName = rs.getString("tag_name");
            if (tagName != null) {
                postModel.getTags().add(tagName);
            }
        }

        return postModel;
    }

    private static BatchPreparedStatementSetter getPostTagsBatchPreparedStatementSetter(
            Long postId, List<Long> tagIds) {

        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, postId);
                ps.setLong(2, tagIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return tagIds.size();
            }
        };

    }

    protected static PostModel mapToPostModel(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getLong(AbstractModel.Fields.id))
                .title(resultSet.getString(PostModel.Fields.title))
                .content(resultSet.getString(PostModel.Fields.content))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }

    protected static PostModel mapToPostModelWithTransientFields(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getLong("post_id"))
                .title(resultSet.getString(PostModel.Fields.title))
                .content(resultSet.getString(PostModel.Fields.content))
                .tags(new ArrayList<>())
                .likeCount(resultSet.getLong("like_count"))
                .commentCount(resultSet.getLong("comment_count"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }

    protected static PostModel mapToPreviewPostModel(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getLong("post_id"))
                .title(resultSet.getString(PostModel.Fields.title))
                .content(resultSet.getString("preview"))
                .tags(new ArrayList<>())
                .likeCount(resultSet.getLong("like_count"))
                .commentCount(resultSet.getLong("comment_count"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
