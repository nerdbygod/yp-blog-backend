package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PostModel findById(BigInteger id) {
        return jdbcTemplate.query(
                        "SELECT FROM posts WHERE id = ?",
                        (rs, row) -> mapResultSet(rs))
                .getFirst();
    }

    @Override
    public List<PostModel> findAll() {
        return jdbcTemplate.query("SELECT * FROM posts ORDER BY updated_at",
                (rs, rowNum) -> mapResultSet(rs)
        );
    }

    @Override
    public PostModel save(PostModel postModel) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO posts(title, content) VALUES (?, ?) RETURNING *",
                (rs, rowNum) -> mapResultSet(rs),
                postModel.getTitle(),
                postModel.getContent()
        );
    }

    @Override
    public int deleteById(BigInteger id) {
        return jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public List<PostModel> findPaged(int page, int size, String query) {
        // TODO: implement filtering by query
        return jdbcTemplate.query(
                "SELECT * FROM posts ORDER BY updated_at LIMIT ? OFFSET ?",
                (rs, row) -> mapResultSet(rs),
                size, page * size
        );
    }

    protected PostModel mapResultSet(final ResultSet resultSet) throws SQLException {
        return PostModel.builder()
                .id(resultSet.getBigDecimal("id").toBigInteger())
                .title(resultSet.getString("title"))
                .content(resultSet.getString("content"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
