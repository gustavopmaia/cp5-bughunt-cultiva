package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsultaPrecoContratoTest {

    @Test
    void deveCobrarPrecoFixoQuandoForConsulta() {
        // Arrange
        LocalDateTime horario = LocalDateTime.of(2026, 12, 1, 10, 0);

        // Act + Assert
        for (String porte : new String[] {"PEQUENO", "MEDIO", "GRANDE"}) {
            ConsultaVeterinaria consulta = new ConsultaVeterinaria(1, "Mimi", porte, "Bruno", horario);
            assertEquals(150.0, consulta.calcularPreco());
        }
    }
}
