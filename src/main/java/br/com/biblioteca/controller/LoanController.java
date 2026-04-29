package br.com.biblioteca.controller;

import br.com.biblioteca.model.Book;
import br.com.biblioteca.model.Loan;
import br.com.biblioteca.model.User;
import br.com.biblioteca.service.LoanService;
import br.com.biblioteca.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Loan> save(@RequestBody Loan loan) {
        return ResponseEntity.ok(loanService.save(loan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        loanService.deleteById(id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/id")
    public ResponseEntity<Loan> update(@PathVariable Long id, @RequestBody Loan loan) {
        return loanService.findById(id)
                .map(existing -> ResponseEntity.ok(loanService.save(loan)))
                .orElse(ResponseEntity.notFound().build());
    }
}