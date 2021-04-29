package com.clickbait.tflow.interfaces;

public interface Classifier<I, O> {
    O classify(I output);

    void close();
}