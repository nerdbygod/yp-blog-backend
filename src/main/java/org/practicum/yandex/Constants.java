package org.practicum.yandex;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public static final String PROPERTIES_PATH = "classpath:application.properties";
    public static final String ROOT_PACKAGE = "org.practicum.yandex";

    public static final String HASHTAG = "#";

    public static final class Controller {
        public static final String API_POSTS = "/api/posts";
        public static final String POST_ID = "postId";
    }
}
