package com.subacol.msagenda.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AsesorTest {
    @Test
    @DisplayName("03 REQ-2.2 con las ocupadas iguales al cupo no hay cupo")
    void testTieneCupo() {
        Asesor asesor = new Asesor(1, "Ana", 2);

        assertThat(asesor.tieneCupo(0)).isTrue();
        assertThat(asesor.tieneCupo(1)).isTrue();
        assertThat(asesor.tieneCupo(2)).isFalse();
    }
}
