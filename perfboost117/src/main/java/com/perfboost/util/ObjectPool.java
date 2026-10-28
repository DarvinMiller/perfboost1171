package com.perfboost.util;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Простой пул объектов (не потокобезопасный: использовать в пределах одного потока).
 * acquire() берёт готовый объект или создаёт новый, release() сбрасывает поля и возвращает в пул.
 */
public final class ObjectPool<T> {
    private final ArrayDeque<T> free = new ArrayDeque<>();
    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private final int maxSize;

    public ObjectPool(Supplier<T> factory, Consumer<T> reset, int maxSize) {
        this.factory = factory;
        this.reset = reset;
        this.maxSize = maxSize;
    }

    public T acquire() {
        T obj = free.pollLast();
        return obj != null ? obj : factory.get();
    }

    public void release(T obj) {
        reset.accept(obj);
        if (free.size() < maxSize) free.addLast(obj);
    }

    public int idleCount() {
        return free.size();
    }
}
