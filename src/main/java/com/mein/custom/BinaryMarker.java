package com.mein.custom;

public enum BinaryMarker {
    MYPROTOCOL((byte)0x77),
    CLASS_START((byte)0x10),
    CLASS_END((byte)0x11),
    FIELD_START((byte)0x2),
    FIELD_END((byte)0x22),
    TYPE_PRIMITIVE((byte)0x3),
    TYPE_CLASS((byte)0x4),
    TYPE_ARRAY((byte)0x5),
    TYPE_ENUM((byte)0x8),
    TYPE_NULL((byte)0x7);

    public byte code;
    BinaryMarker(byte code){
        this.code=code;
    }
}
