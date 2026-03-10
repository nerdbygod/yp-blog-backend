package org.practicum.yandex.service.post.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.converter.PostDtoToModelConverter;
import org.practicum.yandex.persistence.post.PostModel;
import org.practicum.yandex.repository.PostRepository;
import org.practicum.yandex.service.post.PostService;
import org.practicum.yandex.service.validation.PostValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostDtoToModelConverter postDtoToModelConverter;

    @Override
    // FIXME: should be transactional
    public PostModel createPost(PostDto postDto) {
        if (Objects.isNull(postDto) || PostValidator.isInvalid(postDto)) {
            throw new IllegalArgumentException("Invalid post data");
        }

        try {
            final var savedPost = postRepository.save(postDtoToModelConverter.convert(postDto));
            final var tagNames = extractTags(postDto);

            if (CollectionUtils.isNotEmpty(tagNames)) {
                final var tagIds = postRepository.getOrInsertTags(tagNames);
                postRepository.saveTags(savedPost.getId(), tagIds);
            }

            return postRepository.findById(savedPost.getId());
        } catch (Exception e) {
            // TODO: replace with custom exception
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public PostModel getPost(BigInteger postId) {
        return null;
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

    protected List<String> extractTags(final PostDto postDto) {
        return Optional.ofNullable(postDto)
                .map(PostDto::getTags)
                .filter(CollectionUtils::isNotEmpty)
                .orElse(Collections.emptyList())
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::normalizeSpace)
                .toList();
    }
}
