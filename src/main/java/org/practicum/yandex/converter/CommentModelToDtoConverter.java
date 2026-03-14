package org.practicum.yandex.converter;

import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.persistence.comment.CommentModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommentModelToDtoConverter implements Converter<CommentModel, CommentDto> {

    @Override
    public CommentDto convert(CommentModel source) {
        return CommentDto.builder()
                .id(source.getId())
                .postId(source.getPostId())
                .text(source.getContent())
                .build();
    }

}
