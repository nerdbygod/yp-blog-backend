package org.practicum.yandex.repository;

import org.practicum.yandex.persistence.comment.CommentModel;

import java.util.List;

public interface CommentRepository extends SqlRepository<Long, CommentModel> {
    List<CommentModel> findAllByPostId(final Long postId);
}
