package br.com.biblioteca.model.vo;

import br.com.biblioteca.model.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Embeddable
@Getter
public class LoanDetails {


    @NotNull(message = "Data de empréstimo obrigatória")
    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @NotNull(message = "Data de devolução obrigatória")
    @Column(nullable = false)
    private LocalDate dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Column
    private LoanStatus status;

    public LoanDetails() {
        this.dataEmprestimo = LocalDate.now();
        this.dataDevolucao = LocalDate.now().plusDays(30);
        this.status = LoanStatus.ATIVO;

    }
    public LoanDetails(LocalDate dataEmprestimo, LocalDate dataDevolucao, LoanStatus status) {
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
        this.status = status;
    }
}

