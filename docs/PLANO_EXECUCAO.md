# Plano de execução — ProjetoArq desktop

Data: 06/10/2026. Estado: plano para implementação; nenhuma das etapas abaixo está concluída por este documento.

## 1. Objetivo e decisão técnica

Transformar o SimuMIC existente em um aplicativo Windows x64 fácil de baixar, abrir e usar. O usuário final não instalará Java, Maven, Docker, WSL ou servidor X. O programa funcionará sem internet depois de baixado.

Manter Java e JavaFX, reaproveitando a CPU, a memória, as caches, os controladores e os arquivos FXML existentes. A prioridade de “eficiência” é reduzir instalação, configuração e manutenção. Este plano não promete acelerar a simulação nem alterar a microarquitetura.

Entregas obrigatórias:

1. `ProjetoArq-1.0.0-windows-x64.zip`: extrair a pasta inteira e abrir `ProjetoArq.exe`.
2. `ProjetoArq-1.0.0-windows-x64-setup.exe`: instalar para o usuário atual e abrir pelo atalho.
3. Desenvolvimento local com JDK 21 e Maven Wrapper; Maven não precisa ser instalado separadamente.
4. Build e testes no GitHub Actions, com geração dos dois pacotes e uma Release em rascunho para versões marcadas.
5. Instruções curtas de uso e verificações de funcionamento registradas.

O executável portátil depende das pastas que o acompanham. Não apresentar o `.exe` isolado como um programa autocontido de arquivo único.

### Alternativas consideradas

| Alternativa | Consequência | Decisão |
| --- | --- | --- |
| JavaFX com Java incluído | Reutiliza o projeto e entrega abertura por duplo clique | Escolhida |
| Apenas JAR ou script que chama Maven | Continua exigindo ambiente Java e configuração do usuário | Usar somente no desenvolvimento |
| Reescrever para navegador | Simplifica acesso por link, mas exige portar CPU, interface e testes | Fora desta versão |

Plataforma inicial: Windows 11 x64. Windows 10 x64 só deve ser anunciado como testado após uma execução real nesse sistema. Linux, macOS e ARM ficam para versões posteriores; cada pacote nativo exige build para sua plataforma.

## 2. Base atual e limites

- Projeto Maven em `simumic/pom.xml`, atualmente Java 11 e JavaFX 13.
- O FXML principal já declara namespace JavaFX 21.
- Entrada: `com.simumic.App`; módulo JPMS: `com.simumic`.
- Núcleo: `CPU`, `Memoria`, `Cache`, `AcessoMemoria`.
- Interface: `PrimaryController`, `SecondaryController`, `TertiaryController`, três FXMLs e CSS.
- `App.start()` solicita uma janela de 1920 × 1080; isso dificulta uso em notebooks menores.
- Não há suíte automatizada em `src/test`; `testes_simulador` contém exemplos manuais.
- `ESTRUTURA_PROJETO.md` já existe como arquivo não versionado. Preservá-lo; não sobrescrever nem incluí-lo incidentalmente em commits.

Preservar registradores de 16 bits, RAM de 4096 palavras, quatro subciclos, microcódigo e instruções existentes. Não migrar para outra variante da MIC-1, adicionar pipeline, multiprocessamento, banco de dados, servidor, Electron ou atualização automática.

As correções funcionais da etapa 5 são delimitadas e separadas do empacotamento. Não aproveitar essa etapa para reescrever o controlador inteiro ou modificar microcódigo sem um teste específico que demonstre o problema.

## 3. Tecnologias e versões

| Componente | Escolha | Finalidade |
| --- | --- | --- |
| Java | Eclipse Temurin JDK 21, x64 | Compilar, executar e gerar o runtime |
| Interface | JavaFX 21.0.11, `javafx-controls` e `javafx-fxml` | Atualizar a base existente sem trocar de biblioteca |
| Build | Maven 3.9.9 via Maven Wrapper oficial | Build reproduzível sem Maven global |
| Plugin JavaFX | `org.openjfx:javafx-maven-plugin:0.0.8` | `javafx:run` e `javafx:jlink` |
| Compilação | `maven-compiler-plugin:3.13.0`, `release=21` | Compilar o módulo Java |
| Testes | JUnit Jupiter 5.11.4 + Surefire 3.5.2 | Testes do núcleo sem abrir JavaFX |
| Runtime | `jlink` do mesmo JDK 21 | Incluir somente os módulos necessários |
| Pacotes | `jpackage` do mesmo JDK 21 | Pasta portátil e instalador Windows |
| Instalador | WiX Toolset 3.14.1 | Backend do instalador com JDK 21 |
| Scripts | PowerShell compatível com 5.1 | Build local e automação Windows |
| CI | GitHub Actions, runner `windows-2022` x64 | Verificar e gerar os artefatos |

Essas são versões-base propostas, não uma alegação de que todas sejam as versões mais recentes. Na etapa 1, confirmar disponibilidade nos repositórios oficiais e executar o build. Usar uma atualização de manutenção compatível se houver impedimento documentado; registrar a versão efetivamente usada no POM e no relatório. Não usar `LATEST`, intervalos de versão ou dependências `SNAPSHOT` na entrega.

Fixar também o patch/distribuição do JDK escolhido no workflow e registrar `java -version` no build. O runtime entregue receberá atualizações quando o aplicativo for recompilado e redistribuído.

WiX é ferramenta da máquina que gera o instalador; não é requisito de quem instala ou usa o aplicativo. A versão portátil deve poder ser gerada mesmo sem WiX. Com essa combinação JDK/WiX, verificar `candle.exe` e `light.exe`; não substituir automaticamente por uma versão principal diferente de WiX.

## 4. Estrutura de arquivos esperada

```text
projetoarq/
  README.md
  docs/
    PLANO_EXECUCAO.md
    VALIDACAO_WINDOWS.md
  .github/workflows/
    ci.yml
    release-windows.yml
  scripts/
    dev.ps1
    package-windows.ps1
  packaging/
    windows-upgrade-uuid.txt
  simumic/
    pom.xml
    mvnw
    mvnw.cmd
    .mvn/wrapper/maven-wrapper.properties
    src/main/java/module-info.java
    src/main/java/com/simumic/
      App.java
      CPU.java
      Memoria.java
      Cache.java
      AcessoMemoria.java
      Assembler.java
      AssemblerException.java
      PrimaryController.java
      SecondaryController.java
      TertiaryController.java
    src/main/resources/com/simumic/
      primary.fxml
      secondary.fxml
      tertiary.fxml
      style.css
      examples/soma.asm
    src/test/java/com/simumic/
      CPUTest.java
      MemoriaTest.java
      CacheTest.java
      AssemblerTest.java
    target/                         # gerado; não versionar
  dist/                             # gerado; não versionar
```

Não criar arquivos de implementação vazios para cumprir a árvore. Cada arquivo novo deve ser introduzido na etapa que o utiliza. Manter os nomes Java existentes; usar `ProjetoArq` apenas como nome visível e nome dos pacotes.

## 5. Etapas de implementação

Executar em ordem. Ao terminar cada etapa, registrar arquivos alterados, comandos executados e resultado. Se uma verificação falhar, corrigir a causa antes de avançar; não registrar uma verificação não executada como aprovada.

### Etapa 1 — Padronizar o build

Arquivos: `simumic/pom.xml`, Wrapper e `.gitignore`.

1. Conferir instruções locais, estado do Git e remoto. Trabalhar em `codex/windows-desktop`, preservando mudanças existentes.
2. Instalar ou localizar JDK 21 x64 para desenvolvimento; verificar `JAVA_HOME`, `java`, `javac`, `jlink` e `jpackage` apontando para o mesmo JDK.
3. Gerar Maven Wrapper pelo mecanismo oficial, preferencialmente distribuição `only-script`, fixando Maven 3.9.9. Se precisar de Maven para gerar o Wrapper, usar uma distribuição oficial temporária; não escrever uma imitação do Wrapper à mão.
4. Configurar versões da tabela, UTF-8 e `maven.compiler.release=21`. Remover a configuração redundante de source/target 11.
5. Configurar `mainClass` do plugin JavaFX como `com.simumic/com.simumic.App`, fora de uma configuração restrita apenas a uma execução Maven.
6. Manter `module-info.java` e a abertura de `com.simumic` para `javafx.fxml`. Adicionar novos módulos apenas se houver uso real.
7. Para os testes iniciais, configurar Surefire com `useModulePath=false`, evitando adicionar JUnit ao módulo de produção. A compilação e a distribuição do app continuam modulares.
8. Ignorar `dist/` e manter `target/` ignorado. O Wrapper oficial precisa estar versionado; se a modalidade gerada usar JAR, criar exceção específica no `.gitignore` para esse JAR.
9. Verificar resolução da versão JavaFX e carregamento dos recursos FXML/CSS pelo classpath, sem caminhos absolutos da máquina do autor.

Comandos, a partir de `simumic/`:

```powershell
.\mvnw.cmd --version
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

Aceite: a interface abre e os inspetores de RAM/cache carregam. O build funciona com JDK e Wrapper, sem Maven global. A primeira preparação exige internet para baixar ferramentas/dependências; isso não vale para o aplicativo distribuído.

### Etapa 2 — Criar testes mínimos de proteção

Arquivos: quatro classes de teste, introduzindo `AssemblerTest` somente na etapa 5.

1. Testar limites válidos da RAM e leitura/escrita de palavras.
2. Testar cache miss, hit, substituição por conflito e propagação de escrita até a RAM.
3. Rodar a CPU com RAM diretamente e depois com L1/L2. Comparar resultados dos mesmos programas.
4. Criar um auxiliar de teste que execute subciclos até o retorno a `MPC=0` e próximo subciclo 1 após uma macroinstrução. Impor limite de 10.000 subciclos por macroinstrução; exceder o limite deve falhar com diagnóstico.
5. Cobrir carga, armazenamento, soma/subtração, saltos condicionais, PUSH/POP e CALL/RETN com estados esperados explícitos. Inicializar SP por instruções do programa, quando necessário.

Programa-base, palavras em hexadecimal para testes independentes do montador:

```text
7005 1032 7003 2032 1033
```

Equivale a `LOCO 5; STOD 50; LOCO 3; ADDD 50; STOD 51`. Após cinco macroinstruções: AC=8, RAM[50]=5, RAM[51]=8 e PC=5. Não usar espera por tempo de parede para determinar conclusão.

Aceite: suíte executada pelo Wrapper, sem inicializar janela gráfica. Se revelar defeito anterior de microcódigo, registrar o caso e separar a correção do trabalho de empacotamento.

### Etapa 3 — Gerar a versão portátil primeiro

Arquivos: POM, `scripts/package-windows.ps1`, `.gitignore`.

Contrato do script:

```powershell
.\scripts\package-windows.ps1 -PackageType portable
```

Valores aceitos: `portable`, `installer`, `all`. Padrão: `portable`.

1. Resolver a raiz pelo local do próprio script, não pelo diretório de onde foi chamado. Tratar caminhos com espaços e acentos.
2. Usar `$ErrorActionPreference = 'Stop'` e conferir `$LASTEXITCODE` após cada programa externo. Não continuar quando Maven, jlink ou jpackage falhar.
3. Ler a versão do POM; começar a entrega em `1.0.0`. Validar formato numérico `major.minor.patch` e limites aceitos pelo instalador. A versão do pacote e a versão Maven precisam coincidir.
4. Configurar `javafx:jlink` com `jlinkImageName=runtime`, `stripDebug=true`, `noHeaderFiles=true`, `noManPages=true`. Não excluir recursos nem bibliotecas nativas JavaFX.
5. Executar `mvnw.cmd clean verify javafx:jlink` no módulo. Esperar a imagem em `simumic/target/runtime`.
6. Usar `jpackage` modular com essa imagem, gerando o launcher gráfico. Não usar fat JAR, Maven Shade ou um segundo sistema de build.
7. Validar que runtime, DLLs JavaFX, FXML, CSS e exemplos estão presentes e utilizáveis pelo programa gerado.
8. Compactar a pasta completa `ProjetoArq`, incluindo runtime, em `dist/ProjetoArq-1.0.0-windows-x64.zip`.

Referência de chamada, a partir de `simumic/`, depois de criar o runtime:

```powershell
jpackage --type app-image --name ProjetoArq --app-version 1.0.0 --runtime-image target/runtime --module com.simumic/com.simumic.App --dest target/package
```

O script deve usar os caminhos resolvidos e a versão lida do POM, em vez de copiar os valores fixos do exemplo. O módulo da aplicação e suas dependências devem estar na imagem criada por jlink; confirmar isso com a imagem real antes de considerar a chamada validada.

Não passar `--win-console` no pacote de entrega. Para diagnosticar falhas de inicialização, produzir uma imagem separada com console ou executar o launcher do runtime durante o desenvolvimento.

Build repetido: limpar somente saídas conhecidas sob `simumic/target` e os arquivos de distribuição desta versão. Validar caminhos absolutos antes de qualquer remoção; nunca apagar a raiz do repositório ou a pasta de usuário.

Aceite: extrair o ZIP em outro diretório, abrir o `.exe`, executar o exemplo e abrir os dois inspetores. Repetir com nome de pasta contendo espaço e acento. Não mover somente o executável.

### Etapa 4 — Gerar o instalador

Arquivos: `scripts/package-windows.ps1`, `packaging/windows-upgrade-uuid.txt`.

1. Adicionar geração do instalador a partir da imagem portátil já validada.
2. Verificar WiX 3.14.1 e as ferramentas `candle`/`light` antes de iniciar a geração. Se ausentes, emitir mensagem objetiva; o modo portátil continua independente de WiX.
3. Criar um UUID de upgrade uma única vez, salvar no arquivo indicado e reutilizar em todas as versões. Não gerar UUID diferente em cada build.
4. Configurar instalação por usuário, atalho da área de trabalho e Menu Iniciar. Usar nome `ProjetoArq` e endereço do repositório no campo de ajuda.
5. Não depender de execução como administrador para uso normal. Preservar metadados necessários do jpackage na imagem usada para empacotar.

Referência de opções, além dos caminhos e versão resolvidos:

```text
--type exe
--name ProjetoArq
--app-image <pasta da imagem gerada>
--app-version <versão do POM>
--win-per-user-install
--win-shortcut
--win-menu
--win-menu-group ProjetoArq
--win-upgrade-uuid <UUID persistido>
--win-help-url https://github.com/0Perceu/ProjetoArq
--dest <pasta de saída>
```

Renomear o resultado para `ProjetoArq-1.0.0-windows-x64-setup.exe`. O instalador deve carregar tudo de que o aplicativo precisa e não baixar Java durante a instalação.

Aceite: instalar em conta de usuário comum, abrir pelos atalhos, fechar e desinstalar pelo Windows. Validar também atualização entre duas versões de teste com o mesmo UUID.

Assinatura digital comercial não faz parte da primeira entrega. Um pacote sem assinatura pode exibir aviso de reputação do Windows; não prometer ausência de avisos e não orientar a desativar proteções do sistema.

### Etapa 5 — Corrigir atritos de uso com alterações pequenas

Esta etapa melhora o comportamento do aplicativo, mas não é pré-requisito para provar o empacotamento. Manter as alterações separadas em commits e testes.

#### 5.1 Montagem e carregamento previsíveis

Arquivos: `Assembler.java`, `AssemblerException.java`, `PrimaryController.java`, testes.

- Extrair a tradução textual do controlador para `Assembler.assemble(String source): int[]`.
- O montador não acessa RAM, caches, CPU nem JavaFX. Ele retorna todas as palavras ou lança erro com número da linha original e mensagem em português.
- Aceitar mnemônicos sem diferenciar maiúsculas/minúsculas, linhas vazias e comentários iniciados por `;`, inclusive no final da linha.
- Nesta versão, aceitar operandos decimais apenas. Instruções com endereço/constante de 12 bits exigem um operando de 0 a 4095; INSP/DESP exigem 0 a 255. PSHI, POPI, PUSH, POP, RETN e SWAP não aceitam operandos.
- Rejeitar mnemônico desconhecido, argumento faltante/excedente, faixa inválida, programa vazio e mais de 4096 palavras. Não truncar argumentos inválidos silenciosamente.
- Manter exatamente os opcodes existentes. Para INSP/DESP, usar campo de 8 bits depois da validação, evitando alterar bits do opcode.
- Antes de montar, parar a execução automática. Se a montagem falhar, preservar o estado da máquina e informar o erro; a execução fica pausada.
- Se a montagem funcionar: zerar RAM, gravar todas as instruções, limpar L1/L2, resetar CPU, substituir o limite do programa pelo tamanho atual e atualizar a interface.
- Guardar uma cópia das palavras originais montadas para permitir reiniciar do mesmo estado, sem conservar dados produzidos pela execução anterior.

#### 5.2 Reinício, execução e ciclos

Arquivos: `PrimaryController.java`, `CPU.java`, `Memoria.java`, `Cache.java` e testes.

- Remover o valor de depuração do construtor de RAM. Adicionar `clear()` e garantir gravação de palavras com máscara de 16 bits.
- Definir o botão Reiniciar como: parar execução, limpar RAM/caches, restaurar programa originalmente montado e resetar CPU. Sem programa carregado, retornar ao estado vazio.
- Bloquear execução quando não houver programa montado. Após um programa acabar, permitir Reiniciar e executar novamente.
- Usar um único método de avanço para botões e execução automática, conferindo o limite do programa antes da próxima busca, quando `MPC=0` e próximo subciclo=1. `PC` fora de `[0, tamanhoPrograma)` pausa e informa conclusão/saída da região de código. Explicitar que isso é regra da interface, não instrução HALT nem detecção de loop.
- Validar intervalo de execução como inteiro entre 10 e 5000 ms, padrão 200 ms. Mostrar erro compreensível; não deixar `NumberFormatException` escapar.
- Manter `Timeline` no thread do JavaFX; cada evento avança um subciclo. Não criar laço de execução ilimitado no thread gráfico.
- Definir “Ciclo completo” como concluir a microinstrução atual: quando no início, avançar quatro subciclos; quando parcialmente executada, somente os restantes. Implementar chamando `executarSubciclo()` até o próximo início. Isso preserva contadores e não repete etapas já executadas.
- Testar ciclo completo a partir de cada uma das quatro posições; resultados iguais à mesma sequência de subciclos individuais.
- Contar macroinstruções concluídas no retorno ao fetch, não na entrada inicial em MPC 0; validar zero antes de executar e cinco após o programa-base.
- Corrigir `ultimoAcessoHit` nas escritas de cache e no `clear()`. No destaque visual, escritas write-through devem indicar propagação aos níveis inferiores mesmo em hit.
- Fechar a janela principal deve parar o Timeline e fechar as janelas auxiliares; não deixar o processo ativo por um inspetor aberto.

Não alterar nesta etapa o protocolo RD/WR ou a latência da memória: isso requer validação arquitetural separada. Os contadores de cache continuam representando chamadas ao modelo de memória, não medições de desempenho real.

#### 5.3 Janela utilizável e primeiro exemplo

Arquivos: `App.java`, `primary.fxml`, `PrimaryController.java`, `examples/soma.asm`.

- Definir título `ProjetoArq — Simulador MIC-1`.
- Dimensionar a janela pela área útil da tela, em vez de exigir 1920 × 1080. Usar 95% da largura/altura útil como limite inicial.
- Preservar o diagrama existente; permitir rolagem quando não couber. Considerar os limites reais das linhas e grupos FXML, não apenas `prefWidth/prefHeight`, para não cortar parte do circuito.
- Manter botões e editor alcançáveis em 1366 × 768 e escala de 125%. Não fazer um redesenho visual completo.
- Adicionar “Carregar exemplo”, que coloca o programa-base no editor e monta usando o mesmo caminho de montagem normal. Interromper execução anterior antes de substituir o programa.
- Mostrar instrução curta: “Carregue o exemplo e clique em Executar”. O exemplo funciona offline e não exige abrir arquivos externos.
- Se uma falha de inicialização/execução for registrada em arquivo, usar uma pasta do usuário, por exemplo `%LOCALAPPDATA%/ProjetoArq/logs`, nunca a pasta instalada. Não acrescentar telemetria ou envio de dados.

Aceite da etapa 5: carregar o exemplo, obter RAM[51]=8, reiniciar e repetir; montar outro programa depois de aquecer caches e executar o novo código; erro de sintaxe não altera a RAM; delay inválido não derruba a interface; execução automática pode ser pausada mesmo em `JUMP 0`.

### Etapa 6 — Automatizar build e distribuição

Arquivos: workflows em `.github/workflows/`.

`ci.yml`:

1. Disparar em pull requests e pushes para `main`.
2. Usar `windows-2022`, checkout, setup do Temurin 21 fixado e cache Maven com chave baseada no POM e Wrapper.
3. Executar `simumic/mvnw.cmd clean verify` no diretório correto.
4. Gerar pacote portátil com o mesmo script local e disponibilizá-lo como artefato do workflow.
5. Falhar quando os testes ou empacotamento falharem. Não publicar Release nessa rotina.

`release-windows.yml`:

1. Disparar em tag `v*` e permitir execução manual para gerar artefatos de teste sem publicar Release.
2. Validar que a tag segue `vmajor.minor.patch` e corresponde exatamente à versão do POM.
3. Preparar JDK e WiX compatíveis, sem depender de WiX já estar instalado na imagem do runner.
4. Rodar `package-windows.ps1 -PackageType all`, que executa testes antes do empacotamento.
5. Gerar checksums SHA-256 do ZIP e do instalador.
6. Para uma tag válida, criar Release em rascunho com ZIP, instalador, checksums e notas. Não publicar automaticamente o rascunho antes da validação manual da interface.
7. Usar `contents: read` por padrão e `contents: write` somente no job de Release. Pull requests não recebem credenciais de publicação.
8. Fixar as actions em revisões oficiais verificadas na implementação; não usar nomes/SHAs inventados nem `@main`.

Não enviar executáveis ao histórico Git. Eles pertencem aos artefatos do workflow e às Releases. A primeira tag e publicação só ocorrem quando a versão estiver pronta para distribuição; implementar o workflow não significa publicar uma versão imediatamente.

Aceite: checkout limpo no runner gera ambos os pacotes. Testes automáticos e build verde não substituem o teste visual num Windows sem Java.

### Etapa 7 — Documentar e validar a entrega real

Arquivos: `README.md`, `scripts/dev.ps1`, `docs/VALIDACAO_WINDOWS.md`.

Ordem do README:

1. Baixar a versão em Releases.
2. Instalar e abrir pelo atalho, ou extrair ZIP e abrir `ProjetoArq.exe`.
3. Carregar exemplo e executar.
4. Como desenvolver: instalar JDK 21 e usar Wrapper ou `scripts/dev.ps1`.
5. Como gerar os pacotes.
6. Limitações, arquitetura-base e créditos do projeto original.

O Dockerfile pode permanecer como alternativa histórica, mas Docker sai do caminho recomendado de uso. Não mudar autores/licença do código original nem inventar uma licença ausente.

`scripts/dev.ps1` deve localizar o JDK, entrar no módulo, chamar o Wrapper com `javafx:run` e informar erro de forma clara. Nenhum script PowerShell precisa ser usado por quem baixar o app pronto.

Registrar em `VALIDACAO_WINDOWS.md`: versão/arquitetura do Windows, versão do pacote, commit, resolução/escala, presença ou ausência de Java e resultado dos testes. Registrar “não executado” quando faltar acesso ao ambiente.

## 6. Checklist de conclusão

- [ ] Clone limpo compila usando JDK 21 e Wrapper.
- [ ] Testes de núcleo e montagem passam.
- [ ] Portátil abre por duplo clique em Windows sem Java/Maven/Docker.
- [ ] Portátil executa offline e em pasta com espaços/acentos.
- [ ] Instalador funciona por usuário e cria atalhos funcionais.
- [ ] Desinstalação e atualização com UUID persistido foram verificadas.
- [ ] Exemplo resulta em AC=8 e RAM[51]=8.
- [ ] RAM e cache abrem e atualizam durante execução.
- [ ] Reiniciar restaura o estado original do programa.
- [ ] Novo programa substitui código anterior sem leitura de cache antiga.
- [ ] Montagem inválida mostra a linha correta e não modifica a máquina.
- [ ] Passos e ciclos completos produzem o mesmo estado e contagem.
- [ ] Loop pode ser pausado e fechar o app encerra o processo.
- [ ] Janela utilizável em 1366 × 768, incluindo teste com escala de 125%.
- [ ] Pacotes gerados no GitHub correspondem à versão/tag e têm checksums.
- [ ] README permite começar a simular sem terminal.

Não definir um limite arbitrário de tamanho do pacote ou tempo de inicialização antes de medir. Registrar tamanho do ZIP/instalador e tempo observado entre duplo clique e janela pronta no ambiente de validação.

## 7. Divisão em commits sugerida

1. `build: padroniza JDK 21 JavaFX e Maven Wrapper`
2. `test: adiciona cobertura básica do simulador`
3. `build: gera distribuição portátil Windows`
4. `build: adiciona instalador Windows com atalhos`
5. `fix: torna montagem e reinício consistentes`
6. `fix: unifica avanço de ciclos e corrige indicadores de cache`
7. `feat: facilita abertura e primeira execução do simulador`
8. `ci: gera pacotes Windows e release em rascunho`
9. `docs: documenta instalação desenvolvimento e validação`

Cada commit deve conter apenas alterações da respectiva etapa e verificações aplicáveis. Push/Release não devem incluir arquivos pessoais ou mudanças anteriores sem relação com o plano.

## 8. Instrução pronta para o modelo implementador

> Implemente o plano em `docs/PLANO_EXECUCAO.md`, começando pela etapa 1 e seguindo a ordem. O projeto existente está em `simumic/`. Preserve a CPU e o microcódigo; mantenha JavaFX e a estrutura modular. O objetivo é entregar um ZIP portátil e um instalador Windows x64 com Java incluído, mais desenvolvimento via Maven Wrapper e automação no GitHub. Execute as verificações de cada etapa antes de avançar. Use os contratos de arquivos, métodos e scripts definidos no plano. Não substitua a aplicação por uma versão web, não adicione frameworks e não amplie a arquitetura do simulador. Preserve alterações preexistentes. Ao concluir, informe arquivos alterados, testes executados, localização dos pacotes e qualquer validação manual pendente. Não afirme que o aplicativo abre em Windows limpo sem realizar esse teste. Prepare a Release em rascunho somente quando houver uma tag de versão autorizada.

## 9. Referências técnicas

- [jpackage: visão geral e requisitos de empacotamento](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html)
- [Opções de jpackage no JDK 21](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [Plugin Maven oficial do OpenJFX: execução e jlink](https://github.com/openjfx/javafx-maven-plugin)
- [Documentação de início do OpenJFX](https://openjfx.io/openjfx-docs/)
- [Notas da série JavaFX 21](https://gluonhq.com/products/javafx/openjfx-21-release-notes/)
- [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/)

As fontes confirmam os mecanismos de runtime incluído, pacotes por plataforma e Wrapper. A combinação concreta deste projeto ainda precisa passar pelas verificações descritas nas etapas; este plano não substitui um build ou teste de instalação.
