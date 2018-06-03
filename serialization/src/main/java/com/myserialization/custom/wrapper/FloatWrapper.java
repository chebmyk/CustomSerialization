package com.myserialization.custom.wrapper;

public class FloatWrapper<E> implements ObjectWrapper {

    private float value;

    public FloatWrapper() {
    }

    @Override
    public Float readObject() {
        return null;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Float)o;
        return this;
    }
}
