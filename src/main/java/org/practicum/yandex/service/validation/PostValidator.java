package org.practicum.yandex.service.validation;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.PostDto;

@UtilityClass
public class PostValidator {
    public static boolean isValid(final PostDto postDto) {
        return StringUtils.isBlank(postDto.getText()) ||
                StringUtils.isBlank(postDto.getTitle());
    }

    public static boolean isInvalid(final PostDto postDto) {
        return !isValid(postDto);
    }
}
