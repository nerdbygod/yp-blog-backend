package org.practicum.yandex.service.post;

import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.practicum.yandex.service.post.dto.PostDataWrapper;

import java.util.Set;

public interface PostService {
    PostModel createPost(final CreatePostRequest request);

    PostModel getPost(final Long postId);

    PostModel updatePost(final Long postId, final CreatePostRequest request);

    PostDataWrapper getPosts(final long page, final long size, final String query, final Set<String> tags);

    boolean postExists(final Long postId);

    void deletePost(final Long postId) throws DataNotFoundException;

    Long incrementLikes(final Long postId);

    void updatePostImage(byte[] content);

    byte[] getPostImage(final Long postId);
}
