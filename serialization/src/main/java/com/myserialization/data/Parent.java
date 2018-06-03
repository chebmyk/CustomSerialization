package com.myserialization.data;

import java.io.Serializable;
import java.util.*;

public class Parent implements Serializable {
    private int age;
    private Integer hieght = 177;
    private String name;
    private Gender gender;
    private List<Child> children = new ArrayList<>();
    private Parent[] friends;
    private String[][] hobby ={{"Fishing","ok"},{"Climbing","no"}};

    public Parent() {}

    public Parent(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public Parent(Gender gender, int age, String name) {
        this.age = age;
        this.name = name;
        this.gender = gender;
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

    public List getChildren() {
        return children;
    }

    public Parent[] getFriends() {
        return friends;
    }

    public void setFriends(Parent[] friends) {
        this.friends = friends;
    }

    public void addChild(Child child) {
        this.children.add(child);
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getHieght() {
        return hieght;
    }

    public void setHieght(Integer hieght) {
        this.hieght = hieght;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parent)) return false;
        Parent parent = (Parent) o;
        return age == parent.age &&
                Objects.equals(name, parent.name) &&
                Objects.equals(children, parent.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, name, children);
    }
}
