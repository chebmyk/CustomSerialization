package com.myserialization.custom.wrapper;

public class BooleanWrapper implements ObjectWrapper {
    private boolean value;

    public BooleanWrapper() {
    }

    @Override
    public Boolean readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Boolean) o;
        return this;
    }
}
