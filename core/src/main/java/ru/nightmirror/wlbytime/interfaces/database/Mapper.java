package ru.nightmirror.wlbytime.interfaces.database;

public interface Mapper<T, E> {
    E toEntity(T table);

    T toTable(E entity);
}
