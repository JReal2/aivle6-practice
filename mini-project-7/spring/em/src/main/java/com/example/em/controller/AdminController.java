package com.example.em.controller;

import com.example.em.domain.EMDto;
import com.example.em.service.EMService;
import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final EMService emService;
    private final RestTemplate restTemplate;

    @GetMapping("/admin")
    public String adminPage(Model model, @RequestParam(name="page", defaultValue = "1") int page,
                            @RequestParam(name = "startDate", required = false) String startDateStr,
                            @RequestParam(name = "endDate", required = false) String endDateStr,
                            @RequestParam(name = "emClass", required = false) Integer emClass) {
        Pageable pageable = PageRequest.of(page - 1, 5);
        log.info("Accessing admin page");

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDateTime.parse(startDateStr+":00", requestFormatter);
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDateTime.parse(endDateStr+":00", requestFormatter);
        }

        String url = "http://127.0.0.1:8000/items";
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

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    String datetimeString = json.getAsString();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return LocalDateTime.parse(datetimeString, formatter);
                })
                .create();

        EMDto.Log[] emLogsArray = gson.fromJson(response.getBody(), EMDto.Log[].class);
        List<EMDto.Log> emLogsList = Arrays.asList(emLogsArray);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), emLogsList.size());
        List<EMDto.Log> pageContent = emLogsList.subList(start, end);

        Page<EMDto.Log> logs = new PageImpl<>(pageContent, pageable, emLogsList.size());

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

}
