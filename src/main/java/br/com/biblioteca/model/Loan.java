package br.com.biblioteca.model;

import br.com.biblioteca.model.vo.LoanDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
public class Loan extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private LoanDetails details = new LoanDetails();

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Loan() {
        this.details = new LoanDetails();
    }

    public Loan(LoanDetails details) {
        this.details = details;
    }

    public Loan(Book book, User user) {
        this.details = new LoanDetails();
        this.book = book;
        this.user = user;
    }

    public void devolver() {
        this.details = new LoanDetails(
                details.getDataEmprestimo(),
                details.getDataDevolucao(),
                LoanStatus.DEVOLVIDO
        );
    }

}
