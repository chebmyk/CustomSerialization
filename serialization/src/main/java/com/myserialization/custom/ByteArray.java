package com.myserialization.custom;

public class ByteArray {
    private byte[] byteArray;
    private int pos = 0;

    public ByteArray() {
        this.byteArray = new byte[64];
    }

    public ByteArray(int capacity) {
        this.byteArray = new byte[capacity];
    }

    public void add(byte val) {
        if (pos >= byteArray.length) {
            byte[] newarray = new byte[(pos + 1) * 5 / 4];
            System.arraycopy(byteArray, 0, newarray, 0, byteArray.length);
            byteArray = newarray;
        }
        byteArray[pos] = val;
        pos++;
    }

    public void addAll(byte[] val) {
        if (val.length > 0) {
            int i = 0;
            while (i < val.length) {
                add(val[i]);
                i++;
            }
        }
    }

    public byte[] getByteArray() {
        if (byteArray.length > pos) {
            byte[] newarray = new byte[pos];
            System.arraycopy(byteArray, 0, newarray, 0, pos);
            byteArray = newarray;
        }
        return byteArray;
    }


}
