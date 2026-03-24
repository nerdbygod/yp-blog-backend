package org.practicum.yandex.service.image;

import org.practicum.yandex.service.exception.InvalidImageException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    void addImage(final Long postId, final MultipartFile file) throws InvalidImageException;

    void updateImage(final Long postId, final MultipartFile file) throws InvalidImageException;

    Resource downloadImage(Long postId);

    boolean imageExistsByPostId(Long postId);
}
