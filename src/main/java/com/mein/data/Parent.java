package com.mein.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Parent implements Serializable {
    private int age;
    String name;
    private Set<Child> children = new HashSet<Child>();
    private int[] f_array = new int[2];

    public Parent() {}

    public Parent(int age, String name) {
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

    public Set getChildren() {
        return children;
    }

    public void addChild(Child child) {
        this.children.add(child);
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
