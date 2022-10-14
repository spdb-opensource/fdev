package com.test.entity;


import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "role")
public class Role implements Serializable {
    @Id
    private String role_en_name;

    private String role_describe;

    private int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getRole_en_name() {
        return role_en_name;
    }

    public void setRole_en_name(String role_en_name) {
        this.role_en_name = role_en_name;
    }

    public String getRole_describe() {
        return role_describe;
    }

    public void setRole_describe(String role_describe) {
        this.role_describe = role_describe;
    }
}
