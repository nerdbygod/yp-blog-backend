package org.practicum.yandex.service.post;

import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.practicum.yandex.service.post.dto.PostDataWrapper;

import java.math.BigInteger;
import java.util.Set;

public interface PostService {
    PostModel createPost(final CreatePostRequest request);

    PostModel getPost(final BigInteger postId);

    PostModel updatePost(final BigInteger postId, final CreatePostRequest request);

    PostDataWrapper getPosts(final long page, final long size, final String query, final Set<String> tags);

    boolean postExists(final BigInteger postId);

    void deletePost(final BigInteger postId) throws DataNotFoundException;

    BigInteger incrementLikes(final BigInteger postId);

    void updatePostImage(byte[] content);

    byte[] getPostImage(final BigInteger postId);
}
