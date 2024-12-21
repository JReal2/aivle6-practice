package com.example.book.mapper;

import com.example.book.dto.BookDTO;
import com.example.book.entity.Book;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookResponseMapper {
    BookDTO.Response entityToResponse(Book book);
    List<BookDTO.Response> booksToResponses(List<Book> books);

    @AfterMapping
    default void titleAndSubTitle(Book book, @MappingTarget BookDTO.Response response) {
        response.setTitle(book.getTitle() + " - " + book.getSubTitle());
    }
}
