package org.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.Constants;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.converter.PostModelToDtoConverter;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.practicum.yandex.service.post.PostService;
import org.practicum.yandex.service.validation.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.Controller.API_POSTS)
public class PostController {
    private final PostService postService;
    private final PostModelToDtoConverter postModelToDtoConverter;

    @PostMapping
    public PostDto createPost(final @RequestBody CreatePostRequest request) {
        if (Objects.isNull(request) || RequestValidator.isInvalid(request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }

        try {
            final var postModel = postService.createPost(request);
            return postModelToDtoConverter.convert(postModel);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/{postId}")
    public PostDto getPostById(final @PathVariable(Constants.Controller.POST_ID) BigInteger postId) {
        return Optional.ofNullable(postService.getPost(postId))
                .map(postModelToDtoConverter::convert)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{postId}")
    public PostDto updatePost(final @PathVariable(Constants.Controller.POST_ID) BigInteger postId,
                              final @RequestBody CreatePostRequest request) {
        if (Objects.isNull(request) || RequestValidator.isInvalid(request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }

        if (!postService.postExists(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            final var updatedPostModel = postService.updatePost(postId, request);

            return postModelToDtoConverter.convert(updatedPostModel);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(final @PathVariable(Constants.Controller.POST_ID) BigInteger postId) {
        try {
            postService.deletePost(postId);

            return ResponseEntity.ok().build();
        } catch (DataNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Post with id %d not found", postId.longValue())
            );
        }
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<BigInteger> likePost(final @PathVariable(Constants.Controller.POST_ID) BigInteger postId) {
        if (!postService.postExists(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var updatedLikes = postService.incrementLikes(postId);

        return ResponseEntity.ok().body(updatedLikes);
    }
}
