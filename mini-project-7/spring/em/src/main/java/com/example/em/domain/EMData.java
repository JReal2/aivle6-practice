package com.example.em.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class EMData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String datetime;
    private int em_class;
    private String hospital1;
    private String addr1;
    private String tel1;
    private String eta1;
    private double dist1;
    private int fee1;
    private String hospital2;
    private String addr2;
    private String tel2;
    private String eta2;
    private double dist2;
    private int fee2;
    private String hospital3;
    private String addr3;
    private String tel3;
    private String eta3;
    private double dist3;
    private int fee3;
}
