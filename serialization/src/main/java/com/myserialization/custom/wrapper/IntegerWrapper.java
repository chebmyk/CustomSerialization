package com.myserialization.custom.wrapper;

public class IntegerWrapper implements ObjectWrapper {
    private int value;

    public IntegerWrapper() {
    }

    @Override
    public Integer readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Integer)o;
        return this;
    }

}
