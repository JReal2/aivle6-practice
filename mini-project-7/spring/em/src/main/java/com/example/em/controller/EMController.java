package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.service.EMService;
import com.example.em.service.MemberService;
import com.example.em.service.PostService;
import com.example.em.session.SessionConst;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/em")
public class EMController {
    private final PostService postService;
    private final EMService emService;

    @GetMapping()
    public String home(@Login Member loginmember, Model model) {

        if (loginmember == null) {
            return "layouts/login";
        }
        log.info("로그인 멤버: "+loginmember.getName());
        model.addAttribute("member", loginmember);
        return "layouts/home";
    }

    @PostMapping("/info")
    public String getInfo(@Login Member loginmember,EMDto.PostInfo info, Model model) {
        if (loginmember == null) {
            return "layouts/login";
        }
        String url = "http://127.0.0.1:8000/items/text"; // 대상 서버의 URL
        String send = postService.sendPostRequest(url, info);
        Gson gson = new Gson();
        EMDto.Info data = gson.fromJson(send, EMDto.Info.class);
        System.out.println(data);
        List<EMDto.Hospital> hospitalList = emService.transformData(data);
        if(!hospitalList.isEmpty()) {
            model.addAttribute("emergency", true);
            model.addAttribute("data", hospitalList);
        }
        model.addAttribute("member", loginmember);
        return "layouts/result";
    }

}
