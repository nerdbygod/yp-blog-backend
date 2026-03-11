package org.practicum.yandex.service.post.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.converter.PostRequestToModelConverter;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.practicum.yandex.service.post.PostService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostRequestToModelConverter postRequestToModelConverter;

    @Override
    // FIXME: should be transactional
    public PostModel createPost(final CreatePostRequest request) {
        final var savedPost = postRepository.save(postRequestToModelConverter.convert(request));
        final var tagNames = extractTags(request);

        if (CollectionUtils.isNotEmpty(tagNames)) {
            final var tagIds = postRepository.getOrInsertTags(tagNames);
            postRepository.saveTags(savedPost.getId(), tagIds);
        }

        return postRepository.findById(savedPost.getId()).orElseThrow();
    }

    @Override
    @Nullable
    public PostModel getPost(BigInteger postId) {
        return postRepository.findById(postId).orElse(null);
    }

    @Override
    public PostModel updatePost(BigInteger postId, PostDto updatedPost) {
        return null;
    }

    @Override
    public List<PostModel> getPosts(String query, int page, int size) {
        return null;
    }

    @Override
    public void deletePost(BigInteger postId) {

    }

    @Override
    public int incrementLikes(BigInteger postId) {
        return 0;
    }

    @Override
    public void updatePostImage(byte[] content) {

    }

    @Override
    public byte[] getPostImage(BigInteger postId) {
        return new byte[0];
    }

    protected List<String> extractTags(final CreatePostRequest request) {
        return Optional.ofNullable(request)
                .map(CreatePostRequest::getTags)
                .filter(CollectionUtils::isNotEmpty)
                .orElse(Collections.emptyList())
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::normalizeSpace)
                .toList();
    }
}
