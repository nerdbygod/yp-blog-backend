package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PostModel findById(BigInteger id) {
        return jdbcTemplate.query(
                        "SELECT FROM posts WHERE id = ?",
                        (rs, row) -> PostModel.builder()
                                .id(rs.getBigDecimal("id").toBigInteger())
                                .title(rs.getString("title"))
                                .content(rs.getString("content"))
                                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                .build())
                .getFirst();
    }

    @Override
    public List<PostModel> findAll(int page, int size) {
        return jdbcTemplate.query(
                "SELECT * FROM posts ORDER BY updated_at LIMIT ? OFFSET ?",
                (rs, row) -> PostModel.builder()
                        .id(rs.getBigDecimal("id").toBigInteger())
                        .title(rs.getString("title"))
                        .content(rs.getString("content"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                        .build(),
                size,
                page * size
        );
    }

    @Override
    public PostModel save(PostModel postModel) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO posts(title, content) VALUES (?, ?) RETURNING *",
                (rs, rowNum) -> PostModel.builder()
                        .id(rs.getBigDecimal("id").toBigInteger())
                        .title(rs.getString("title"))
                        .content(rs.getString("content"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                        .build(),
                postModel.getTitle(),
                postModel.getContent()
        );
    }

    @Override
    public int deleteById(BigInteger id) {
        return jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }
}
