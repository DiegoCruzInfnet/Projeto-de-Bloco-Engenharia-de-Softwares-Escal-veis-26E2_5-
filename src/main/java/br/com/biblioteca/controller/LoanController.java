package br.com.biblioteca.controller;

import br.com.biblioteca.model.Book;
import br.com.biblioteca.model.Loan;
import br.com.biblioteca.model.User;
import br.com.biblioteca.service.LoanService;
import br.com.biblioteca.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @GetMapping
    public List<Loan> findAll() {
        return loanService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> findById(@PathVariable Long id) {
        return loanService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Loan> save(@Valid @RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.save(loan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        loanService.deleteById(id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public ResponseEntity<Loan> update(@PathVariable Long id,@Valid @RequestBody Loan loan) {
        return loanService.findById(id)
                .map(existing -> ResponseEntity.ok(loanService.save(loan)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dataemprestimo/{dataempretimo}")
    public List<Loan> findByDataEmprestimo(@PathVariable LocalDate dataempretimo) {
        return loanService.findByDataEmprestimo(dataempretimo);
    }

    @GetMapping("/datadevolucao/{datadevolucao}")
    public List<Loan> findByDataDevolucao(@PathVariable LocalDate datadevolucao) {
        return loanService.findByDataDevolucao(datadevolucao);
    }

    @GetMapping("/user/{userId}")
    public List<Loan> findByUser(@PathVariable Long userId) {
        return loanService.findByUserId(userId);
    }

    @GetMapping("/vencidos")
    public List<Loan> findEmprestimosVencidos() {
        return loanService.findEmprestimosVencidos();
    }

    @PutMapping("/devolver/{id}")
    public ResponseEntity<Loan> devolver(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(loanService.devolver(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}