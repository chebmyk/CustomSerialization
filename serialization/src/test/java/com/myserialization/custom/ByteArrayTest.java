package com.myserialization.custom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class ByteArrayTest {
    private byte i =(byte) 1;
    private ByteArray ba = new ByteArray();;

    @BeforeEach
    void setUp() {}

    @Test
    void testInsert() {
        assertArrayEquals(ba.getByteArray(),new byte[0]);
        ba.add(i);
        assertEquals(ba.getByteArray().length,1);
        byte[] b2 =new byte[]{i,i,i,i,i,i,i};
        ba.addAll(b2);
        assertEquals(ba.getByteArray().length,8);
        System.out.println(Arrays.toString(ba.getByteArray()));;
    }

    @Test
    void testCovertString() {
        String s ="com.mein.data.Parent";
        byte[] sa = ByteArrayUtils.toByta(s);
        String s2 = ByteArrayUtils.toString(sa);
        assertEquals(s,s2);
        System.out.println("s=" + s + " s2="+s2);
        System.out.println(Arrays.toString(sa));;
    }

}