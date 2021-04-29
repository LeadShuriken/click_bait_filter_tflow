package com.clickbait.tflow.utilities;

import java.util.Arrays;

class Array<E> {
    private final E[] arr;
    public final int length;

    public Array(Class<E> type, int length) {
        this.arr = (E[]) java.lang.reflect.Array.newInstance(type, length);
        this.length = length;
    }

    E get(int i) {
        return arr[i];
    }

    void set(int i, E e) {
        arr[i] = e;
    }

    @Override
    public String toString() {
        return Arrays.toString(arr);
    }
}