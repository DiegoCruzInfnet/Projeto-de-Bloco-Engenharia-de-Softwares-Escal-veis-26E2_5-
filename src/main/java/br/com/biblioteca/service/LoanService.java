package br.com.biblioteca.service;

import br.com.biblioteca.model.Loan;
import br.com.biblioteca.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    public Loan save(Loan loan) {
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
}
