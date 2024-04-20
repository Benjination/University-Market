package com.example.universitymarket.utilities;

public interface Listener<T> {
    void onAdded(T added);
    void onModified(T modified);
    void onRemoved(T removed);
    void onFailure(Exception error);
}
