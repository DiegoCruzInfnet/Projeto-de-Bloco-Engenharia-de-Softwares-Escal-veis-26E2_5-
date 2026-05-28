package br.com.biblioteca;

import br.com.biblioteca.model.Book;
import br.com.biblioteca.model.Loan;
import br.com.biblioteca.model.LoanStatus;
import br.com.biblioteca.model.User;
import br.com.biblioteca.model.vo.BookDetails;
import br.com.biblioteca.model.vo.LoanDetails;
import br.com.biblioteca.model.vo.UserDetails;
import br.com.biblioteca.repository.BookRepository;
import br.com.biblioteca.repository.LoanRepository;
import br.com.biblioteca.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Loan loan;

    @BeforeEach
    public void setUp() {
        BookDetails bookDetails = new BookDetails(
                "Clean Code Test", "Robert C. Martin",
                "978-9999999999", "Tecnologia", "Prentice Hall"
        );
        Book book = bookRepository.save(new Book(bookDetails));

        UserDetails userDetails = new UserDetails(
                "UserTest", "usertest@email.com", "11999999999"
        );
        User user = userRepository.save(new User(userDetails));

        loan = new Loan(book, user);
    }

    @Test
    public void deveSalvarLoan() {
        Loan loanSalvo = loanRepository.save(loan);
        assertNotNull(loanSalvo.getId());
    }

    @Test
    public void deveBuscarLoanById() {
        Loan loanSalvo = loanRepository.save(loan);
        Optional<Loan> found = loanRepository.findById(loanSalvo.getId());
        assertTrue(found.isPresent());
    }

    @Test
    public void deveVerificarLivroEmprestado() {
        loanRepository.save(loan);
        boolean emprestado = loanRepository.existsByBookAndDetailsDataDevolucaoGreaterThanEqual(
                loan.getBook(), LocalDate.now()
        );
        assertTrue(emprestado);
    }

    @Test
    public void deveContarEmprestimosAtivos() {
        loanRepository.save(loan);
        long qtd = loanRepository.countByUserAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(
                loan.getUser(), LoanStatus.ATIVO, LocalDate.now()
        );
        assertEquals(1, qtd);
    }

    @Test
    public void deveRetornarEmprestimosVencidos() {
        loanRepository.save(loan);
        List<Loan> vencidos = loanRepository.findByDetailsDataDevolucaoLessThan(LocalDate.now());
        assertTrue(vencidos.isEmpty()); // empréstimo novo não está vencido
    }

}
