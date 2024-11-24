package com.intelligence.commonlib.notification;

public interface Subscriber<T> {
    void onEvent(T t);
}
