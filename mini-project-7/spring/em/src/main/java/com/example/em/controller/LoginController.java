package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMData;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.repository.EMRepository;
import com.example.em.repository.MemberRepository;
import com.example.em.service.EMService;
import com.example.em.service.LoginService;
import com.example.em.service.MemberService;
import com.example.em.session.SessionConst;
import com.example.em.session.SessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final SessionManager sessionManager;
    private final MemberService memberService;
    private final EMService emService;

    @GetMapping("/")
    public String homeLogin(@Login Member loginMember) {
        if (loginMember == null) {
            return "layouts/login";
        }

        return "redirect:/em";
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") EMDto.LoginForm form) {
        return "layouts/login";
    }

    @PostMapping("/login")
    public String loginV4(@Valid @ModelAttribute EMDto.LoginForm form, BindingResult bindingResult,
                          @RequestParam(defaultValue = "/") String redirectURL,Model model,
                          HttpServletRequest request) {

        log.info("Login form: " + form);

        if (bindingResult.hasErrors()) {
            return "layouts/login";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if (loginMember == null) {
            log.info("Login failed");
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            log.info("BindingResult has errors: {}", bindingResult.hasErrors());
            model.addAttribute("globalErrors", bindingResult.getAllErrors());
            return "layouts/login";
        }
        log.info("BindingResult has errors: {}", bindingResult.hasErrors());
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        if (loginMember.isAdmin()) {
            log.info("Admin login detected for user: {}", loginMember.getLoginId());
            return "redirect:/admin"; // 관리자 화면 경로
        }

        return "redirect:" + redirectURL;

    }

    @PostMapping("/logout")
    public String logoutV3(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        log.info("Logout1: " + session);
        if (session != null) {
            session.invalidate();
            log.info("Logout2: " + session);
        }
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signUp() {
        return "layouts/signUp";
    }

    @PostMapping("/signup")
    public String getInfo(EMDto.MemberDTO member, Model model, BindingResult bindingResult) {
        log.info("name :"+member.getName());
        log.info("id :"+member.getLoginId());
        log.info("password :"+member.getPassword());

        if(memberService.isLoginIdExists(member.getLoginId())) {
            bindingResult.reject("Duplicated loginId detected", "이미 존재하는 아이디입니다.");
            log.info("BindingResult has errors: {}", bindingResult.hasErrors());
            model.addAttribute("globalErrors", bindingResult.getAllErrors());
            return "layouts/signUp";
        }
        memberService.save(member);
        return "layouts/login";
    }
}
