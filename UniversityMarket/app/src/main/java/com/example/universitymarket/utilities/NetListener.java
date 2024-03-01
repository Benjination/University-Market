package com.example.universitymarket.utilities;

public interface NetListener<T> {
    void onSuccess(final T result);
    void onFailure(Exception error);
}
