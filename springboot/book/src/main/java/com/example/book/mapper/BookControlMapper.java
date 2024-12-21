package com.example.book.mapper;

import com.example.book.dto.BookDTO;
import com.example.book.entity.Book;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookControlMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Book PostDTOToEntity(BookDTO.Post post);

    @Mapping(target = "id", ignore = true)
    void PutDTOToEntity(BookDTO.Put put, @MappingTarget Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "subTitle", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    void PatchDTOToEntity(BookDTO.Patch patch, @MappingTarget Book book);
}
