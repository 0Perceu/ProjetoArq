# Validação Windows

## Ambiente de build

- Data: 06/10/2026.
- Sistema: Windows 11 x64, ambiente de desenvolvimento com Java instalado.
- JDK de build: Eclipse Temurin 21.0.12.1+1, obtido de forma isolada para esta validação.
- Maven: 3.9.9 via Maven Wrapper oficial.
- JavaFX: 21.0.11.
- WiX: 3.14.1, binários oficiais isolados.

## Resultados

Preencher somente após cada verificação real. Um teste automático não comprova aparência, atalhos ou instalação em máquina limpa.

| Verificação | Resultado |
| --- | --- |
| `mvnw.cmd test` | Aprovado: 8 testes, 0 falhas |
| `mvnw.cmd clean verify` | Aprovado como parte do build final: 8 testes, 0 falhas |
| `package-windows.ps1 -PackageType portable` | Aprovado: ZIP de 31.396.439 bytes |
| `package-windows.ps1 -PackageType installer` | Aprovado: EXE de 32.483.328 bytes |
| Iniciar launcher empacotado | Aprovado no build final: processo permaneceu ativo por 8 segundos, sem erro no stderr; foi encerrado após o teste |
| Interface em 1366 × 768 com escala de 125% | Não executado |
| Abrir em Windows sem Java/Maven/Docker | Não executado |
| Instalar/desinstalar com conta comum | Não executado |
| Atualizar de uma versão de teste para outra | Não executado |
| Executar pacote offline | Não executado |

Módulos presentes na imagem: `com.simumic@1.0.0`, `javafx.controls@21.0.11`, `javafx.fxml@21.0.11` e `javafx.graphics@21.0.11`.

SHA-256 dos pacotes locais:

```text
ac513addbe2e9a8d0b8db3cc602196fe61127b5e40a76a91d61a83810e7e57aa  ProjetoArq-1.0.0-windows-x64.zip
60f3701638cc3bf9ff7867876a7e67687d6d15bf99f80eb116a7ed44a8a061ae  ProjetoArq-1.0.0-windows-x64-setup.exe
```

Para completar a validação manual, anotar o commit, versão do pacote, resolução, escala, tempo entre o duplo clique e a janela pronta, tamanho dos arquivos e resultado do programa de exemplo. Conferir também RAM/cache, montagem inválida, reinício, pausa de loop e fechamento do processo.
