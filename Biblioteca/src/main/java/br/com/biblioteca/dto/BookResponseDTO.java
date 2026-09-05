package br.com.biblioteca.dto;

import br.com.biblioteca.model.Book;

public record BookResponseDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        String genero,
        String editora
) {}
