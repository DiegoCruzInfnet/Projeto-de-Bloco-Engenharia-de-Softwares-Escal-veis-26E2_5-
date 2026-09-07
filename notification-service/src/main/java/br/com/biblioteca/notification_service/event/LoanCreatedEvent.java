package br.com.biblioteca.notification_service.event;

import java.time.LocalDate;

public class LoanCreatedEvent {

    private Long loanId;
    private Long bookId;
    private Long userId;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;

    public LoanCreatedEvent() {}

    public Long getLoanId() { return loanId; }
    public Long getBookId() { return bookId; }
    public Long getUserId() { return userId; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }

    public LoanCreatedEvent(Long loanId, Long bookId, Long userId,
                            LocalDate dataEmprestimo, LocalDate dataDevolucao) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.userId = userId;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
    }
}