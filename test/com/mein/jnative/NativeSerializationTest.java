package com.mein.jnative;

import com.mein.data.Child;
import com.mein.data.Parent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeSerializationTest {

    Parent parent1 = new Parent(30,"ParentName1");
    Parent parent2 = new Parent(35,"ParentName2");
    Child child = new Child(8,"ChildName");
    private byte[] input = new byte[0];

    @BeforeEach
    void setUp() {
        parent1.addChild(child);
        parent2.addChild(child);
        input = NativeSerialization.writeObject(parent1);
    }

    @Test
    void readObject() {
        Parent p2 = (Parent)NativeSerialization.readObject(input);
        assertEquals(p2.getAge(),parent1.getAge());
        assertEquals(p2.getName(),parent1.getName());
        assertArrayEquals(p2.getChildren().toArray(),parent1.getChildren().toArray());
    }
}