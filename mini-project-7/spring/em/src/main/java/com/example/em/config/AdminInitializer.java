package com.example.em.config;

import com.example.em.domain.Member;
import com.example.em.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInitializer {
    @Bean
    public CommandLineRunner initData(MemberRepository repository) {
        return args -> {
            // 관리자 계정이 없다면 추가
            if (repository.findByLoginId("admin").isEmpty()) {
                Member admin = new Member();
                admin.setLoginId("admin");
                admin.setName("Administrator");
                admin.setPassword("admin"); // 비밀번호 암호화
                admin.setAdmin(true); // admin 필드를 true로 설정
                repository.save(admin);
            }
        };
    }
}

