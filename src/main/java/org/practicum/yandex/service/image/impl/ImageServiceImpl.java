package org.practicum.yandex.service.image.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.tika.Tika;
import org.practicum.yandex.persistence.image.ImageModel;
import org.practicum.yandex.repository.ImageRepository;
import org.practicum.yandex.service.exception.InvalidImageException;
import org.practicum.yandex.service.image.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private static final Tika TIKA = new Tika();

    private final ImageRepository imageRepository;

    @Value("${image.uploadDir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        createUploadDirIfNeeded();
    }

    @Override
    public void addImage(Long postId, MultipartFile file) throws InvalidImageException {
        String fileName = null;

        try {
            fileName = relocateImage(file);

            final var imageModel = ImageModel.builder()
                    .postId(postId)
                    .fileName(fileName)
                    .build();

            imageRepository.save(imageModel);
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while adding image to post {}, attempting to delete image",
                    postId, e
            );

            tryDeleteImage(fileName);

            throw e;
        }

    }

    @Override
    public void updateImage(Long postId, MultipartFile file) throws InvalidImageException {
        final var imageModel = imageRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("No image found for post %s", postId))
                );

        final var currentFileName = imageModel.getFileName();

        tryDeleteImage(currentFileName);

        String newFileName = null;

        try {
            newFileName = relocateImage(file);

            imageModel.setFileName(newFileName);

            imageRepository.update(imageModel.getId(), imageModel);
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while adding image to post {}", postId, e);

            tryDeleteImage(newFileName);

            throw e;
        }
    }

    @Nullable
    @Override
    public Resource downloadImage(Long postId) {
        final var imageModel = imageRepository.findByPostId(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("No image found for post %s", postId))
                );

        try {
            final var filePath = Paths.get(uploadDir).resolve(imageModel.getFileName()).normalize();

            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                final var content = Files.readAllBytes(filePath);

                return new ByteArrayResource(content);
            } else {
                log.warn("No image was found for post {}; deleting image", postId);

                imageRepository.deleteById(imageModel.getId());

                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean imageExistsByPostId(Long postId) {
        return false;
    }

    protected void tryDeleteImage(final String fileName) {
        try {
            if (StringUtils.isNotEmpty(fileName)) {
                final var filePath = Paths.get(uploadDir).resolve(fileName).normalize();

                if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String relocateImage(MultipartFile file) throws InvalidImageException {
        try {
            if (file.isEmpty()) {
                throw new InvalidImageException("Cannot save empty file");
            }

            final var mediaType = TIKA.detect(file.getInputStream());

            if (StringUtils.isBlank(mediaType) || !LocalConstants.ALLOWED_IMAGE_FORMATS.contains(mediaType)) {
                throw new InvalidImageException(String.format("Unknown image format: %s", mediaType));
            }

            final var fileName = getFileName(mediaType);
            final var filePath = Paths.get(uploadDir).resolve(fileName);

            file.transferTo(filePath);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createUploadDirIfNeeded() {
        try {
            final var uploadDirPath = Paths.get(uploadDir);

            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String determineExtension(final String mediaType) {
        return switch (mediaType) {
            case LocalConstants.JPEG_MIME -> LocalConstants.JPEG;
            case LocalConstants.PNG_MIME -> LocalConstants.PNG;
            default -> LocalConstants.UNKNOWN;
        };
    }

    private static String getFileName(final @NonNull String mediaType) throws InvalidImageException {
        final var extension = determineExtension(mediaType);

        if (isUnknownExtension(extension)) {
            throw new InvalidImageException(String.format("Unknown image extension: %s", mediaType));
        }

        return String.format(LocalConstants.FILE_FORMAT_TEMPLATE, UUID.randomUUID(), extension);
    }

    private static boolean isUnknownExtension(String extension) {
        return Strings.CI.equals(LocalConstants.UNKNOWN, extension);
    }

    private static final class LocalConstants {
        private static final String FILE_FORMAT_TEMPLATE = "%s.%s";

        private static final String JPEG_MIME = "image/jpeg";
        private static final String PNG_MIME = "image/png";

        private static final String JPEG = "jpeg";
        private static final String PNG = "png";
        private static final String UNKNOWN = "unknown";

        private static final Set<String> ALLOWED_IMAGE_FORMATS = Set.of(JPEG_MIME, PNG_MIME);
    }
}
