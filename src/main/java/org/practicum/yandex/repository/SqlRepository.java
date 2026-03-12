package org.practicum.yandex.repository;

import java.util.Optional;

public interface SqlRepository<ID, T> {
    Optional<T> findById(final ID id);

    T save(final T entity);

    int deleteById(final ID id);

    T update(ID id, final T updatedEntity);

    boolean existsById(final ID id);
}
