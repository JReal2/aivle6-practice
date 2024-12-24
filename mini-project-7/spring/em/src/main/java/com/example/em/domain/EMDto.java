package com.example.em.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class EMDto {
    @Data
    public static class PostInfo {
        private String status;
        private double lat;
        private double lon;
    }
    @Data
    public static class Info {
        private String datetime;
        private int em_class;
        private String hospital1;
        private String addr1;
        private String tel1;
        private String eta1;
        private double dist1;
        private int fee1;
        private String path1;
        private String hospital2;
        private String addr2;
        private String tel2;
        private String eta2;
        private double dist2;
        private int fee2;
        private String path2;
        private String hospital3;
        private String addr3;
        private String tel3;
        private String eta3;
        private double dist3;
        private int fee3;
        private String path3;
    }

    @Data
    @AllArgsConstructor
    public static class Hospital {
        private String hospital;
        private String addr;
        private String tel;
        private String eta;
        private double dist;
        private int fee;
        private String path;
    }


    @Data
    public static class LoginForm {
        @NotEmpty
        private String loginId;

        @NotEmpty
        private String password;
    }

    @Data
    public static class MemberDTO {
        @NotEmpty
        private String name;

        @NotEmpty
        private String loginId;

        @NotEmpty
        private String password;

        @NotEmpty
        private boolean admin;
    }

    @Data
    public static class Log {
        private LocalDateTime datetime;
        private String input_text;
        private String input_summary;
        private double input_latitude;
        private double input_longitude;
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

}
