package br.com.biblioteca.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoanDetails {

    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;
}

