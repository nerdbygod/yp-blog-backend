package org.practicum.yandex.storage;

import lombok.experimental.UtilityClass;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;

import java.util.Set;

@UtilityClass
public class TestData {
    private static final byte[] PNG_STUB = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_STUB = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    public static final byte[] ZIP_STUB = new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};

    public static CreatePostRequest getDefaultCreatePostRequest() {
        return CreatePostRequest.builder()
                .title("New Post")
                .text("Some text here")
                .tags(Set.of("Java", "Blog"))
                .build();
    }

    public static CreatePostRequest getCreatePostRequest(String title,
                                                         String text,
                                                         Set<String> tags) {
        return CreatePostRequest.builder()
                .title(title)
                .text(text)
                .tags(tags)
                .build();
    }

    public static AddCommentRequest getDefaultAddCommentRequest(Long postId) {
        return AddCommentRequest.builder()
                .postId(postId)
                .text("Default comment text")
                .build();
    }

    public static AddCommentRequest getAddCommentRequest(Long postId, String text) {
        return AddCommentRequest.builder()
                .postId(postId)
                .text(text)
                .build();
    }

    public static byte[] pngStub() {
        return PNG_STUB;
    }

    public static byte[] jpegStub() {
        return JPEG_STUB;
    }

    public static byte[] zipStub() {
        return ZIP_STUB;
    }

    public static final class Tags {
        public static final String HISTORY = "History";
        public static final String JAVA = "Java";
        public static final String MATHS = "Maths";
        public static final String PYTHON = "Python";
        public static final String GOLANG = "Golang";
        public static final String ECONOMICS = "Economics";
        public static final String PHYSICS = "Physics";
        public static final String MONEY = "Money";
        public static final String SPACE = "Space";
    }
}
