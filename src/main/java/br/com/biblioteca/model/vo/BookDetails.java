package br.com.biblioteca.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookDetails {
    private String titulo;
    private String autor;
    private String isbn;
    private String genero;
    private String editora;
}
