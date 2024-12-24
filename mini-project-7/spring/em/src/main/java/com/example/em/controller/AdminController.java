package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.service.AdminService;
import com.example.em.service.EMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final EMService emService;
    private final RestTemplate restTemplate;
    private final AdminService adminService;

    @Value("${hospital.api.host}")
    private String hospitalApiHost;

    @GetMapping("/admin")
    public String adminPage(@Login Member loginmember, Model model, @RequestParam(name="page", defaultValue = "1") int page,
                            @RequestParam(name = "startDate", required = false) String startDateStr,
                            @RequestParam(name = "endDate", required = false) String endDateStr,
                            @RequestParam(name = "emClass", required = false) Integer emClass) {
        log.info("Accessing admin page");

        if(loginmember == null || !loginmember.isAdmin()) {
            model.addAttribute("message", "관리자가 아닙니다.");
            model.addAttribute("url", "/");
            return "alert/alert";
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDateTime.parse(startDateStr+":00", requestFormatter);
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDateTime.parse(endDateStr+":00", requestFormatter);
        }

        String url = hospitalApiHost + "/items";
        if (startDate != null && endDate != null && emClass != null) {
            url += "?startDate=" + startDate.format(requestFormatter) +
                    "&endDate=" + endDate.format(requestFormatter) +
                    "&emClass=" + emClass;
        }
        if (startDate != null && endDate != null && emClass == null) {
            url += "?startDate=" + startDate.format(requestFormatter) +
                    "&endDate=" + endDate.format(requestFormatter);
        }
        if (startDate == null && endDate == null && emClass != null) {
            url += "?emClass=" + emClass;
        }

        List<EMDto.HospitalLog> hospitalLogs = adminService.getLogs(url);

        Pageable pageable = PageRequest.of(page - 1, 5);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), hospitalLogs.size());
        List<EMDto.HospitalLog> pageContent = hospitalLogs.subList(start, end);

        Page<EMDto.HospitalLog> logs = new PageImpl<>(pageContent, pageable, hospitalLogs.size());

        model.addAttribute("logs", logs);
        model.addAttribute("prev", pageable.previousOrFirst().getPageNumber() + 1);
        model.addAttribute("next", pageable.next().getPageNumber() + 1);
        model.addAttribute("hasPrev", logs.hasPrevious());
        model.addAttribute("hasNext", logs.hasNext());

        model.addAttribute("startDate", startDateStr == null ? "" : startDateStr);
        model.addAttribute("endDate", endDateStr == null ? "" : endDateStr);
        model.addAttribute("emClass", emClass);

        int currentPage = logs.getNumber();
        int totalPages = logs.getTotalPages();
        List<Map<String, Object>> pageNumbers = IntStream.range(1, totalPages + 1)
                .mapToObj(i -> {
                    Map<String, Object> pageMap = new HashMap<>();
                    pageMap.put("number", i);
                    pageMap.put("isCurrentPage", i == page);
                    return pageMap;
                })
                .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", currentPage);

        return "layouts/adminlog";
    }

    @GetMapping("/admin/chart-data")
    public String adminChart(@Login Member loginmember, Model model,
                             @RequestParam(name = "startDate", required = false) String startDateStr,
                             @RequestParam(name = "endDate", required = false) String endDateStr,
                             @RequestParam(name = "emClass", required = false) Integer emClass) {

        log.info("Accessing admin chart page");

        if(loginmember == null || !loginmember.isAdmin()) {
            model.addAttribute("message", "관리자가 아닙니다.");
            model.addAttribute("url", "/");
            return "alert/alert";
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDateTime.parse(startDateStr+":00", requestFormatter);
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDateTime.parse(endDateStr+":00", requestFormatter);
        }

        String url = hospitalApiHost + "/items";
        if (startDate != null && endDate != null && emClass != null) {
            url += "?startDate=" + startDate.format(requestFormatter) +
                    "&endDate=" + endDate.format(requestFormatter) +
                    "&emClass=" + emClass;
        }
        if (startDate != null && endDate != null && emClass == null) {
            url += "?startDate=" + startDate.format(requestFormatter) +
                    "&endDate=" + endDate.format(requestFormatter);
        }
        if (startDate == null && endDate == null && emClass != null) {
            url += "?emClass=" + emClass;
        }

        List<EMDto.HospitalLog> hospitalLogs = adminService.getLogs(url);
        List<EMDto.LogChart> logs = adminService.getChartLogs(hospitalLogs);

        // JSON으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            String logsJson = objectMapper.writeValueAsString(logs);
            model.addAttribute("logs", logsJson);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("logs", "[]");
        }

        model.addAttribute("startDate", startDateStr == null ? "" : startDateStr);
        model.addAttribute("endDate", endDateStr == null ? "" : endDateStr);
        model.addAttribute("emClass", emClass);

        return "layouts/adminchart";
    }
}
