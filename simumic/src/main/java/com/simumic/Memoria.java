// Memória externa que contém endereços, dados e se comunica com MBR e MAR
package com.simumic;

import java.util.Arrays;

public class Memoria implements AcessoMemoria {
    private final int[] enderecos = new int[4096];

    public void clear() {
        Arrays.fill(enderecos, 0);
    }

    // Sinal RD
    public int read(int endereco) {
        // Confere se existe para não dar out of index
        if (endereco >= 0 && endereco < enderecos.length) {
            return enderecos[endereco];
        }
        return 0;
    }

    // Sinal WR
    public void write(int endereco, int dado) {
        if (endereco >= 0 && endereco < enderecos.length) {
            enderecos[endereco] = dado & 0xFFFF;
        }
    }
}
