package com.simumic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Monta instruções MAC-1 em palavras de 16 bits sem alterar a máquina. */
public final class Assembler {
    private Assembler() { }

    public static int[] assemble(String source) {
        List<Integer> palavras = new ArrayList<>();
        String[] linhas = (source == null ? "" : source).split("\\R", -1);
        for (int indice = 0; indice < linhas.length; indice++) {
            String linha = linhas[indice].split(";", 2)[0].trim();
            if (linha.isEmpty()) continue;
            if (palavras.size() == 4096) {
                throw new AssemblerException(indice + 1, "o programa excede 4096 palavras.");
            }
            String[] partes = linha.split("\\s+");
            String mnem = partes[0].toUpperCase(Locale.ROOT);
            int opcode;
            int bits;
            switch (mnem) {
                case "LODD": opcode = 0x0000; bits = 12; break;
                case "STOD": opcode = 0x1000; bits = 12; break;
                case "ADDD": opcode = 0x2000; bits = 12; break;
                case "SUBD": opcode = 0x3000; bits = 12; break;
                case "JPOS": opcode = 0x4000; bits = 12; break;
                case "JZER": opcode = 0x5000; bits = 12; break;
                case "JUMP": opcode = 0x6000; bits = 12; break;
                case "LOCO": opcode = 0x7000; bits = 12; break;
                case "LODL": opcode = 0x8000; bits = 12; break;
                case "STOL": opcode = 0x9000; bits = 12; break;
                case "ADDL": opcode = 0xA000; bits = 12; break;
                case "SUBL": opcode = 0xB000; bits = 12; break;
                case "JNEG": opcode = 0xC000; bits = 12; break;
                case "JNZE": opcode = 0xD000; bits = 12; break;
                case "CALL": opcode = 0xE000; bits = 12; break;
                case "PSHI": opcode = 0xF000; bits = 0; break;
                case "POPI": opcode = 0xF200; bits = 0; break;
                case "PUSH": opcode = 0xF400; bits = 0; break;
                case "POP":  opcode = 0xF600; bits = 0; break;
                case "RETN": opcode = 0xF800; bits = 0; break;
                case "SWAP": opcode = 0xFA00; bits = 0; break;
                case "INSP": opcode = 0xFC00; bits = 8; break;
                case "DESP": opcode = 0xFE00; bits = 8; break;
                default: throw new AssemblerException(indice + 1, "mnemônico desconhecido: " + mnem + ".");
            }
            if (partes.length != (bits == 0 ? 1 : 2)) {
                throw new AssemblerException(indice + 1,
                        bits == 0 ? "esta instrução não aceita operando." : "informe um único operando decimal.");
            }
            int operando = 0;
            if (bits != 0) {
                try {
                    operando = Integer.parseInt(partes[1]);
                } catch (NumberFormatException e) {
                    throw new AssemblerException(indice + 1, "operando decimal inválido.");
                }
                int maximo = bits == 8 ? 255 : 4095;
                if (operando < 0 || operando > maximo) {
                    throw new AssemblerException(indice + 1, "operando fora da faixa de 0 a " + maximo + ".");
                }
            }
            palavras.add(opcode | operando);
        }
        if (palavras.isEmpty()) throw new AssemblerException(1, "o programa está vazio.");
        return palavras.stream().mapToInt(Integer::intValue).toArray();
    }
}
