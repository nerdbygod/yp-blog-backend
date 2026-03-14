package org.practicum.yandex.service.comment.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.converter.CommentDtoToModelConverter;
import org.practicum.yandex.persistence.comment.CommentModel;
import org.practicum.yandex.repository.CommentRepository;
import org.practicum.yandex.service.comment.CommentService;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentDtoToModelConverter commentDtoToModelConverter;

    @Override
    public List<CommentModel> getAllComments(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    @Override
    public Optional<CommentModel> getComment(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public boolean commentExists(Long commentId) {
        return commentRepository.existsById(commentId);
    }

    @Override
    public CommentModel addComment(AddCommentRequest addCommentRequest) {
        final var commentModel = buildCommentModel(addCommentRequest);

        return commentRepository.save(commentModel);
    }

    @Override
    public CommentModel updateComment(final Long commentId, CommentDto commentDto) {
        final var commentModel = commentDtoToModelConverter.convert(commentDto);

        return commentRepository.update(commentId, commentModel);
    }

    @Override
    public void deleteComment(Long commentId) throws DataNotFoundException {
        final var deleted = commentRepository.deleteById(commentId) > 0;

        if (!deleted) {
            throw new DataNotFoundException();
        }
    }

    protected static CommentModel buildCommentModel(final AddCommentRequest request) {
        final var content = StringUtils.trim(request.getText());

        return CommentModel.builder()
                .postId(request.getPostId())
                .content(content)
                .build();
    }
}
