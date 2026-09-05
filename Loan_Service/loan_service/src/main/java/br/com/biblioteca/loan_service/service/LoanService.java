package br.com.biblioteca.loan_service.service;

import br.com.biblioteca.loan_service.DTO.LoanResponseDTO;
import br.com.biblioteca.loan_service.client.BibliotecaClient;
import br.com.biblioteca.loan_service.config.RabbitMQConfig;
import br.com.biblioteca.loan_service.event.LoanCreatedEvent;
import br.com.biblioteca.loan_service.model.Loan;
import br.com.biblioteca.loan_service.model.LoanStatus;
import br.com.biblioteca.loan_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BibliotecaClient bibliotecaClient;
    private final RabbitTemplate rabbitTemplate;

    public Loan save(Loan loan) {
        // valida se livro existe
        bibliotecaClient.buscarLivro(loan.getBookId());
        // valida se usuário existe
        bibliotecaClient.buscarUsuario(loan.getUserId());
        // verifica se livro está disponível
        boolean emprestado = loanRepository.existsByBookIdAndDetailsDataDevolucaoGreaterThanEqual(
                loan.getBookId(), LocalDate.now());
        if (emprestado) {
            throw new IllegalStateException("Livro indisponível!");
        }
        // verifica limite de empréstimos
        long qtd = loanRepository.countByUserIdAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(
                loan.getUserId(), LoanStatus.ATIVO, LocalDate.now());
        if (qtd >= 2) {
            throw new IllegalStateException("Usuário já atingiu o número máximo de empréstimos!");
        }
        //return loanRepository.save(loan);
        Loan salvo = loanRepository.save(loan);

        LoanCreatedEvent event = new LoanCreatedEvent(
                salvo.getId(),
                salvo.getBookId(),
                salvo.getUserId(),
                salvo.getDetails().getDataEmprestimo(),
                salvo.getDetails().getDataDevolucao()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_QUEUE, event);
        return salvo;
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }

    public List<Loan> findByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<Loan> findByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId);
    }

    public List<Loan> findEmprestimosVencidos() {
        return loanRepository.findByDetailsDataDevolucaoLessThan(LocalDate.now());
    }

    public Loan devolver(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado!"));
        loan.devolver();
        return loanRepository.save(loan);
    }

    public void deleteById(Long id) {
        loanRepository.deleteById(id);
    }

    public LoanResponseDTO toDTO(Loan loan) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getBookId(),
                loan.getUserId(),
                loan.getDetails().getDataEmprestimo(),
                loan.getDetails().getDataDevolucao(),
                loan.getDetails().getStatus().name()
        );
    }

    public List<LoanResponseDTO> findAllToDTO() {
        return loanRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
}