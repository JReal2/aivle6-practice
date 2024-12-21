package com.example.emergency.controller;

import com.example.emergency.entity.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class HospitalController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("")
    public String emergencyInput() {
        return "emergency";
    }

    @GetMapping("/hospitals")
    public String getHospitalList(
            @RequestParam String input_text,
            @RequestParam double latitude,
            @RequestParam double longitude,
            Model model
    ) throws UnsupportedEncodingException {
        String decodedText = StringEscapeUtils.unescapeHtml4(input_text);
        String urlEncodedText = URLEncoder.encode(decodedText, StandardCharsets.UTF_8);

        String url = "http://localhost:8000/hospital_by_module?input_text=" + urlEncodedText +
                "&latitude=" + latitude + "&longitude=" + longitude;

        List<Hospital> hospitals = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Hospital>>() {}
        ).getBody();

        model.addAttribute("hospitals", hospitals);

        return "hospital_list";
    }
}
