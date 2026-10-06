package com.simumic;

public class AssemblerException extends IllegalArgumentException {
    private final int linha;

    public AssemblerException(int linha, String mensagem) {
        super("Linha " + linha + ": " + mensagem);
        this.linha = linha;
    }

    public int getLinha() {
        return linha;
    }
}
