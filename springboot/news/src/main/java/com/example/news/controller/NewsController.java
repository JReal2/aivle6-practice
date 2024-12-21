package com.example.news.controller;

import com.example.news.domain.News;
import com.example.news.dto.NewsDto;
import com.example.news.mapper.NewsMapper;
import com.example.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    @GetMapping("/create")
    public String newArticleForm() {
        return "news/create";
    }

    @PostMapping("/create")
    public String createNews(NewsDto.Post post) {
        News news = newsMapper.newsPostDtoToNews(post);
        newsRepository.save(news);

        return "redirect:/news/" + news.getNewsId();
    }

    @GetMapping("/{newsId}")
    public String getNews(@PathVariable Long newsId, Model model) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 뉴스가 존재하지 않습니다."));

        model.addAttribute("news", news);

        return "news/detail";
    }

    @GetMapping("/list")
    public String getNewsList(Model model, @RequestParam(name="page", defaultValue = "1") int page) {
        Pageable pageable = PageRequest.of(page - 1, 7);
        Page<News> newsPage = newsRepository.findAll(pageable);

        model.addAttribute("newsPage", newsPage);
        model.addAttribute("prev", pageable.previousOrFirst().getPageNumber() + 1);
        model.addAttribute("next", pageable.next().getPageNumber() + 1);
        model.addAttribute("hasNext", newsPage.hasNext());
        model.addAttribute("hasPrev", newsPage.hasPrevious());

        int currentPage = newsPage.getNumber();
        int totalPages = newsPage.getTotalPages();
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

        return "news/list";
    }

    @GetMapping("/{newsId}/delete")
    public String deleteNews(@PathVariable("newsId") Long newsId) {
        newsRepository.deleteById(newsId);

        return "redirect:/news/list";
    }

    @GetMapping("/{newsId}/edit")
    public String editNewsForm(@PathVariable("newsId") Long newsId, Model model) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 뉴스가 존재하지 않습니다."));

        model.addAttribute("news", news);

        return "news/edit";
    }

    @PostMapping("/{newsId}/update")
    public String editNews(@PathVariable("newsId") Long newsId, NewsDto.Patch patch) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 뉴스가 존재하지 않습니다."));

        newsMapper.patchDtoToNews(patch, news);
        newsRepository.save(news);

        return "redirect:/news/" + news.getNewsId();
    }
}
