package com.simumic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CPUTest {
    private static void instrucao(CPU cpu) {
        long antes = cpu.getInstrucoesExecutadas();
        for (int i = 0; i < 10000; i++) {
            cpu.executarSubciclo();
            if (cpu.getInstrucoesExecutadas() > antes && cpu.getMPC() == 0 && cpu.getSubcicloAtual() == 1) return;
        }
        fail("Microprograma não retornou ao fetch; MPC=" + cpu.getMPC());
    }

    @Test
    void programaBaseEmRamDiretaEEmCaches() {
        verificarPrograma(false);
        verificarPrograma(true);
    }

    private static void verificarPrograma(boolean comCache) {
        Memoria ram = new Memoria();
        int[] programa = {0x7005, 0x1032, 0x7003, 0x2032, 0x1033};
        for (int i = 0; i < programa.length; i++) ram.write(i, programa[i]);
        AcessoMemoria acesso = ram;
        if (comCache) acesso = new Cache("L1", new Cache("L2", ram, 8, 4), 2, 4);
        CPU cpu = new CPU(acesso);
        assertEquals(0, cpu.getInstrucoesExecutadas());
        for (int i = 0; i < programa.length; i++) instrucao(cpu);
        assertEquals(8, cpu.getReg(1));
        assertEquals(5, ram.read(50));
        assertEquals(8, ram.read(51));
        assertEquals(5, cpu.getReg(0));
        assertEquals(5, cpu.getInstrucoesExecutadas());
    }

    @Test
    void cicloCompletoContinuaSubcicloParcial() {
        for (int posicao = 1; posicao <= 4; posicao++) {
            Memoria ram = new Memoria();
            ram.write(0, 0x7005);
            CPU porCiclo = new CPU(ram);
            CPU porPasso = new CPU(ram);
            for (int i = 1; i < posicao; i++) {
                porCiclo.executarSubciclo();
                porPasso.executarSubciclo();
            }
            porCiclo.executarCicloCompleto();
            do {
                porPasso.executarSubciclo();
            } while (porPasso.getSubcicloAtual() != 1);
            assertEquals(porPasso.getMPC(), porCiclo.getMPC());
            assertEquals(porPasso.getTotalCiclos(), porCiclo.getTotalCiclos());
            assertEquals(porPasso.getTotalSubciclos(), porCiclo.getTotalSubciclos());
        }
    }

    @Test
    void saltosCondicionaisUsamResultadoDaUla() {
        Memoria ram = new Memoria();
        int[] programa = {0x7000, 0x5003, 0x7009, 0x7001, 0x4006, 0x7008, 0x7000};
        for (int i = 0; i < programa.length; i++) ram.write(i, programa[i]);
        CPU cpu = new CPU(ram);
        instrucao(cpu); // LOCO 0
        instrucao(cpu); // JZER 3
        assertEquals(3, cpu.getReg(0));
        instrucao(cpu); // LOCO 1
        instrucao(cpu); // JPOS 6
        assertEquals(6, cpu.getReg(0));
        assertEquals(1, cpu.getReg(1));
    }

    @Test
    void pushPopECallRetnPreservamPilha() {
        Memoria ram = new Memoria();
        int[] programa = {0x7014, 0xFA00, 0x702A, 0xF400, 0x7000, 0xF600,
                0xE009, 0x600B, 0x7001, 0x7009, 0xF800, 0x7007};
        for (int i = 0; i < programa.length; i++) ram.write(i, programa[i]);
        CPU cpu = new CPU(ram);
        for (int i = 0; i < 4; i++) instrucao(cpu);
        assertEquals(19, cpu.getReg(2));
        assertEquals(42, ram.read(19));
        instrucao(cpu); // LOCO 0
        instrucao(cpu); // POP
        assertEquals(42, cpu.getReg(1));
        assertEquals(20, cpu.getReg(2));
        instrucao(cpu); // CALL 9
        assertEquals(9, cpu.getReg(0));
        assertEquals(7, ram.read(19));
        instrucao(cpu); // LOCO 9
        instrucao(cpu); // RETN
        assertEquals(7, cpu.getReg(0));
        assertEquals(20, cpu.getReg(2));
    }
}
