package org.practicum.yandex.persistence.image;

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
public class ImageModel extends AbstractModel {
    private Long postId;
    private String fileName;
}
