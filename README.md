# 🖥️ SimuMIC

## Abrir no Windows

Baixe a versão mais recente em [Releases](https://github.com/0Perceu/ProjetoArq/releases). Há duas opções:

- **Instalador** (`ProjetoArq-*-windows-x64-setup.exe`): instale para sua conta e abra pelo atalho ProjetoArq.
- **Portátil** (`ProjetoArq-*-windows-x64.zip`): extraia o ZIP inteiro e abra `ProjetoArq.exe` dentro da pasta extraída. Mantenha as outras pastas junto do executável.

O aplicativo já inclui Java. Para simular, clique em **Carregar exemplo**, depois em **CICLO COMPLETO**, **SUBCICLO** ou **AUTO RUN**. O exemplo contém cinco instruções e deixa o valor `0008` em AC e no endereço 51 da RAM. **REINICIAR** restaura o programa carregado ao estado inicial.

O simulador pausa quando PC sai da região do programa montado. Isso não é uma instrução HALT; programas em loop podem ser parados pelo botão **PARAR**. O campo ao lado de AUTO RUN aceita intervalos de 10 a 5000 ms. O montador aceita operandos decimais, comentários com `;` e até 4096 palavras.

### Desenvolver e gerar pacotes

Requisitos para desenvolver: Windows x64, JDK 21 com `JAVA_HOME` configurado e internet no primeiro build. O [Maven Wrapper](https://maven.apache.org/tools/wrapper/) baixa a versão fixada do Maven; não é preciso instalar Maven separadamente.

```powershell
cd "<pasta onde você clonou ProjetoArq>"
.\scripts\dev.ps1
```

Também é possível usar `cd simumic` seguido de `.\mvnw.cmd clean verify` e `.\mvnw.cmd javafx:run`. Para gerar a versão portátil:

```powershell
.\scripts\package-windows.ps1 -PackageType portable
```

Para gerar também o instalador, tenha WiX Toolset 3.14.1 (`candle.exe` e `light.exe`) no `PATH` e execute `-PackageType all`. Os arquivos são criados em `dist/`; não são versionados. O GitHub Actions repete o build no Windows e anexa os pacotes ao workflow. Em uma tag `v1.0.0` correspondente ao POM, cria uma Release **em rascunho** para revisão antes de publicar.

O código e as decisões de implementação estão em [docs/PLANO_EXECUCAO.md](docs/PLANO_EXECUCAO.md). Consulte [docs/VALIDACAO_WINDOWS.md](docs/VALIDACAO_WINDOWS.md) para os testes efetivamente realizados e as verificações pendentes.

O código original do simulador e seu histórico foram preservados neste repositório. A documentação original abaixo descreve a arquitetura e a execução pelo Docker, que permanece como alternativa para ambientes Linux com X11.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

> *Um simulador educacional interativo e visual da microarquitetura MIC-1, projetado para facilitar o estudo e a compreensão de datapaths e execução de instruções.*

---

## 🎯 Proposta
A proposta do projeto é criar um simulador que explique, de forma visual e passo a passo, o funcionamento da microarquitetura **MIC-1**, em estrita conformidade com a literatura de *Organização Estruturada de Computadores* de Andrew S. Tanenbaum, 3ªEdição.

## 📚 Estudos e Referências
O embasamento teórico para o desenvolvimento do SimuMIC contou com:
* Literatura clássica sobre Arquitetura de Computadores.
* Pesquisas e referências em websites especializados.
* Análise de arquivos e diagramas focados no funcionamento do MIC-1.
* Utilização de um simulador MIC-1 base para estudos prévios e testes práticos sobre o comportamento do *datapath* e das instruções implementadas.

## 🛠️ Implementação e Tecnologias
O simulador foi inteiramente desenvolvido na linguagem **Java**, utilizando conceitos sólidos de Programação Orientada a Objetos e as seguintes ferramentas:

* **[JavaFX](https://openjfx.io/):** Responsável por toda a interface gráfica e diagramação visual (através de arquivos `.fxml` e *Controllers* para interação em tempo real).
* **[Maven](https://maven.apache.org/):** Utilizado para a estruturação do projeto, *build* automatizado e gerenciamento de dependências.

## ⚙️ Estruturas Implementadas
O motor do simulador modela o hardware com alta fidelidade, exibindo o estado da máquina através de três visões principais (`main`, `ram-view` e `cache-view`). As estruturas contemplam:

- **Registradores:** 16 registradores básicos do caminho de dados + `MAR` e `MBR`.
- **Sinalização:** 2 flags de desvio (N, Z) e status dinâmico da RAM (*Idle*, *Read*, *Write*).
- **Barramentos:** Implementação completa dos barramentos `A`, `B` e `C`.
- **Hierarquia de Memória:** Caches `L1` e `L2` operando com Mapeamento Direto e política de atualização *Write Through*.
- **Microcódigo:** Interpretação nativa das macroinstruções do MIC-1.
- **Ciclo de Clock:** Execução de todos os `MPCs` (Microprogram Counter) dividida rigorosamente em 4 subciclos de clock.

---

## 🐳 Como rodar com Docker (alternativa para Linux/X11)

Não é necessário ter o Java ou o Maven instalados na sua máquina local para testar a aplicação. Basta ter o Docker configurado.

**1. Construir a imagem do simulador:**
```bash
sudo docker build -t simumic-app simumic/
```
**2. Executar o simulador (Linux):**

```bash
sudo docker run -it --rm \
    --net=host \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -v $HOME/.Xauthority:/root/.Xauthority \
    simumic-app
```
*(Nota: Para Windows com WSL2 ou macOS, consulte a documentação sobre partilha do servidor X11/XQuartz).*

## 🚀 Perspectiva de Features (Roadmap)

O desenvolvimento é contínuo. As próximas atualizações focam em elevar a complexidade arquitetural do simulador:

- **Multiprocessamento:** Integrar dois processadores MIC-1 operando em paralelo.

- **Pipelining:** Implementar a arquitetura de pipeline para demonstrar o ganho de performance na sobreposição de instruções.
