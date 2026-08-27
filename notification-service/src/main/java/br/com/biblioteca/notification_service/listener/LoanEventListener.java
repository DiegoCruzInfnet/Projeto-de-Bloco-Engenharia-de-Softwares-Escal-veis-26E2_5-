package br.com.biblioteca.notification_service.listener;

import br.com.biblioteca.notification_service.event.LoanCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanEventListener {

    @RabbitListener(queues = "loan.created")
    public void handleLoanCreated(LoanCreatedEvent event) {
        log.info("📧 Notificação enviada!");
        log.info("Empréstimo ID: {}", event.getLoanId());
        log.info("Livro ID: {}", event.getBookId());
        log.info("Usuário ID: {}", event.getUserId());
        log.info("Data empréstimo: {}", event.getDataEmprestimo());
        log.info("Data devolução: {}", event.getDataDevolucao());
    }
}
