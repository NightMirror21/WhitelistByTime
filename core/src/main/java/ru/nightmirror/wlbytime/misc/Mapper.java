package ru.nightmirror.wlbytime.misc;

public interface Mapper<T, E> {
    E toEntity(T table);

    T toTable(E entity);
}
