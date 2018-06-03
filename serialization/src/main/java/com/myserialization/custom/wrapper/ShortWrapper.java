package com.myserialization.custom.wrapper;

public class ShortWrapper implements ObjectWrapper {
    private short value;

    public ShortWrapper() {
    }

    @Override
    public Short readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Short)o;
        return this;
    }
}
