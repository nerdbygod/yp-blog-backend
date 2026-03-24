package org.practicum.yandex.service.comment;

import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.persistence.comment.CommentModel;
import org.practicum.yandex.service.exception.DataNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<CommentModel> getAllComments(final Long postId);

    Optional<CommentModel> getComment(final Long commentId);

    boolean commentExists(final Long commentId);

    CommentModel addComment(final AddCommentRequest addCommentRequest);

    CommentModel updateComment(final Long commentId, final CommentDto commentDto);

    void deleteComment(final Long commentId) throws DataNotFoundException;
}
