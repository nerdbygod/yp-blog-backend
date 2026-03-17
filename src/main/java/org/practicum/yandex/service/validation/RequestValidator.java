package org.practicum.yandex.service.validation;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.controller.dto.request.AddCommentRequest;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;

import java.util.Objects;

@UtilityClass
public class RequestValidator {
    public static boolean isInvalid(final CreatePostRequest request) {
        return StringUtils.isBlank(request.getText()) ||
                StringUtils.isBlank(request.getTitle());
    }

    public static boolean isInvalid(final AddCommentRequest request) {
        return Objects.isNull(request.getPostId()) ||
                StringUtils.isBlank(request.getText());
    }

    public static boolean isInvalid(final CommentDto commentDto) {
        return StringUtils.isBlank(commentDto.getText()) ||
                ObjectUtils.anyNull(commentDto.getId(), commentDto.getPostId());
    }
}
