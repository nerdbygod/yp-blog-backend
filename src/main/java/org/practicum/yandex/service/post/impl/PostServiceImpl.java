package org.practicum.yandex.service.post.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.Constants;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.converter.PostRequestToModelConverter;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.practicum.yandex.service.exception.DataNotFoundException;
import org.practicum.yandex.service.post.PostService;
import org.practicum.yandex.service.post.dto.PostDataWrapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public PostModel getPost(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    @Override
    // FIXME: should be transactional
    public PostModel updatePost(Long postId, CreatePostRequest request) {
        final var postModel = PostModel.builder()
                .title(request.getTitle())
                .content(request.getTitle())
                .build();

        postRepository.update(postId, postModel);
        postRepository.removePostTags(postId);

        if (CollectionUtils.isNotEmpty(request.getTags())) {
            final var tagIds = postRepository.getOrInsertTags(new ArrayList<>(request.getTags()));
            postRepository.saveTags(postId, tagIds);
        }

        return postRepository.findById(postId).orElseThrow();
    }

    @Override
    public PostDataWrapper getPosts(long page, long size, String query, Set<String> tags) {
        final var postModelPage = postRepository.findPaged(page, size, query, tags);

        final var totalPosts = postModelPage.getCount();

        if (totalPosts > 0) {
            final var totalPages = totalPosts % size == 0 ? totalPosts / size : (totalPosts / size) + 1;

            return PostDataWrapper.builder()
                    .posts(postModelPage.getData())
                    .count(postModelPage.getCount())
                    .hasPrev(page > 1)
                    .hasNext(page < totalPages)
                    .lastPage(totalPages)
                    .build();
        }

        return PostDataWrapper.builder()
                .posts(Collections.emptyList())
                .count(0L)
                .build();
    }

    @Override
    public boolean postExists(Long postId) {
        return postRepository.existsById(postId);
    }

    @Override
    public void deletePost(Long postId) throws DataNotFoundException {
        final var deleted = postRepository.deleteById(postId) > 0;

        if (!deleted) {
            throw new DataNotFoundException();
        }
    }

    @Override
    public Long incrementLikes(Long postId) {
        return postRepository.incrementLikes(postId);
    }

    @Override
    public void updatePostImage(byte[] content) {

    }

    @Override
    public byte[] getPostImage(Long postId) {
        return new byte[0];
    }

    protected List<String> extractTags(final CreatePostRequest request) {
        return Optional.ofNullable(request)
                .map(CreatePostRequest::getTags)
                .filter(CollectionUtils::isNotEmpty)
                .orElse(Collections.emptySet())
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::normalizeSpace)
                .map(tag -> tag.replace(Constants.HASHTAG, StringUtils.EMPTY))
                .filter(StringUtils::isNotBlank)
                .toList();
    }
}
