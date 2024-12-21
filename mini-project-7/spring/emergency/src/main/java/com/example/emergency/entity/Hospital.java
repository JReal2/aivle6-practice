package com.example.emergency.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Hospital {
    @JsonProperty("hospital_name")
    private String hospital_name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("eta")
    private String eta;

    @JsonProperty("distance")
    private double distance;

    @JsonProperty("fee")
    private int fee;

    @Override
    public String toString() {
        return "Hospital{" +
                "hospitalName='" + hospital_name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", eta='" + eta + '\'' +
                ", distance=" + distance +
                ", fee=" + fee +
                '}';
    }
}
