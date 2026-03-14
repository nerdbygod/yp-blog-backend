package org.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.practicum.yandex.Constants;
import org.practicum.yandex.service.exception.InvalidImageException;
import org.practicum.yandex.service.image.ImageService;
import org.practicum.yandex.service.post.PostService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.Controller.API_POSTS)
public class ImageController {
    private final ImageService imageService;
    private final PostService postService;

    @PutMapping("/{postId}/image")
    public ResponseEntity<Void> uploadImage(final @PathVariable(Constants.Controller.POST_ID) Long postId,
                                            final @RequestParam("filename") MultipartFile file) {
        if (!postService.postExists(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            if (!imageService.imageExistsByPostId(postId)) {
                imageService.addImage(postId, file);
            } else {
                imageService.updateImage(postId, file);
            }

            return ResponseEntity.ok().build();
        } catch (InvalidImageException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.warn("Unhandled error occurred while trying to upload image", e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<Resource> downloadImage(
            final @PathVariable(Constants.Controller.POST_ID) Long postId) {

        if (!postService.postExists(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!imageService.imageExistsByPostId(postId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        final var imageResource = imageService.downloadImage(postId);

        if (Objects.isNull(imageResource)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageResource);
    }
}
