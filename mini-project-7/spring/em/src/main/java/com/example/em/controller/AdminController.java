package com.example.em.controller;

import com.example.em.domain.EMData;
import com.example.em.service.EMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/admin")
    public String adminPage(Model model, @RequestParam(name="page", defaultValue = "1") int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);
        log.info("Accessing admin page");

        Page<EMData> logs = emService.getLogList(pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("prev", pageable.previousOrFirst().getPageNumber() + 1);
        model.addAttribute("next", pageable.next().getPageNumber() + 1);
        model.addAttribute("hasPrev", logs.hasPrevious());
        model.addAttribute("hasNext", logs.hasNext());

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
