package com.example.em.service;

import com.example.em.domain.Member;
import com.example.em.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberService service;

    /**
     * @return null 로그인 실패
     */
    public Member login(String loginId, String password) {
        return service.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }
}