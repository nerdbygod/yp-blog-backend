package org.practicum.yandex.persistence.comment;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.practicum.yandex.persistence.AbstractModel;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class CommentModel extends AbstractModel {
    @EqualsAndHashCode.Include
    private String content;

    @EqualsAndHashCode.Include
    private Long postId;
}
