package org.practicum.yandex.persistence.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.practicum.yandex.persistence.AbstractModel;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class PostModel extends AbstractModel {
    @EqualsAndHashCode.Include
    private String title;

    @EqualsAndHashCode.Include
    private String content;
}
