package com.example.book.service;

import com.example.book.dto.BookDTO;
import com.example.book.entity.Book;
import com.example.book.mapper.BookControlMapper;
import com.example.book.mapper.BookResponseMapper;
import com.example.book.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final BookControlMapper controlMapper;
    private final BookResponseMapper responseMapper;

    public Book insertBook(BookDTO.Post dto) {
        Book book = controlMapper.PostDTOToEntity(dto);
        return saveBook(book);
    }

    public Book updateBook(Long id, BookDTO.Put dto) {
        Book book = findVerifiredBook(id);
        controlMapper.PutDTOToEntity(dto, book);
        return saveBook(book);
    }

    public Book updateBookStatus(Long id, BookDTO.Patch dto) {
        Book book = findVerifiredBook(id);
        controlMapper.PatchDTOToEntity(dto, book);
        return saveBook(book);
    }

    public void updateBookTitle(Book book, String title) {
        book.setTitle(title);
        saveBook(book);
    }

    public void updateBookPublisher(Book book, String publisher) {
        book.setPublisher(publisher);
        saveBook(book);
    }

    public Book updateBookTitleAndPublisher(Long id, BookDTO.Rollback dto) {
        Book book = findVerifiredBook(id);
        updateBookTitle(book, dto.getTitle());
        updateBookPublisher(book, dto.getPublisher());
        return book;
    }


    public void deleteBook(Long id) {
        Book book = findVerifiredBook(id);
        if(book.getStatus() == Book.Status.BORROWED) {
            throw new IllegalArgumentException("대출 중인 책은 삭제할 수 없습니다.");
        }
        bookRepository.delete(book);
    }

    public BookDTO.Response findBook(Long id) {
        Book book = findVerifiredBook(id);
        return responseMapper.entityToResponse(book);
    }

    public List<BookDTO.Response> findBooks() {
        List<Book> books = bookRepository.findAll();
        return responseMapper.booksToResponses(books);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Book findVerifiredBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다."));
    }
}

