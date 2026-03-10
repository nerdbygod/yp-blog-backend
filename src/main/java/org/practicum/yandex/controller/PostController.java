package org.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.Constants;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.converter.PostModelToDtoConverter;
import org.practicum.yandex.service.post.PostService;
import org.practicum.yandex.service.validation.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

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
}
