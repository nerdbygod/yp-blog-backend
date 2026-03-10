package org.practicum.yandex.service.validation;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class RequestValidator {
    public static boolean isValid(final CreatePostRequest request) {
        return StringUtils.isBlank(request.getText()) ||
                StringUtils.isBlank(request.getTitle()) ||
                CollectionUtils.isEmpty(request.getTags()) ||
                request.getTags().stream().noneMatch(StringUtils::isNotBlank);
    }

    public static boolean isInvalid(final CreatePostRequest postDto) {
        return !isValid(postDto);
    }
}
