package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.persistence.AbstractModel;
import org.practicum.yandex.persistence.comment.CommentModel;
import org.practicum.yandex.repository.CommentRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<CommentModel> findById(Long id) {
        try {
            final var commentModel = jdbcTemplate.queryForObject(
                    "SELECT * FROM blog.comments WHERE id = ?",
                    (rs, row) -> mapToCommentModel(rs), id
            );

            return Optional.of(commentModel);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CommentModel> findAllByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT * FROM blog.comments WHERE post_id = ?",
                (rs, row) -> mapToCommentModel(rs), postId
        );
    }

    @Override
    public CommentModel save(CommentModel entity) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO blog.comments (post_id, content) VALUES ?, ? RETURNING *",
                (rs, rowNum) -> mapToCommentModel(rs),
                entity.getPostId(), entity.getContent()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM blog.comments WHERE id = ?", id);
    }

    @Override
    public CommentModel update(Long id, CommentModel updatedEntity) {
        final var query = """
                UPDATE blog.comments
                SET content = ?, updated_at = ?
                WHERE id = ?
                RETURNING *
                """;

        return jdbcTemplate.queryForObject(
                query, (rs, rowNum) -> mapToCommentModel(rs),
                updatedEntity.getContent(),
                LocalDateTime.now(),
                id
        );
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM blog.comments WHERE id = ?)",
                Boolean.class, id
        );
    }

    protected static CommentModel mapToCommentModel(final ResultSet resultSet) throws SQLException {
        return CommentModel.builder()
                .id(resultSet.getLong(AbstractModel.Fields.id))
                .content(resultSet.getString(CommentModel.Fields.content))
                .postId(resultSet.getLong(CommentModel.Fields.postId))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
