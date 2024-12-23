package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMData;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.repository.EMRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/em")
public class EMController {
    private final PostService postService;
    private final EMService emService;
    private final EMRepository emRepository;

    @Value("${hospital.api.host}")
    private String hospitalApiHost;

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
        String url = hospitalApiHost + "/items/text"; // 대상 서버의 URL
        String send = postService.sendPostRequest(url, info);
        Gson gson = new Gson();
        EMData emData = gson.fromJson(send, EMData.class);
        System.out.println(emData);
        emRepository.save(emData);
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

    @PostMapping("/detail")
    public String getDetail(@Login Member loginmember, @ModelAttribute EMDto.Hospital hospital, Model model) {
        if (loginmember == null) {
            return "layouts/login";
        }
        System.out.println(hospital.getPath());
        model.addAttribute("hospitalInfo", hospital);

        return "layouts/detail";
    }
}
