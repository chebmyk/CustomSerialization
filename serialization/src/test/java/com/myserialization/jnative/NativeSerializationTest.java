package com.myserialization.jnative;


import com.myserialization.data.Parent;
import com.myserialization.data.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeSerializationTest {

    private Parent parent;
    private byte[] input = new byte[0];

    @BeforeEach
    void setUp() {
        parent = TestDataProvider.getTestData();
        input = NativeSerialization.writeObject(parent);
    }

    @Test
    void readObject() {
        Parent p2 = (Parent) NativeSerialization.readObject(input);
        assertEquals(p2.getAge(), parent.getAge());
        assertEquals(p2.getName(), parent.getName());
        assertArrayEquals(p2.getChildren().toArray(), parent.getChildren().toArray());
    }
}