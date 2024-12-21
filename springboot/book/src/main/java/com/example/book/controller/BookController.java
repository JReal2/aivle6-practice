package com.example.book.controller;

import com.example.book.dto.BookDTO;
import com.example.book.entity.Book;
import com.example.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public List<BookDTO.Response> getBooks() {
        return bookService.findBooks();
    }

    @GetMapping("/{bookId}")
    public BookDTO.Response getBook(@PathVariable("bookId") Long bookId) {
        return bookService.findBook(bookId);
    }

    @PostMapping
    public Book insertBook(@RequestBody BookDTO.Post dto) {
        return bookService.insertBook(dto);
    }

    @PatchMapping("/{bookId}")
    public Book updateBookStatus(@PathVariable("bookId") Long bookId, @RequestBody BookDTO.Patch dto) {
        return bookService.updateBookStatus(bookId, dto);
    }

    @PutMapping("/{bookId}")
    public Book updateBook(@PathVariable("bookId") Long bookId, @RequestBody BookDTO.Put dto) {
        return bookService.updateBook(bookId, dto);
    }

    @DeleteMapping("/{bookId}")
    public void deleteBook(@PathVariable("bookId") Long bookId) {
        bookService.deleteBook(bookId);
    }
}
