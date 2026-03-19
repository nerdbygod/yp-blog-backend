package org.practicum.yandex.storage;

import lombok.experimental.UtilityClass;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;

import java.util.Set;

@UtilityClass
public class TestData {
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
