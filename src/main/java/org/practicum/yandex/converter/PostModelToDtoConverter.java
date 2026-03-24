package org.practicum.yandex.converter;

import io.micrometer.common.lang.NonNull;
import org.practicum.yandex.controller.dto.PostDto;
import org.practicum.yandex.persistence.post.PostModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PostModelToDtoConverter implements Converter<PostModel, PostDto> {

    @Override
    public PostDto convert(final @NonNull PostModel source) {
        return PostDto.builder()
                .id(source.getId())
                .title(source.getTitle())
                .text(source.getContent())
                .tags(source.getTags())
                .commentsCount(source.getCommentCount())
                .likesCount(source.getLikeCount())
                .build();
    }

}
