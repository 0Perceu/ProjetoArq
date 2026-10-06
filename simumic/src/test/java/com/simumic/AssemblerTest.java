package com.simumic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssemblerTest {
    @Test
    void montaProgramaComComentariosEInstrucaoExtendida() {
        assertArrayEquals(new int[] {0x7005, 0x1032, 0xFCFF},
                Assembler.assemble("; exemplo\n loco 5 ; imediato\nSTOD 50\nINSP 255\n"));
    }

    @Test
    void informaLinhaOriginalESemTruncamento() {
        AssemblerException erro = assertThrows(AssemblerException.class,
                () -> Assembler.assemble("; comentário\nLOCO 5\nINSP 256"));
        assertEquals(3, erro.getLinha());
        assertThrows(AssemblerException.class, () -> Assembler.assemble("LOCO 4096"));
        assertThrows(AssemblerException.class, () -> Assembler.assemble("PUSH 1"));
        assertThrows(AssemblerException.class, () -> Assembler.assemble("JUMP"));
        assertThrows(AssemblerException.class, () -> Assembler.assemble(""));
        String limite = "PUSH\n".repeat(4096);
        assertEquals(4096, Assembler.assemble(limite).length);
        assertThrows(AssemblerException.class, () -> Assembler.assemble(limite + "PUSH"));
    }
}
