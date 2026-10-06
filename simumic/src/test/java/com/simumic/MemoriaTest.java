package com.simumic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoriaTest {
    @Test
    void armazenaPalavrasDe16BitsERestauraZeros() {
        Memoria memoria = new Memoria();
        assertEquals(0, memoria.read(0));
        memoria.write(4095, 0x12345);
        assertEquals(0x2345, memoria.read(4095));
        memoria.clear();
        assertEquals(0, memoria.read(4095));
    }
}
