package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BanhoPrecoContratoTest {

    @Test
    void deveCobrarPrecoPorPorteQuandoForBanho() {
        // Arrange
        LocalDateTime horario = LocalDateTime.of(2026, 12, 1, 10, 0);
        Banho pequeno = new Banho(1, "Rex", "PEQUENO", "Ana", horario);
        Banho medio = new Banho(2, "Mimi", "MEDIO", "Bruno", horario);
        Banho grande = new Banho(3, "Bob", "GRANDE", "Clara", horario);

        // Act + Assert
        assertEquals(60.0, pequeno.calcularPreco());
        assertEquals(80.0, medio.calcularPreco());
        assertEquals(100.0, grande.calcularPreco());
    }
}
