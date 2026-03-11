package org.practicum.yandex.repository;

import java.util.List;
import java.util.Optional;

public interface SqlRepository<ID, T> {
    Optional<T> findById(final ID id);

    List<T> findAll();

    T save(final T entity);

    int deleteById(final ID id);
}
