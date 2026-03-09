package org.practicum.yandex.service.post;

import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.persistence.post.PostModel;

import java.math.BigInteger;
import java.util.List;

public interface PostService {
    PostModel createPost(final PostDto postDto);

    PostModel getPost(final BigInteger postId);

    PostModel updatePost(final BigInteger postId, final PostDto updatedPost);

    List<PostModel> getPosts(final String query, final int page, final int size);

    void deletePost(final BigInteger postId);

    int incrementLikes(final BigInteger postId);

    void updatePostImage(byte[] content);

    byte[] getPostImage(final BigInteger postId);
}
