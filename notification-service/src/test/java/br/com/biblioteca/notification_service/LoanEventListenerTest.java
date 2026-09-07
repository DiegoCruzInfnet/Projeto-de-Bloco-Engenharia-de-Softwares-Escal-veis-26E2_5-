package br.com.biblioteca.notification_service;

import br.com.biblioteca.notification_service.event.LoanCreatedEvent;
import br.com.biblioteca.notification_service.listener.LoanEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoanEventListenerTest {

    @Autowired
    private LoanEventListener loanEventListener;

    @Test
    public void deveProcessarEventoSemErro() {
        LoanCreatedEvent event = new LoanCreatedEvent(
                1L, 1L, 1L,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );
        assertDoesNotThrow(() -> loanEventListener.handleLoanCreated(event));
    }

    @Test
    public void deveProcessarEventoComDadosCompletos() {
        LoanCreatedEvent event = new LoanCreatedEvent(
                10L, 5L, 3L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );
        assertDoesNotThrow(() -> loanEventListener.handleLoanCreated(event));
        assertEquals(10L, event.getLoanId());
        assertEquals(5L, event.getBookId());
        assertEquals(3L, event.getUserId());
    }

    @Test
    public void deveRetornarDadosCorretosDoEvento() {
        LocalDate emprestimo = LocalDate.now();
        LocalDate devolucao = LocalDate.now().plusDays(30);

        LoanCreatedEvent event = new LoanCreatedEvent(
                1L, 2L, 3L, emprestimo, devolucao
        );

        assertEquals(1L, event.getLoanId());
        assertEquals(2L, event.getBookId());
        assertEquals(3L, event.getUserId());
        assertEquals(emprestimo, event.getDataEmprestimo());
        assertEquals(devolucao, event.getDataDevolucao());
    }

    @Test
    public void deveProcessarEventoComLoanIdDiferente() {
        LoanCreatedEvent event1 = new LoanCreatedEvent(
                1L, 1L, 1L, LocalDate.now(), LocalDate.now().plusDays(30));
        LoanCreatedEvent event2 = new LoanCreatedEvent(
                2L, 1L, 1L, LocalDate.now(), LocalDate.now().plusDays(30));

        assertNotEquals(event1.getLoanId(), event2.getLoanId());
        assertDoesNotThrow(() -> loanEventListener.handleLoanCreated(event1));
        assertDoesNotThrow(() -> loanEventListener.handleLoanCreated(event2));
    }

    @Test
    public void dataDevolucaoDeveSerDepoisDataEmprestimo() {
        LoanCreatedEvent event = new LoanCreatedEvent(
                1L, 1L, 1L,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );
        assertTrue(event.getDataDevolucao().isAfter(event.getDataEmprestimo()));
    }
}