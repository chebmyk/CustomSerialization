package com.mein.custom;

import com.mein.data.Child;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.mein.data.Parent;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MetaDataLoaderTest {

    private Parent parent1 = new Parent(30,"ParentName1");
    private Child child = new Child(8,"ChildName");
    private Child child2 = new Child(9,"ChildName2");


    @BeforeEach
    void setUp() {
        child.setBestFriend(child2);
        parent1.addChild(child);
        parent1.addChild(null);
    }

    @Test
    void testSerialize() {
        System.out.println(Arrays.toString(MetaDataLoader.serialize(parent1)));
    }

    @Test
    void testDeSerialized() {
        Parent p2 =(Parent)MetaDataLoader.deSerialized(MetaDataLoader.serialize(parent1));
        assertEquals(p2.getAge(),parent1.getAge());
        assertEquals(p2.getName(),parent1.getName());
        assertArrayEquals(p2.getChildren().toArray(),parent1.getChildren().toArray());
    }
}