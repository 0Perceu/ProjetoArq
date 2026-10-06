package com.simumic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CacheTest {
    @Test
    void registraMissHitConflitoEWriteThrough() {
        Memoria memoria = new Memoria();
        Cache l2 = new Cache("L2", memoria, 8, 4);
        Cache l1 = new Cache("L1", l2, 2, 4);
        memoria.write(0, 10);
        assertEquals(10, l1.read(0));
        assertEquals(1, l1.getMisses());
        assertEquals(10, l1.read(0));
        assertEquals(1, l1.getHits());
        l1.read(8); // bloco 2 ocupa a mesma linha do bloco 0
        assertEquals(2, l1.getMisses());
        l1.write(8, 25);
        assertTrue(l1.isUltimoAcessoHit());
        assertEquals(25, memoria.read(8));
        assertEquals(25, l2.read(8));
        l1.clear();
        assertFalse(l1.isUltimoAcessoHit());
    }
}
