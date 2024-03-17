package com.example.universitymarket.utilities;

public interface Callback<T> {
    void onSuccess(final T result);
    void onFailure(Exception error);
}
