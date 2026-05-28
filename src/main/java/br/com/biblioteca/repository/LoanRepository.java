package br.com.biblioteca.repository;

import br.com.biblioteca.model.Book;
import br.com.biblioteca.model.Loan;
import br.com.biblioteca.model.LoanStatus;
import br.com.biblioteca.model.User;
import br.com.biblioteca.model.vo.LoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByDetailsDataEmprestimo(LocalDate  dataEmprestimo);
    List<Loan> findByDetailsDataDevolucao(LocalDate dataDevolucao);
    List<Loan> findByUser(User user);
    List<Loan> findByDetailsDataDevolucaoLessThan(LocalDate data);
    // Conta quantos empréstimos ativos um usuário tem
    //long countByUserAndDetailsDataDevolucaoGreaterThanEqual(User user, LocalDate data);
    long countByUserAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(User user, LoanStatus status, LocalDate data);
    // Verifica se o livro está emprestado
    boolean existsByBookAndDetailsDataDevolucaoGreaterThanEqual(Book book, LocalDate data);
}
