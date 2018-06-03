package com.myserialization.custom.wrapper;

public class LongWrapper implements ObjectWrapper {

    private long value;

    public LongWrapper() {
    }

    @Override
    public Long readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Long)o;
        return this;
    }
}
