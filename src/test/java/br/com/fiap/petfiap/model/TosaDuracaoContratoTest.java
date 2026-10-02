package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TosaDuracaoContratoTest {

    @Test
    void deveDurar60MinutosQuandoForTosa() {
        // Arrange
        Atendimento tosa = new Tosa(1, "Rex", "PEQUENO", "Ana", LocalDateTime.of(2026, 12, 1, 10, 0));

        // Act
        int duracao = tosa.getDuracaoMinutos();

        // Assert
        assertEquals(60, duracao);
    }
}
