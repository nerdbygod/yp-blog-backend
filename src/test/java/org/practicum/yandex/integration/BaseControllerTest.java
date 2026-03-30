package org.practicum.yandex.integration;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.practicum.yandex.Constants;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.controller.dto.response.GetPostListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MultiValueMap;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTest {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("blog")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    protected void cleanupDatabase() {
        jdbcTemplate.update("TRUNCATE TABLE blog.posts RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE blog.tags RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE blog.post_tags RESTART IDENTITY CASCADE");
    }

    protected PostDto createPost(CreatePostRequest request) throws Exception {
        final var createPostRequestJson = MAPPER.writeValueAsString(request);

        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(postsPath())
                                .content(createPostRequestJson)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, PostDto.class);
    }

    protected GetPostListResponse getPosts(int pageNumber,
                                           int pageSize,
                                           String search) throws Exception {

        final var params = prepareParams(pageNumber, pageSize, search);

        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(postsPath())
                                .params(params))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, GetPostListResponse.class);
    }

    protected PostDto getExistingPostById(long postId) throws Exception {
        final var response = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(postByIdPath(postId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return MAPPER.readValue(response, PostDto.class);
    }

    protected void deletePost(long postId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete(postByIdPath(postId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
    }

    protected static MultiValueMap<String, String> prepareParams(Integer pageNumber,
                                                                 Integer pageSize,
                                                                 String search) {
        final var params = new HashMap<String, String>();

        if (Objects.nonNull(pageNumber)) {
            params.put("pageNumber", pageNumber.toString());
        }

        if (Objects.nonNull(pageSize)) {
            params.put("pageSize", pageSize.toString());
        }

        if (StringUtils.isNotBlank(search)) {
            params.put("search", search);
        }

        return MultiValueMap.fromSingleValue(params);
    }

    protected String postsPath() {
        return Constants.Controller.API_POSTS;
    }

    protected String postByIdPath(long postId) {
        return String.format(Const.TEMPLATE, postsPath(), postId);
    }

    protected static final class Const {
        public static final String TEMPLATE = "%s/%s";
    }
}
