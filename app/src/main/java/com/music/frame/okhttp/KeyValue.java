package com.music.frame.okhttp;

/**
 * Created by jack
 */

public class KeyValue {

    private String key;
    private Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
