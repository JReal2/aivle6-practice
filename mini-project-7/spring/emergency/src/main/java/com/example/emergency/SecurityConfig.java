package com.example.emergency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                            .requestMatchers("/admin").hasRole("ADMIN") // ROLE_ADMIN만 접근 가능
                            .requestMatchers("/", "/login", "/public/**").permitAll() // 공용 경로는 누구나 접근 가능
                            .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin((formLogin) ->
                        formLogin
                                .loginPage("/login") // 사용자 정의 로그인 페이지
                                .defaultSuccessUrl("/", true) // 로그인 성공 시 리다이렉트
                                .permitAll()
                )
                .logout((logoutConfig) ->
                        logoutConfig
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login")
                            .permitAll()
                );

        return http.build();
    }
}
