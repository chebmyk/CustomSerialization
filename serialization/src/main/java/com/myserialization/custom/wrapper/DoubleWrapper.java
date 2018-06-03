package com.myserialization.custom.wrapper;

public class DoubleWrapper implements ObjectWrapper {
    private double value;

    public DoubleWrapper() {
    }

    @Override
    public Double readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Double)o;
        return this;
    }
}
