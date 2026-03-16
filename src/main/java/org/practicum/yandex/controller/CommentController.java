package org.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import org.practicum.yandex.Constants;
import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.converter.CommentModelToDtoConverter;
import org.practicum.yandex.service.comment.CommentService;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.practicum.yandex.service.post.PostService;
import org.practicum.yandex.service.validation.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.Controller.API_POSTS)
public class CommentController {
    private final PostService postService;
    private final CommentService commentService;
    private final CommentModelToDtoConverter commentModelToDtoConverter;

    @GetMapping("/{postId}/comments")
    public List<CommentDto> getPostComments(final @PathVariable("postId") Long postId) {
        if (postService.postExists(postId)) {
            return commentService.getAllComments(postId)
                    .stream()
                    .map(commentModelToDtoConverter::convert)
                    .toList();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto getComment(final @PathVariable("postId") Long postId,
                                 final @PathVariable("commentId") Long commentId) {
        if (postService.postExists(postId)) {
            return commentService.getComment(commentId)
                    .map(commentModelToDtoConverter::convert)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{postId}/comments")
    public CommentDto addComment(final @RequestBody AddCommentRequest request) {
        if (Objects.isNull(request) || RequestValidator.isInvalid(request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }

        final var savedComment = commentService.addComment(request);

        return commentModelToDtoConverter.convert(savedComment);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentDto updateComment(final @PathVariable("postId") Long postId,
                                    final @PathVariable("commentId") Long commentId,
                                    final @RequestBody CommentDto commentDto) {
        if (Objects.isNull(commentDto) || RequestValidator.isInvalid(commentDto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }

        if (postService.postExists(postId) && commentService.commentExists(commentId)) {
            final var updated = commentService.updateComment(commentId, commentDto);

            return commentModelToDtoConverter.convert(updated);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(final @PathVariable("postId") Long postId,
                                              final @PathVariable("commentId") Long commentId) {

        if (postService.postExists(postId)) {
            try {
                commentService.deleteComment(commentId);

                return ResponseEntity.ok().build();
            } catch (DataNotFoundException e) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Comment with id %d not found", commentId)
                );
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Post with id %d not found", postId)
            );
        }
    }
}
