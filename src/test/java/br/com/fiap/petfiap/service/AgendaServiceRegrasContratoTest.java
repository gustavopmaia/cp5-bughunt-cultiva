package br.com.fiap.petfiap.service;

import br.com.fiap.petfiap.model.Banho;
import br.com.fiap.petfiap.exception.StatusInvalidoException;
import br.com.fiap.petfiap.repository.AtendimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AgendaServiceRegrasContratoTest {

    @Mock
    private AtendimentoRepository repository;

    @InjectMocks
    private AgendaService service;

    @Test
    void deveRecusarAgendamentoQuandoDataEstaNoPassado() {
        // Arrange
        Banho banho = new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.now().minusDays(1));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.agendar(banho));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRecusarCancelamentoQuandoAtendimentoEstaConcluido() {
        // Arrange
        Banho concluido = new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.now().plusDays(1));
        concluido.concluir();
        when(repository.findById(1L)).thenReturn(Optional.of(concluido));

        // Act + Assert
        assertThrows(StatusInvalidoException.class, () -> service.cancelar(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void deveCancelarQuandoAtendimentoEstaAgendado() {
        // Arrange
        Banho agendado = new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.now().plusDays(1));
        when(repository.findById(1L)).thenReturn(Optional.of(agendado));
        when(repository.save(agendado)).thenReturn(agendado);

        // Act
        Banho cancelado = (Banho) service.cancelar(1L);

        // Assert
        assertEquals("CANCELADO", cancelado.getStatus());
        verify(repository).save(agendado);
    }
}
