package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.persistence.AbstractModel;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<PostModel> findById(BigInteger id) {
        final var query = """
                SELECT p.id AS post_id,
                       p.title,
                       p.content,
                       t.name AS tag_name,
                       (SELECT COALESCE(l.lcount, 0) FROM likes l WHERE l.post_id = p.id) AS like_count,
                       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count
                FROM posts p
                LEFT JOIN post_tags pt ON p.id = pt.post_id
                LEFT JOIN tags t ON pt.tag_id = t.id
                WHERE post_id = ?
                """;

        try {
            final var postModel = jdbcTemplate.queryForObject(query, (rs, row) -> mapToPostModelWithTags(rs), id);

            return Optional.of(postModel);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PostModel> findAll() {
        final var query = """
                SELECT p.id AS post_id,
                       p.title,
                       p.content,
                       p.updated_at,
                       t.name AS tag_name,
                       (SELECT COALESCE(l.lcount, 0) FROM likes l WHERE l.post_id = p.id) AS like_count,
                       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count
                FROM posts p
                LEFT JOIN post_tags pt ON p.id = pt.post_id
                LEFT JOIN tags t ON pt.tag_id = t.id
                ORDER BY p.updated_at DESC
                """;

        return jdbcTemplate.query(query, (rs, rowNum) -> mapToPostModelWithTags(rs));
    }

    @Override
    public PostModel save(PostModel entity) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO posts(title, content) VALUES (?, ?) RETURNING *",
                (rs, rowNum) -> mapToPostModel(rs),
                entity.getTitle(),
                entity.getContent()
        );
    }

    @Override
    public int deleteById(BigInteger id) {
        return jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public PostModel update(BigInteger id, PostModel updatedEntity) {
        final var query = """
                UPDATE posts
                SET title = ?, content = ?
                WHERE id = ?
                RETURNING *
                """;

        return jdbcTemplate.queryForObject(
                query, (rs, rowNum) -> mapToPostModel(rs),
                updatedEntity.getTitle(),
                updatedEntity.getContent(),
                id
        );
    }

    @Override
    public boolean existsById(BigInteger id) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM posts WHERE id = ?)",
                Boolean.class, id
        );
    }

    @Override
    public List<BigInteger> getOrInsertTags(List<String> tagNames) {
        final var query = """
                WITH input_tags AS (
                    SELECT unnest(?::text[]) AS name
                ),
                inserted_tags AS (
                    INSERT INTO tags (name)
                    SELECT name FROM input_tags
                    ON CONFLICT (name) DO NOTHING
                    RETURNING id, name
                )
                SELECT id FROM inserted_tags
                UNION ALL
                SELECT t.id FROM tags t
                JOIN input_tags it ON t.name = it.name
                WHERE NOT EXISTS (SELECT 1 FROM inserted_tags WHERE name = it.name)
                """;

        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> rs.getBigDecimal(AbstractModel.Fields.id).toBigInteger(),
                tagNames.toArray()
        );
    }

    @Override
    public void saveTags(final BigInteger postId, final List<BigInteger> tagIds) {
        final var query = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(query, getPostTagsBatchPreparedStatementSetter(postId, tagIds));
    }

    @Override
    public List<PostModel> findPaged(int page, int size, String query) {
        // TODO: implement filtering by query
        return jdbcTemplate.query(
                "SELECT * FROM posts ORDER BY updated_at LIMIT ? OFFSET ?",
                (rs, row) -> mapToPostModelWithTransientFields(rs),
                size, page * size
        );
    }

    @Override
    public void removePostTags(BigInteger postId) {
        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", postId);
    }

    @Override
    public BigInteger incrementLikes(BigInteger postId) {
        return jdbcTemplate.queryForObject(
                "UPDATE posts SET lcount = lcount + 1 WHERE id = ? RETURNING lcount",
                BigInteger.class,
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
            BigInteger postId,
            List<BigInteger> tagIds
    ) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setBigDecimal(1, BigDecimal.valueOf(postId.doubleValue()));
                ps.setBigDecimal(2, BigDecimal.valueOf(tagIds.get(i).doubleValue()));
            }

            @Override
            public int getBatchSize() {
                return tagIds.size();
            }
        };

    }

    protected static PostModel mapToPostModel(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getBigDecimal(AbstractModel.Fields.id).toBigInteger())
                .title(resultSet.getString(PostModel.Fields.title))
                .content(resultSet.getString(PostModel.Fields.content))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }

    protected static PostModel mapToPostModelWithTransientFields(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getBigDecimal("post_id").toBigInteger())
                .title(resultSet.getString("p.title"))
                .content(resultSet.getString("p.content"))
                .tags(new ArrayList<>())
                .likeCount(resultSet.getLong("like_count"))
                .commentCount(resultSet.getLong("comment_count"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
