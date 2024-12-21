package com.example.em.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "LOGIN_ID", nullable = false)
    private String loginId; //로그인 ID

    @Column(name = "NAME", nullable = false)
    private String name; //사용자 이름

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "ADMIN", nullable = false)
    private boolean admin;
}
