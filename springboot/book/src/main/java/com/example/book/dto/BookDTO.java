package com.example.book.dto;

import com.example.book.entity.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BookDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {
        @NotBlank(message="제목은 필수 입력 값입니다.")
        @NotNull(message="제목은 필수 입력 값입니다.")
        @Size(min=1, max=45, message="제목은 45자 이하여야합니다.")
        private String title;

        @NotBlank
        @NotNull
        @Size(max=45)
        private String subTitle;

        @Size(max=45)
        private String author;

        @Size(max=45)
        private String publisher;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Put {
        @NotBlank(message="제목은 필수 입력 값입니다.")
        @NotNull(message="제목은 필수 입력 값입니다.")
        @Size(min=1, max=45, message="제목은 45자 이하여야합니다.")
        private String title;

        @NotBlank
        @NotNull
        @Size(max=45)
        private String subTitle;

        @Size(max=45)
        private String author;

        @Size(max=45)
        private String publisher;

        private Book.Status status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch {
        private Book.Status status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rollback {
        @NotBlank(message="제목은 필수 입력 값입니다.")
        @NotNull(message="제목은 필수 입력 값입니다.")
        @Size(min=1, max=45, message="제목은 45자 이하여야합니다.")
        private String title;

        @NotBlank
        @NotNull
        @Size(max=45)
        private String subTitle;

        @Size(max=45)
        private String publisher;
    }

    // 조회(GET)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String subTitle;
        private String publisher;
        private Book.Status status;
    }
}
