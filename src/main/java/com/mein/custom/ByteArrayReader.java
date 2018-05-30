package com.mein.custom;

import java.util.Arrays;

public class ByteArrayReader {

    private byte[] byteArray;
    private int pos = 0;

    public ByteArrayReader(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public byte[] getSubArrayFromCurrent(int count) {
        byte[] result = Arrays.copyOfRange(byteArray, pos, pos + count);
        pos += count;
        return result;
    }

    public byte current() {
        return byteArray[pos];
    }

    public boolean hasNext() {
        return pos < byteArray.length;
    }


    public byte next() {
        return byteArray[pos++];
    }
}
