package br.com.biblioteca.loan_service.DTO;

import java.time.LocalDate;

public record LoanResponseDTO(
        Long id,
        Long bookId,
        Long userId,
        LocalDate dataEmprestimo,
        LocalDate dataDevolucao,
        String status
) {
}

