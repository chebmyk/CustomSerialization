package com.myserialization.custom.wrapper;

public class ByteWrapper implements ObjectWrapper{
    private byte value;

    public ByteWrapper() {
    }

    @Override
    public Byte readObject() {
        return value;
    }

    @Override
    public ObjectWrapper writeObject(Object o) {
        this.value = (Byte) o;
        return this;
    }
}
