package org.practicum.yandex.integration;

import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.practicum.yandex.Constants;
import org.practicum.yandex.repository.ImageRepository;
import org.practicum.yandex.service.image.ImageService;
import org.practicum.yandex.service.image.impl.ImageServiceImpl;
import org.practicum.yandex.storage.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ReflectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageControllerIntegrationTest extends BaseControllerTest {
    @TempDir(cleanup = CleanupMode.ALWAYS)
    private static Path TEMP_UPLOAD_DIR = Paths.get("temp/uploads/image/");

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    @DynamicPropertySource
    static void overrideUploadDirProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.servlet.multipart.location", TEMP_UPLOAD_DIR::toString);
    }

    @Test
    void testValidPngImageUploadToExistingPost_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var pngStub = TestData.pngStub();
        final var image = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "avatar.png", "image/png", pngStub);

        uploadImageToExistingPost(post.getId(), image);

        final var downloadedImage = downloadImageForExistingPost(post.getId());

        assertThat(downloadedImage).isEqualTo(pngStub);
    }

    @Test
    void testValidJpegImageUploadToExistingPost_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var jpegStub = TestData.jpegStub();
        final var image = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "avatar.jpeg", "image/jpeg", jpegStub);

        uploadImageToExistingPost(post.getId(), image);

        final var downloadedImage = downloadImageForExistingPost(post.getId());

        assertThat(downloadedImage).isEqualTo(jpegStub);
    }

    @Test
    void testInvalidImageExtensionAndContentTypeUpload_isFailed() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var zipStub = TestData.zipStub();
        final var image = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "maliciousImage.jpeg", "image/jpeg", zipStub);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(HttpMethod.PUT, imagePath(post.getId()))
                                .file(image))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.containsString("Unknown image format")));
    }

    @Test
    void testUploadImageWithInvalidNamePart_isFailed() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var jpegStub = TestData.jpegStub();
        final var image = new MockMultipartFile("unknownName", "avatar.jpeg", "image/jpeg", jpegStub);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(HttpMethod.PUT, imagePath(post.getId()))
                                .file(image))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testReplaceImageForExistingPost_isSuccessful() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        // 1. Verify initial image is uploaded successfully
        final var initialJpegStub = ArrayUtils.addAll(TestData.jpegStub(), (byte) 0x01, (byte) 0x02, (byte) 0x03);
        final var initialImage = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "avatar.jpeg", "image/jpeg", initialJpegStub);

        uploadImageToExistingPost(post.getId(), initialImage);

        final var initialDownloadedImage = downloadImageForExistingPost(post.getId());

        assertThat(initialDownloadedImage).isEqualTo(initialJpegStub);

        final var initialImageFileName = imageRepository.findByPostId(post.getId()).orElseThrow().getFileName();

        // 2. Verify updated image is uploaded successfully
        final var updatedJpegStub = ArrayUtils.addAll(TestData.jpegStub(), (byte) 0x05, (byte) 0x06, (byte) 0x07);
        final var updatedImage = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "updated_avatar.jpeg", "image/jpeg", updatedJpegStub);

        uploadImageToExistingPost(post.getId(), updatedImage);

        final var updatedDownloadedImage = downloadImageForExistingPost(post.getId());

        assertThat(updatedDownloadedImage).isEqualTo(updatedJpegStub);

        var updatedImageFilename = imageRepository.findByPostId(post.getId()).orElseThrow().getFileName();

        // 3. Verify that old image is deleted
        assertThat(updatedImageFilename).isNotEqualTo(initialImageFileName);
        assertFileIsDeleted(initialImageFileName);
    }

    @Test
    void testDownloadImageForNonExistingPost_isFailed() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get(imagePath(100500)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testDownloadImageForExistingPost_withoutImage_returnsNoContent() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        mockMvc.perform(
                        MockMvcRequestBuilders.get(imagePath(post.getId())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testDownloadImageForExistingPost_afterFileWasDeleted_returnsNoContent() throws Exception {
        final var post = createPost(TestData.getDefaultCreatePostRequest());

        final var pngStub = TestData.pngStub();
        final var image = new MockMultipartFile(Constants.Controller.IMAGE_PARAM, "avatar.png", "image/png", pngStub);

        uploadImageToExistingPost(post.getId(), image);
        final var imageFileName = imageRepository.findByPostId(post.getId()).orElseThrow().getFileName();

        deleteFile(imageFileName);
        assertFileIsDeleted(imageFileName);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(imagePath(post.getId())))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    protected void uploadImageToExistingPost(Long postId, MockMultipartFile image) throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(HttpMethod.PUT, imagePath(postId))
                                .file(image))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    protected byte[] downloadImageForExistingPost(Long postId) throws Exception {
        return mockMvc.perform(
                        MockMvcRequestBuilders.get(imagePath(postId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
    }

    private void deleteFile(String fileName) {
        final var tryDeleteImageMethod = ReflectionUtils.findMethod(
                ImageServiceImpl.class, "tryDeleteImage", String.class
        );

        ReflectionUtils.makeAccessible(tryDeleteImageMethod);
        ReflectionUtils.invokeMethod(tryDeleteImageMethod, imageService, fileName);
    }

    private String imagePath(long postId) {
        return String.format(Const.TEMPLATE, postByIdPath(postId), Constants.Controller.IMAGE_PARAM);
    }

    private static void assertFileIsDeleted(String fileName) {
        final var filePath = TEMP_UPLOAD_DIR.resolve(fileName).normalize();

        assertThat(filePath).doesNotExist();
    }
}
