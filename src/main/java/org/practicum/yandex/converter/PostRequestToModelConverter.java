package org.practicum.yandex.converter;

import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.request.CreatePostRequest;
import org.practicum.yandex.persistence.post.PostModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PostRequestToModelConverter implements Converter<CreatePostRequest, PostModel> {
    @Override
    public PostModel convert(@NonNull CreatePostRequest source) {
        return PostModel.builder()
                .title(StringUtils.trimToEmpty(source.getTitle()))
                .content(StringUtils.trimToEmpty(source.getText()))
                .build();
    }

}
