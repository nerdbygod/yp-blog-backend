package org.practicum.yandex.service.post;

import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.service.exception.DataNotFoundException;

import java.math.BigInteger;
import java.util.List;

public interface PostService {
    PostModel createPost(final CreatePostRequest request);

    PostModel getPost(final BigInteger postId);

    PostModel updatePost(final BigInteger postId, final CreatePostRequest request);

    List<PostModel> getPosts(final String query, final int page, final int size);

    boolean postExists(final BigInteger postId);

    void deletePost(final BigInteger postId) throws DataNotFoundException;

    BigInteger incrementLikes(final BigInteger postId);

    void updatePostImage(byte[] content);

    byte[] getPostImage(final BigInteger postId);
}
