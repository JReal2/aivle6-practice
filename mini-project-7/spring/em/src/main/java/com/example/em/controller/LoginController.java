package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.repository.MemberRepository;
import com.example.em.service.LoginService;
import com.example.em.session.SessionConst;
import com.example.em.session.SessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final SessionManager sessionManager;

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
}
