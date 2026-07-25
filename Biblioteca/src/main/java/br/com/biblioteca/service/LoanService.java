package br.com.biblioteca.service;

import br.com.biblioteca.model.Loan;
import br.com.biblioteca.model.LoanStatus;
import br.com.biblioteca.model.User;
import br.com.biblioteca.repository.LoanRepository;
import br.com.biblioteca.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public Loan save(Loan loan) {
        //Verifica se livro está disponível
        boolean emprestado = loanRepository.existsByBookAndDetailsDataDevolucaoGreaterThanEqual(loan.getBook(), LocalDate.now());
        if (emprestado) {
            throw new IllegalStateException("Livro indisponível");
        }
        //verifica se user tem mais de 2 livros emprestados
        long qtdLivros = loanRepository.countByUserAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(loan.getUser(), LoanStatus.ATIVO,LocalDate.now());
        if (qtdLivros >= 2) {
            throw new IllegalStateException("Usuário já atingiu o número máximo de empréstimos!");
        }
        return loanRepository.save(loan);
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }

    public void deleteById(Long id) {
        loanRepository.deleteById(id);
    }

    public List<Loan> findByDataEmprestimo(LocalDate dataEmprestimo) {
        return loanRepository.findByDetailsDataEmprestimo(dataEmprestimo);
    }

    public List<Loan> findByDataDevolucao(LocalDate dataDevolucao) {
        return loanRepository.findByDetailsDataDevolucao(dataDevolucao);
    }

    public List<Loan> findEmprestimosVencidos() {
        return loanRepository.findByDetailsDataDevolucaoLessThan(LocalDate.now());
    }

    public List<Loan> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return loanRepository.findByUser(user);
    }

    public Loan devolver(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado!"));
        loan.devolver();
        return loanRepository.save(loan);
    }

}
