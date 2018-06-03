package com.myserialization.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class Child implements Serializable {
    private int age;
    private String name;
    private Child bestFriend;
    private Map<Gender,Child> friends;


    public Child() {
    }

    public Child(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Child getBestFriend() {
        return bestFriend;
    }

    public void setBestFriend(Child bestFriend) {
        this.bestFriend = bestFriend;
    }

    public Map<Gender, Child> getFriends() {
        return friends;
    }

    public void setFriends(Map<Gender, Child> friends) {
        this.friends = friends;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Child)) return false;
        Child child = (Child) o;
        return age == child.age &&
                Objects.equals(name, child.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, name);
    }
}
