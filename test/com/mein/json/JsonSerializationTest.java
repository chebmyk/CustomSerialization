package com.mein.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mein.data.Child;
import com.mein.data.Parent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializationTest {

    private Parent parent1 = new Parent(30,"ParentName1");
    private Child child = new Child(8,"ChildName");
    private Child child2 = new Child(9,"ChildName2");
    private byte[] input = new byte[0];

    @BeforeEach
    void setUp() {
        child.setBestFriend(child2);
        parent1.addChild(child);
        parent1.addChild(null);
        try {
            input = JsonSerialization.writeJsonObject(parent1);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readJsonObjectEquals() {
        Parent p2 = (Parent)JsonSerialization.readJsonObject(input,Parent.class);
        assertEquals(p2.getAge(),parent1.getAge());
        assertEquals(p2.getName(),parent1.getName());
        assertArrayEquals(p2.getChildren().toArray(),parent1.getChildren().toArray());
    }

}