package org.practicum.yandex.repository.impl;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.persistence.AbstractModel;
import org.practicum.yandex.persistence.image.ImageModel;
import org.practicum.yandex.repository.ImageRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ImageModel> findById(Long id) {
        try {
            final var imageModel = jdbcTemplate.queryForObject(
                    "SELECT * FROM images WHERE id = ?",
                    (rs, rowNum) -> mapToImageModel(rs),
                    id
            );

            return Optional.of(imageModel);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ImageModel save(ImageModel entity) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO images(post_id, file_name) VALUES (?, ?) RETURNING *",
                (rs, rowNum) -> mapToImageModel(rs),
                entity.getPostId(),
                entity.getFileName()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM images WHERE id = ?", id);
    }

    @Override
    public ImageModel update(Long id, ImageModel updatedEntity) {
        final var query = """
                UPDATE images
                SET file_name = ?
                WHERE id = ?
                RETURNING *
                """;

        return jdbcTemplate.queryForObject(
                query, (rs, rowNum) -> mapToImageModel(rs),
                updatedEntity.getFileName(),
                id
        );
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM images WHERE id = ?)",
                Boolean.class, id
        );
    }

    @Override
    public Optional<ImageModel> findByPostId(Long postId) {
        try {
            final var imageModel = jdbcTemplate.queryForObject(
                    "SELECT * FROM images WHERE post_id = ?",
                    (rs, rowNum) -> mapToImageModel(rs),
                    postId
            );

            return Optional.of(imageModel);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected static ImageModel mapToImageModel(ResultSet resultSet) throws SQLException {
        return ImageModel.builder()
                .id(resultSet.getLong(AbstractModel.Fields.id))
                .postId(resultSet.getLong("post_id"))
                .fileName(resultSet.getString("file_name"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
