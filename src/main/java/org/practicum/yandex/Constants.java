package org.practicum.yandex;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class Constants {
    public static final String PROPERTIES_PATH = "classpath:application.properties";
    public static final String ROOT_PACKAGE = "org.practicum.yandex";

    public static final String HASHTAG = "#";

    public static final Set<String> IGNORABLE_POST_ID_VALUES = Set.of("undefined", "null");

    public static final class Controller {
        public static final String API_POSTS = "/api/posts";
        public static final String POST_ID = "postId";
        public static final String IMAGE_PARAM = "image";
    }
}
