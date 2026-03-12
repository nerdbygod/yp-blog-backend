package org.practicum.yandex.repository;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class Page<T> {
    private Collection<T> data;
    private long count;
}
