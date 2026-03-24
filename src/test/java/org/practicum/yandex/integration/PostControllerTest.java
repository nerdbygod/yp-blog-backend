package org.practicum.yandex.integration;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.controller.dto.response.GetPostListResponse;
import org.practicum.yandex.persistence.AbstractModel;
import org.practicum.yandex.storage.TestData;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PostControllerTest extends BaseControllerTest {

    @Test
    void testGetPosts_throwsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(postsPath()))
                .andExpect(MockMvcResultMatchers
                        .status().isBadRequest());

        final var paramsWithMissingPageNumber = prepareParams(null, 5, null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(postsPath())
                        .params(paramsWithMissingPageNumber))
                .andExpect(MockMvcResultMatchers
                        .status().isBadRequest());

        final var paramsWithMissingPageSize = prepareParams(1, null, null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(postsPath())
                        .params(paramsWithMissingPageSize))
                .andExpect(MockMvcResultMatchers
                        .status().isBadRequest());
    }

    @Test
    void testGetPosts_noPostsReturned() throws Exception {
        final var getPostsResponse = getPosts(1, 5, null);

        assertThat(getPostsResponse).isInstanceOf(GetPostListResponse.class);
        assertThat(getPostsResponse.getPosts()).isEmpty();
        assertThat(getPostsResponse.isHasPrev()).isFalse();
        assertThat(getPostsResponse.isHasNext()).isFalse();
        assertThat(getPostsResponse.getLastPage()).isNull();
    }

    @Test
    void testCreatePostSuccessful() throws Exception {
        final var createPostRequest = TestData.getDefaultCreatePostRequest();
        final var createdPost = createPost(createPostRequest);

        assertThat(createdPost).isInstanceOf(PostDto.class);
        assertThat(createdPost.getId()).isNotNull();
        assertThat(createdPost.getCommentsCount()).isZero();
        assertThat(createdPost.getLikesCount()).isZero();
        assertThat(createdPost.getTags()).containsExactlyInAnyOrderElementsOf(createPostRequest.getTags());
        assertThat(createdPost.getTitle()).isEqualTo(createPostRequest.getTitle());
        assertThat(createdPost.getText()).isEqualTo(createPostRequest.getText());
    }

    @Test
    void testCreatePost_postIsReturnedWhenRequested() throws Exception {
        final var createdPost = createPost(TestData.getDefaultCreatePostRequest());

        final var findPostByIdResponse = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(postByIdPath(createdPost.getId())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var foundPost = MAPPER.readValue(findPostByIdResponse, PostDto.class);

        assertThat(foundPost).isInstanceOf(PostDto.class);
        assertThat(foundPost.getId()).isNotNull();
        assertThat(foundPost.getCommentsCount()).isEqualTo(0);
        assertThat(foundPost.getLikesCount()).isEqualTo(0);
        assertThat(foundPost.getTags()).isEqualTo(createdPost.getTags());
        assertThat(foundPost.getTitle()).isEqualTo(createdPost.getTitle());
        assertThat(foundPost.getText()).isEqualTo(createdPost.getText());
    }

    @Test
    void testPagedPostsAreReturned() throws Exception {
        final var createdPosts = createPosts(10);

        int pageNumber = 1, pageSize = 5;
        final var getPostsResponseFirstPage = getPosts(pageNumber, pageSize, null);

        assertThat(getPostsResponseFirstPage).isNotNull();
        assertThat(getPostsResponseFirstPage.getPosts()).hasSize(pageSize);
        assertThat(getPostsResponseFirstPage.isHasPrev()).isFalse();
        assertThat(getPostsResponseFirstPage.isHasNext()).isTrue();
        assertThat(getPostsResponseFirstPage.getLastPage()).isEqualTo(2);

        // Last created post is the first returned post as they're sorted by updated_at in descending order (newest go firs)
        final var lastCreatedPost = createdPosts.getLast();
        final var firstRequestedPost = getPostsResponseFirstPage.getPosts().getFirst();

        assertThat(firstRequestedPost.getId()).isEqualTo(10);
        assertThat(firstRequestedPost)
                .usingRecursiveComparison()
                .ignoringFields(AbstractModel.Fields.id,
                        AbstractModel.Fields.createdAt,
                        AbstractModel.Fields.updatedAt)
                .ignoringCollectionOrder()
                .isEqualTo(lastCreatedPost);

        pageNumber = 2;
        final var getPostsResponseSecondPage = getPosts(pageNumber, pageSize, null);

        assertThat(getPostsResponseSecondPage).isNotNull();
        assertThat(getPostsResponseSecondPage.getPosts()).hasSize(pageSize);
        assertThat(getPostsResponseSecondPage.isHasPrev()).isTrue();
        assertThat(getPostsResponseSecondPage.isHasNext()).isFalse();
        assertThat(getPostsResponseSecondPage.getLastPage()).isEqualTo(2);
    }

    @Test
    void testPostsAreFilteredByTitleSuccessfully() throws Exception {
        final var createdPosts = createPosts(10);

        int pageNumber = 1, pageSize = 5;
        final var getPostsResponseFirstPage = getPosts(pageNumber, pageSize, "2");

        assertThat(getPostsResponseFirstPage).isNotNull();
        assertThat(getPostsResponseFirstPage.getPosts()).hasSize(1);
        assertThat(getPostsResponseFirstPage.isHasPrev()).isFalse();
        assertThat(getPostsResponseFirstPage.isHasNext()).isFalse();
        assertThat(getPostsResponseFirstPage.getLastPage()).isEqualTo(1);

        final var searchedPost = createdPosts.stream()
                .filter(post -> post.getTitle().contains("2"))
                .findFirst()
                .orElseThrow();
        final var foundPost = getPostsResponseFirstPage.getPosts().getFirst();

        assertThat(foundPost)
                .usingRecursiveComparison()
                .ignoringFields(AbstractModel.Fields.id,
                        AbstractModel.Fields.createdAt,
                        AbstractModel.Fields.updatedAt)
                .ignoringCollectionOrder()
                .isEqualTo(searchedPost);
    }

    @Test
    void testPostsAreFilteredByTagsSuccessfully() throws Exception {
        final var firstPostTags = Set.of(TestData.Tags.JAVA, TestData.Tags.PYTHON, TestData.Tags.GOLANG, TestData.Tags.HISTORY);
        final var secondPostTags = Set.of(TestData.Tags.JAVA, TestData.Tags.MATHS, TestData.Tags.HISTORY);
        final var thirdPostTags = Set.of(TestData.Tags.MATHS, TestData.Tags.ECONOMICS, TestData.Tags.HISTORY);
        final var fourthPostTags = Set.of(TestData.Tags.PHYSICS, TestData.Tags.MONEY, TestData.Tags.SPACE);

        createPost(CreatePostRequest.builder()
                .title("First post")
                .text("Text about Java and Python")
                .tags(firstPostTags)
                .build());

        createPost(CreatePostRequest.builder()
                .title("Second post")
                .text("Text about Maths and History")
                .tags(secondPostTags)
                .build());

        createPost(CreatePostRequest.builder()
                .title("Third post")
                .text("Text about Economics")
                .tags(thirdPostTags)
                .build());

        createPost(CreatePostRequest.builder()
                .title("Fourth post")
                .text("Text about Space")
                .tags(fourthPostTags)
                .build());

        int pageNumber = 1, pageSize = 5;
        final var postsResponseFilteredByHistoryTag = getPosts(pageNumber, pageSize, withHashtag(TestData.Tags.HISTORY));

        assertThat(postsResponseFilteredByHistoryTag).isNotNull();
        assertThat(postsResponseFilteredByHistoryTag.isHasPrev()).isFalse();
        assertThat(postsResponseFilteredByHistoryTag.isHasNext()).isFalse();
        assertThat(postsResponseFilteredByHistoryTag.getLastPage()).isEqualTo(1);
        assertThat(postsResponseFilteredByHistoryTag.getPosts()).hasSize(3)
                .extracting(PostDto::getTags)
                .allMatch(tags -> CollectionUtils.isNotEmpty(tags) && tags.contains(TestData.Tags.HISTORY));

        final var postsResponseFilteredByJavaAndHistoryTags = getPosts(
                pageNumber, pageSize, toHashtags(TestData.Tags.HISTORY, TestData.Tags.JAVA)
        );

        assertThat(postsResponseFilteredByJavaAndHistoryTags).isNotNull();
        assertThat(postsResponseFilteredByJavaAndHistoryTags.isHasPrev()).isFalse();
        assertThat(postsResponseFilteredByJavaAndHistoryTags.isHasNext()).isFalse();
        assertThat(postsResponseFilteredByJavaAndHistoryTags.getLastPage()).isEqualTo(1);
        assertThat(postsResponseFilteredByJavaAndHistoryTags.getPosts()).hasSize(2)
                .extracting(PostDto::getTags)
                .allMatch(tags -> CollectionUtils.isNotEmpty(tags) &&
                        tags.containsAll(List.of(TestData.Tags.HISTORY, TestData.Tags.JAVA)));

        final var postsResponseFilteredByUniqueTags = getPosts(
                pageNumber, pageSize, toHashtags(TestData.Tags.PHYSICS, TestData.Tags.MONEY, TestData.Tags.SPACE)
        );

        assertThat(postsResponseFilteredByUniqueTags).isNotNull();
        assertThat(postsResponseFilteredByUniqueTags.isHasPrev()).isFalse();
        assertThat(postsResponseFilteredByUniqueTags.isHasNext()).isFalse();
        assertThat(postsResponseFilteredByUniqueTags.getLastPage()).isEqualTo(1);
        assertThat(postsResponseFilteredByUniqueTags.getPosts()).hasSize(1)
                .extracting(PostDto::getTags)
                .allMatch(tags -> CollectionUtils.isNotEmpty(tags) &&
                        tags.containsAll(List.of(TestData.Tags.PHYSICS, TestData.Tags.MONEY, TestData.Tags.SPACE)));

        final var postsResponseFilteredByUnknownTags = getPosts(pageNumber, pageSize, withHashtag("unknown"));

        assertThat(postsResponseFilteredByUnknownTags).isNotNull();
        assertThat(postsResponseFilteredByUnknownTags.isHasPrev()).isFalse();
        assertThat(postsResponseFilteredByUnknownTags.isHasNext()).isFalse();
        assertThat(postsResponseFilteredByUnknownTags.getLastPage()).isNull();
        assertThat(postsResponseFilteredByUnknownTags.getPosts()).isEmpty();
    }

    @Test
    void testIncrementLikesIsSuccessful() throws Exception {
        final var createdPost = createPost(TestData.getDefaultCreatePostRequest());

        assertThat(createdPost.getLikesCount()).isZero();

        final var count = 10;

        long likes = 0;
        for (int i = 0; i < count; i++) {
            likes = incrementLikes(createdPost.getId());
        }

        assertThat(likes).isEqualTo(count);

        final var foundPost = getExistingPostById(createdPost.getId());

        assertThat(foundPost).isNotNull();
        assertThat(foundPost).isInstanceOf(PostDto.class);
        assertThat(foundPost.getLikesCount()).isEqualTo(count);
    }

    @Test
    void testIncrementForNonExistingPost_isFailed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post(postByIdPath(100500) + "/likes"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDeletePostIsSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var foundPost = getExistingPostById(post.getId());

        assertThat(foundPost).isNotNull();

        deletePost(post.getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(postByIdPath(post.getId())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDeleteNonExistingPost_isFailed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(postByIdPath(100500)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testUpdatePostIsSuccessful() throws Exception {
        final var defaultPostRequest = TestData.getDefaultCreatePostRequest();
        final var post = createPost(defaultPostRequest);

        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo(defaultPostRequest.getTitle());
        assertThat(post.getText()).isEqualTo(defaultPostRequest.getText());
        assertThat(post.getTags()).containsExactlyInAnyOrderElementsOf(defaultPostRequest.getTags());

        final var updatePostRequest = TestData.getCreatePostRequest("Updated title", "Updated text", Set.of("new_tag"));
        final var updatedPost = updateExistingPost(post.getId(), updatePostRequest);

        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getTitle()).isEqualTo(updatePostRequest.getTitle());
        assertThat(updatedPost.getText()).isEqualTo(updatePostRequest.getText());
        assertThat(updatedPost.getTags()).containsExactlyInAnyOrderElementsOf(updatePostRequest.getTags());
        assertThat(updatedPost.getCommentsCount()).isEqualTo(post.getCommentsCount());
        assertThat(updatedPost.getLikesCount()).isEqualTo(post.getLikesCount());
    }

    @Test
    void testUpdateNonExistingPost_isFailed() throws Exception {
        final var updatePostRequestJson = MAPPER.writeValueAsString(TestData.getDefaultCreatePostRequest());

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(postByIdPath(100500))
                                .content(updatePostRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testMalformedUpdatePostRequest_isRejected() throws Exception {
        final var postId = createPost(TestData.getDefaultCreatePostRequest()).getId();

        final var emptyUpdateRequest = MAPPER.writeValueAsString(new CreatePostRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .put(postByIdPath(postId))
                        .content(emptyUpdateRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        final var missingTextRequest = MAPPER.writeValueAsString(
                CreatePostRequest.builder()
                        .text("")
                        .title("Title")
                        .tags(Set.of())
                        .build()
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .put(postByIdPath(postId))
                        .content(missingTextRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        final var missingTitleRequest = MAPPER.writeValueAsString(
                CreatePostRequest.builder()
                        .text("Test")
                        .title("")
                        .tags(Set.of())
                        .build()
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .put(postByIdPath(postId))
                        .content(missingTitleRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    protected PostDto updateExistingPost(long postId, CreatePostRequest request) throws Exception {
        final var createPostRequestJson = MAPPER.writeValueAsString(request);

        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(postByIdPath(postId))
                                .content(createPostRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, PostDto.class);
    }

    protected List<PostDto> createPosts(final int count) throws Exception {
        final var createdPosts = new ArrayList<PostDto>();

        for (int i = 0; i < count; i++) {
            final var request = TestData.getCreatePostRequest(
                    "Post " + i, "Post content " + i, Set.of("common_tag", "custom_tag_" + i)
            );

            createdPosts.add(createPost(request));
        }

        return createdPosts;
    }

    protected long incrementLikes(long postId) throws Exception {
        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(postByIdPath(postId) + "/likes"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        try {
            return Long.parseLong(response);
        } catch (NumberFormatException e) {
            throw new AssertionError(String.format("Unable to parse response; should be long, got [%s]", response));
        }
    }

    protected String toHashtags(String... strings) {
        if (Objects.nonNull(strings) && strings.length > 0) {
            final var sb = new StringBuilder();

            for (String s : strings) {
                sb.append(withHashtag(s)).append(" ");
            }

            return sb.toString();
        }

        return StringUtils.EMPTY;
    }

    protected String withHashtag(String s) {
        return "#" + s;
    }

}
