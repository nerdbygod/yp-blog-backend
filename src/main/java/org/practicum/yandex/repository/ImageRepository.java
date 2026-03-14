package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.image.ImageModel;

import java.util.Optional;

public interface ImageRepository extends SqlRepository<Long, ImageModel> {
    Optional<ImageModel> findByPostId(final Long postId);
}
