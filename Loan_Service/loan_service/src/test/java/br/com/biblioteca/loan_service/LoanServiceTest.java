package br.com.biblioteca.loan_service;

import br.com.biblioteca.loan_service.client.BibliotecaClient;
import br.com.biblioteca.loan_service.config.RabbitMQConfig;
import br.com.biblioteca.loan_service.event.LoanCreatedEvent;
import br.com.biblioteca.loan_service.model.Loan;
import br.com.biblioteca.loan_service.model.LoanStatus;
import br.com.biblioteca.loan_service.repository.LoanRepository;
import br.com.biblioteca.loan_service.service.LoanService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BibliotecaClient bibliotecaClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan(1L, 1L);
    }

    @Test
    void deveSalvarEPublicarEvento() {
        when(loanRepository.existsByBookIdAndDetailsDataDevolucaoGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(false);
        when(loanRepository.countByUserIdAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(
                eq(1L), eq(LoanStatus.ATIVO), any(LocalDate.class))).thenReturn(0L);
        when(loanRepository.save(loan)).thenReturn(loan);

        Loan salvo = loanService.save(loan);

        assertSame(loan, salvo);
        verify(loanRepository).save(loan);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.LOAN_QUEUE), any(LoanCreatedEvent.class));
    }

    @Test
    void naoDeveEmprestarLivroJaEmprestado() {
        when(loanRepository.existsByBookIdAndDetailsDataDevolucaoGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.save(loan));

        assertEquals("Livro indisponível!", ex.getMessage());
        verify(loanRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void naoDevePermitirTerceiroEmprestimo() {
        when(loanRepository.existsByBookIdAndDetailsDataDevolucaoGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(false);
        when(loanRepository.countByUserIdAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(
                eq(1L), eq(LoanStatus.ATIVO), any(LocalDate.class))).thenReturn(2L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.save(loan));

        assertEquals("Usuário já atingiu o número máximo de empréstimos!", ex.getMessage());
        verify(loanRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void devePermitirSegundoEmprestimo() {
        when(loanRepository.existsByBookIdAndDetailsDataDevolucaoGreaterThanEqual(eq(1L), any(LocalDate.class)))
                .thenReturn(false);
        when(loanRepository.countByUserIdAndDetailsStatusAndDetailsDataDevolucaoGreaterThanEqual(
                eq(1L), eq(LoanStatus.ATIVO), any(LocalDate.class))).thenReturn(1L);
        when(loanRepository.save(loan)).thenReturn(loan);

        assertDoesNotThrow(() -> loanService.save(loan));
        verify(loanRepository).save(loan);
    }

    @Test
    void naoDeveEmprestarSeLivroNaoExiste() {
        when(bibliotecaClient.buscarLivro(1L)).thenThrow(mock(FeignException.NotFound.class));

        assertThrows(FeignException.NotFound.class, () -> loanService.save(loan));

        verify(loanRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void naoDeveEmprestarSeUsuarioNaoExiste() {
        when(bibliotecaClient.buscarUsuario(1L)).thenThrow(mock(FeignException.NotFound.class));

        assertThrows(FeignException.NotFound.class, () -> loanService.save(loan));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void devolverDeveMarcarComoDevolvido() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        Loan devolvido = loanService.devolver(10L);

        assertEquals(LoanStatus.DEVOLVIDO, devolvido.getDetails().getStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    void devolverDeveFalharSeEmprestimoNaoExiste() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.devolver(99L));

        assertEquals("Empréstimo não encontrado!", ex.getMessage());
        verify(loanRepository, never()).save(any());
    }
}