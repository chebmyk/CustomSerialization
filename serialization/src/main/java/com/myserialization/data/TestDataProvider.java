package com.myserialization.data;

import java.util.HashMap;
import java.util.Map;

public class TestDataProvider {

    public static Parent getTestData() {
        Parent parent1 = new Parent(Gender.MALE, 30, "ParentName1");
        Parent parent2 = new Parent(Gender.FEMALE, 28, "ParentName2");
        Child child = new Child(8, "ChildName");
        Child child2 = new Child(9, "ChildName2");
        Child child3 = new Child(8, "ChildName3");

        child.setBestFriend(child2);
        Map<Gender, Child> friends = new HashMap<>();
        friends.put(Gender.FEMALE, child3);
        child.setFriends(friends);
        parent1.addChild(child);
        parent1.addChild(null);
        parent1.setFriends(new Parent[]{parent2});

        return parent1;
    }
}
