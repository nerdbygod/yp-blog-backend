package org.practicum.yandex.converter;

import org.apache.commons.lang3.StringUtils;
import org.practicum.yandex.controller.dto.CommentDto;
import org.practicum.yandex.persistence.comment.CommentModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommentDtoToModelConverter implements Converter<CommentDto, CommentModel> {

    @Override
    public CommentModel convert(CommentDto source) {
        return CommentModel.builder()
                .id(source.getId())
                .postId(source.getPostId())
                .content(StringUtils.trim(source.getText()))
                .build();
    }

}
