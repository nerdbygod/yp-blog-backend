package org.practicum.yandex.integration;

import org.apache.tika.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.service.comment.CommentService;
import org.practicum.yandex.storage.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentControllerIntegrationTest extends BaseControllerTest {
    @Autowired
    private CommentService commentService;

    @Test
    void testNoCommentsFoundForExistingPost() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var comments = getCommentsByExistingPostId(post.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void testNoCommentFoundForNonExistingPost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByPostIdPath(100500)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCommentIsAddedSuccessfully_withValidData() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var addCommentRequest = TestData.getDefaultAddCommentRequest(post.getId());
        final var comment = createCommentForExistingPost(addCommentRequest);

        assertThat(comment).isNotNull().isInstanceOf(CommentDto.class);
        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getPostId()).isEqualTo(post.getId());
        assertThat(comment.getText()).isEqualTo(addCommentRequest.getText());
    }

    @Test
    void testCommentAddRequestFailed_withInvalidData() throws Exception {
        var addCommentRequest = TestData.getAddCommentRequest(1L, "");
        var addCommentRequestJson = MAPPER.writeValueAsString(addCommentRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(commentsByPostIdPath(1))
                                .content(addCommentRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        addCommentRequest = TestData.getAddCommentRequest(null, "Some text");
        addCommentRequestJson = MAPPER.writeValueAsString(addCommentRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(commentsByPostIdPath(1))
                                .content(addCommentRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        addCommentRequest = TestData.getAddCommentRequest(1L, null);
        addCommentRequestJson = MAPPER.writeValueAsString(addCommentRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(commentsByPostIdPath(1))
                                .content(addCommentRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testAddCommentToNonExistingPost_isFailed() throws Exception {
        final var postId = 100L;

        final var addCommentRequestJson = MAPPER.writeValueAsString(TestData.getDefaultAddCommentRequest(postId));

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(commentsByPostIdPath(postId))
                                .content(addCommentRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCommentCountIsIncrementedAfterCommentAdded() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var commentCount = 7;

        for (int i = 0; i < commentCount; i++) {
            createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));
        }

        final var posts = getPosts(1, 5, null);

        assertThat(posts).isNotNull();
        assertThat(posts.getPosts()).hasSize(1);

        final var foundPost = posts.getPosts().getFirst();

        assertThat(foundPost.getCommentsCount()).isEqualTo(commentCount);

        final var foundPostById = getExistingPostById(post.getId());

        assertThat(foundPostById.getCommentsCount()).isEqualTo(commentCount);
    }

    @Test
    void testFindCommentsByPostId_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var commentCount = 5;

        for (int i = 0; i < commentCount; i++) {
            createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));
        }

        final var comments = getCommentsByExistingPostId(post.getId());

        assertThat(comments).hasSize(commentCount);
        assertThat(comments).extracting(CommentDto::getPostId)
                .allMatch(commentPostId -> Objects.equals(post.getId(), commentPostId));
    }

    @Test
    void testFindNonExistingCommentById_isFailed() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByIdPath(post.getId(), 100500)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testFindExistingCommentById_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var comment = createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));

        final var foundComment = getExistingCommentById(post.getId(), comment.getId());

        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(comment.getId());
        assertThat(foundComment.getPostId()).isEqualTo(post.getId());
        assertThat(foundComment.getText()).isEqualTo(comment.getText());
    }

    @Test
    void testUpdateCommentWithValidData_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var comment = createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));

        final var updatedCommentText = "Updated comment text";
        final var updateCommentDto = CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .text(updatedCommentText)
                .build();

        final var updatedComment = updateExistingCommentById(post.getId(), comment.getId(), updateCommentDto);

        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getPostId()).isEqualTo(post.getId());
        assertThat(updatedComment.getId()).isEqualTo(comment.getId());
        assertThat(updatedComment.getText()).isNotEqualTo(comment.getText())
                .isEqualTo(updatedCommentText);
    }

    @Test
    void testUpdateNonExistingComment_isFailed() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var updateCommentDto = CommentDto.builder()
                .id(100L)
                .postId(post.getId())
                .text("Updated comment text")
                .build();

        final var requestJson = MAPPER.writeValueAsString(updateCommentDto);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(commentsByIdPath(post.getId(), updateCommentDto.getId()))
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testUpdateCommentForNonExistingPost_isFailed() throws Exception {
        final var updateCommentDto = CommentDto.builder()
                .id(100L)
                .postId(500L)
                .text("Updated comment text")
                .build();

        final var requestJson = MAPPER.writeValueAsString(updateCommentDto);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(commentsByIdPath(updateCommentDto.getPostId(), updateCommentDto.getId()))
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDeleteExistingComment_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final var comment = createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));

        deleteExistingComment(post.getId(), comment.getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByIdPath(post.getId(), comment.getId())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDeleteNonExistingComment_isFailed() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());
        final long commentId = 100;

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(commentsByIdPath(post.getId(), commentId)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(String.format("Comment with id %d not found", commentId)));
    }

    @Test
    void testDeleteCommentForNonExistingPost_isFailed() throws Exception {
        final long postId = 100;

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(commentsByIdPath(postId, 500)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(String.format("Post with id %d not found", postId)));
    }

    @Test
    void testCommentCountIsDecreasedAfterCommentsDeleted() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var commentCount = 10;

        for (int i = 0; i < commentCount; i++) {
            createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));
        }

        final var commentsToDeleteCount = 3;

        for (int i = 0; i < commentsToDeleteCount; i++) {
            deleteExistingComment(post.getId(), (i + 1));
        }

        final var posts = getPosts(1, 5, null);

        assertThat(posts).isNotNull();
        assertThat(posts.getPosts()).hasSize(1);

        final var foundPost = posts.getPosts().getFirst();

        assertThat(foundPost.getCommentsCount()).isEqualTo(commentCount - commentsToDeleteCount);

        final var foundPostById = getExistingPostById(post.getId());

        assertThat(foundPostById.getCommentsCount()).isEqualTo(commentCount - commentsToDeleteCount);
    }

    @Test
    void testCommentsAreDeletedAfterPostIsDeleted() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var commentCount = 3;

        for (int i = 0; i < commentCount; i++) {
            createCommentForExistingPost(TestData.getDefaultAddCommentRequest(post.getId()));
        }

        deletePost(post.getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByPostIdPath(post.getId())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        final var comments = commentService.getAllComments(post.getId());

        assertThat(comments).isEmpty();
    }

    protected List<CommentDto> getCommentsByExistingPostId(long postId) throws Exception {
        final var response = mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByPostIdPath(postId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var mapped = MAPPER.readValue(response, CommentDto[].class);

        return Arrays.asList(mapped);
    }

    protected CommentDto getExistingCommentById(long postId, long commentId) throws Exception {
        final var response = mockMvc.perform(MockMvcRequestBuilders
                        .get(commentsByIdPath(postId, commentId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, CommentDto.class);
    }

    protected CommentDto createCommentForExistingPost(AddCommentRequest request) throws Exception {
        final var addCommentRequestJson = MAPPER.writeValueAsString(request);

        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(commentsByPostIdPath(request.getPostId()))
                                .content(addCommentRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, CommentDto.class);
    }

    protected CommentDto updateExistingCommentById(long postId,
                                                   long commentId,
                                                   CommentDto updatedComment) throws Exception {
        final var requestJson = MAPPER.writeValueAsString(updatedComment);

        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(commentsByIdPath(postId, commentId))
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, CommentDto.class);
    }

    protected void deleteExistingComment(long postId, long commentId) throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete(commentsByIdPath(postId, commentId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
    }

    protected String commentsByPostIdPath(long postId) {
        return String.format(Const.TEMPLATE, postByIdPath(postId), "/comments");
    }

    protected String commentsByIdPath(long postId, long commentId) {
        return String.format(Const.TEMPLATE, postByIdPath(postId), String.format(Const.TEMPLATE, "/comments", commentId));
    }
}
