package com.example.em.service;

import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;

    public void save(EMDto.MemberDTO member) {
        Member saveM = new Member();
        saveM.setName(member.getName());
        saveM.setLoginId(member.getLoginId());
        saveM.setPassword(member.getPassword());
        repository.save(saveM);
    }

    public Optional<Member> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Member> findByLoginId(String loginId) {
        log.info("Find member by loginId: " + loginId);
        List<Member> all = findAll();
        for (Member m : all) {
            if (m.getLoginId().equals(loginId)) {
                log.info("Find");
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public boolean isLoginIdExists(String loginId) {
        return repository.findByLoginId(loginId).isPresent();
    }

    public List<Member> findAll() {
        return repository.findAll();
    }

    public Member delete(Member member) {
        repository.delete(member);
        return null;

    }

}
