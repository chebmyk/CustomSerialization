package com.myserialization.custom.wrapper;

public class CharWrapper implements ObjectWrapper {
    private char value;

    public CharWrapper() {
    }

    @Override
    public Character readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Character)o;
        return this;
    }
}
