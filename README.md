# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** Cultiva

| Integrante      | RM     | Turma |
| --------------- | ------ | ----- |
| Gabriel Fidalgo | 563213 | 2CCPY |
| Pedro Lima      | 565461 | 2CCPY |
| Gustavo Maia    | 562240 | 2CCPY |
| Gustavo Rossi   | 566075 | 2CCPY |

| Campo                              | Resultado               |
| ---------------------------------- | ----------------------- |
| **Total de bugs corrigidos**       | **12 / 12**             |
| **Total de ajustes de Clean Code** | **6 / 6**               |
| **Total de testes novos escritos** | **6 / 6**               |
| **Suíte final (JUnit 5)**          | **26 testes, 0 falhas** |

## Parte 1 — Bugs encontrados

| #     | Sintoma observado (o que fiz/vi)                                                             | Causa raiz (arquivo e linha aproximada)                                                          | Correção aplicada                                                                                  | Conceito da disciplina                                    |
| ----- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| bug01 | Duas chamadas a `getInstancia()` devolviam objetos diferentes e `proximo()` recomeçava em 1. | `model/GeradorProtocolo.java:16-24` criava um objeto sem guardar em `instancia`.                 | Guardar a instância e sincronizar acesso e incremento para preservar sequência sob concorrência.   | Singleton; estado compartilhado; sincronização.           |
| bug02 | Pedir `TOSA` pela factory produzia um `Banho`.                                               | `factory/AtendimentoFactory.java:18` apontava o case para a subclasse errada.                    | Instanciar `Tosa` no case `TOSA`.                                                                  | Factory; polimorfismo.                                    |
| bug03 | A consulta criada tinha `petNome`, porte e tutor nulos.                                      | `model/ConsultaVeterinaria.java:16-17` chamava `super()` sem os argumentos recebidos.            | Passar todos os campos ao construtor de `Atendimento`.                                             | Herança; construtores.                                    |
| bug04 | `comPet("Rex", ...)` deixava o nome nulo no objeto construído.                               | `builder/AtendimentoBuilder.java:23-25` fazia autoatribuição do parâmetro em vez do campo.       | Usar `this.petNome = petNome`.                                                                     | Escopo; `this`; Builder.                                  |
| bug05 | O Builder aceitava pet sem nome ou sem porte.                                                | `builder/AtendimentoBuilder.java:39-46` chamava a factory sem validar campos obrigatórios.       | Recusar valores nulos ou em branco antes de construir.                                             | Invariantes de objeto; validação; exceções unchecked.     |
| bug06 | Dois objetos com o mesmo pet e horário eram aceitos como agendamentos distintos.             | `service/AgendaService.java:29` comparava `String` e `LocalDateTime` com `==`.                   | Comparar valores com `.equals()` e manter a verificação de status `AGENDADO`.                      | Igualdade de objetos; regras de agenda.                   |
| bug07 | Buscar ID inexistente devolvia `null`, e os chamadores podiam quebrar depois.                | `service/AgendaService.java:39-41` capturava toda `Exception` e ocultava a exceção específica.   | Deixar `AtendimentoNaoEncontradoException` sair de `orElseThrow`.                                  | Tratamento de exceções; fail fast.                        |
| bug08 | Banho pequeno custava R$ 100 e grande R$ 60.                                                 | `model/Banho.java:26-32` tinha os valores dos portes pequeno e grande invertidos.                | Aplicar R$ 60, R$ 80 e R$ 100 para pequeno, médio e grande.                                        | Encapsulamento da regra de preço; testes de limite.       |
| bug09 | `getDuracaoMinutos()` em uma referência `Atendimento` para `Tosa` retornava 30.              | `model/Tosa.java:40-42` recebia `String porte`, sobrecarregando em vez de sobrescrever o método. | Corrigir a assinatura e adicionar `@Override`; duração de 60 minutos.                              | Sobrescrita vs. sobrecarga; polimorfismo.                 |
| bug10 | Agendamento no passado alcançava o repository.                                               | `service/AgendaService.java:23-27` não validava a data antes da consulta.                        | Recusar data nula, presente ou passada com `IllegalArgumentException` antes de acessar o banco.    | Validação da regra de negócio; fronteira de persistência. |
| bug11 | Um atendimento concluído podia virar cancelado.                                              | `model/Atendimento.java:63-68` alterava o status sem conferir o estado anterior.                 | Permitir `cancelar()` apenas em `AGENDADO`, lançando `StatusInvalidoException` nos outros estados. | Máquina de estados; encapsulamento.                       |
| bug12 | Um atendimento novo não tinha ID definido para `repository.save()`.                          | `model/Atendimento.java:14-16` declarava `@Id` sem estratégia de geração.                        | Adicionar `@GeneratedValue(strategy = IDENTITY)`; gravação confirmada em H2 com ID gerado.         | JPA; identidade de entidade; revisão do fluxo completo.   |

Os bugs 08 a 11 foram revelados pelos testes novos. O bug 12 apareceu na revisão da persistência e foi confirmado com Spring Boot e H2 em memória.

## Parte 2 — Ajustes de Clean Code

| #       | Onde estava                | Princípio/boa prática violado                                                                                     | O que mudou                                                                            |
| ------- | -------------------------- | ----------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| clean01 | `AtendimentoFactory.criar` | Parâmetros `p`, `t`, `n`, `po`, `tu`, `d` escondiam o significado dos dados.                                      | Nomes completos (`protocolo`, `tipo`, `petNome`, `petPorte`, `tutorNome`, `dataHora`). |
| clean02 | `AgendaService`            | Injeção em campo ocultava a dependência e permitia objeto parcialmente inicializado.                              | `repository` final e injeção pelo construtor.                                          |
| clean03 | `AtendimentoController`    | Mesmo problema de injeção em campo para o service.                                                                | `service` final e injeção pelo construtor.                                             |
| clean04 | `GeradorProtocolo`         | `System.out.println` no construtor do model causava efeito colateral sem valor de negócio.                        | Remover a impressão.                                                                   |
| clean05 | `AgendaService.agendar`    | `System.out.println` misturava recibo de console com regra de agendamento.                                        | Retornar diretamente o objeto salvo; a API fornece a resposta HTTP.                    |
| clean06 | `AtendimentoController`    | Método privado de desconto e comentários de funcionalidades futuras não eram usados nem faziam parte do contrato. | Remover código morto e especulativo.                                                   |

## Parte 3 — Testes novos (regras que estavam sem cobertura)

| #       | Teste escrito (classe.método)                                                           | Regra coberta                                            | Resultado ao escrever           |
| ------- | --------------------------------------------------------------------------------------- | -------------------------------------------------------- | ------------------------------- |
| teste01 | `BanhoPrecoContratoTest.deveCobrarPrecoPorPorteQuandoForBanho`                          | Banho custa R$ 60/80/100 conforme o porte.               | **Vermelho:** revelou bug08.    |
| teste02 | `TosaDuracaoContratoTest.deveDurar60MinutosQuandoForTosa`                               | Tosa dura 60 minutos mesmo via referência `Atendimento`. | **Vermelho:** revelou bug09.    |
| teste03 | `AgendaServiceRegrasContratoTest.deveRecusarAgendamentoQuandoDataEstaNoPassado`         | Data passada é recusada sem consultar o repository.      | **Vermelho:** revelou bug10.    |
| teste04 | `AgendaServiceRegrasContratoTest.deveRecusarCancelamentoQuandoAtendimentoEstaConcluido` | Atendimento concluído não pode ser cancelado nem salvo.  | **Vermelho:** revelou bug11.    |
| teste05 | `AgendaServiceRegrasContratoTest.deveCancelarQuandoAtendimentoEstaAgendado`             | Atendimento agendado pode ser cancelado e salvo.         | **Verde:** regra já funcionava. |
| teste06 | `ConsultaPrecoContratoTest.deveCobrarPrecoFixoQuandoForConsulta`                        | Consulta custa R$ 150 nos três portes.                   | **Verde:** regra já funcionava. |

Os 20 métodos originais de `src/test/java` foram preservados. Os seis métodos acima foram adicionados em classes novas.

## Parte 4 — Perguntas de reflexão

### 1. A suíte como contrato (Aula 15)

A suíte entregue apontou o comportamento esperado sem depender de abrir a API.  
A falha do Builder, por exemplo, mostrava que `"Rex"` era esperado e o nome vinha nulo.  
Seguimos o caminho `comPet` → campo do Builder → factory, onde estava a autoatribuição.  
O teste de Tosa da factory distinguia uma subclasse incorreta de um valor de preço incorreto.  
Após cada correção, rodamos os 20 testes originais e os seis novos para detectar regressões.  
O curl depende de API, banco, rede e inspeção manual da resposta; JUnit isola e repete a mesma regra rapidamente.

### 2. Mock e injeção de dependência (Aulas 13 a 15)

Em produção, o Spring cria `AtendimentoRepository` e passa a dependência ao construtor de `AgendaService`.  
Nos testes, `@Mock` cria um repository falso e `@InjectMocks` o passa ao mesmo construtor.  
Assim, `AgendaService.agendar()` executa a regra real, mas `findByPetNome` e `save` usam respostas configuradas com `when`.  
O teste de conflito usa `verify(repository, never()).save(any())` para provar que nada foi persistido.  
O teste de data passada usa `verifyNoInteractions(repository)` para provar que a validação vem antes da consulta.  
Como o Mockito substitui a dependência, não é preciso subir o container Spring nem acessar Oracle.

### 3. `==` vs `.equals()` (Aula 7)

`==` verifica se duas referências apontam para o mesmo objeto, não se representam o mesmo pet ou horário.  
O teste cria outro `LocalDateTime` com o mesmo valor e a comparação antiga não detectava o conflito.  
Com literais como `"Rex"`, o pool de Strings pode reutilizar a referência e fazer `==` parecer correto por acaso.  
Dados de requisições reais podem criar Strings distintas com o mesmo conteúdo.  
Em `AgendaService.agendar`, `.equals()` compara o valor dos nomes e dos horários.  
A condição ainda exige status `AGENDADO`, portanto um atendimento cancelado não bloqueia outro horário igual.

### 4. Sobrescrita vs sobrecarga (Aula 7)

`Atendimento.getDuracaoMinutos()` não recebe argumentos e devolve 30 por padrão.  
`Tosa` declarava `getDuracaoMinutos(String porte)`, um método novo com outra assinatura: sobrecarga.  
Ao chamar pelo tipo `Atendimento`, Java escolhia o método sem parâmetro herdado, então a Tosa parecia durar 30 minutos.  
A correção removeu o parâmetro e manteve o retorno 60.  
`@Override` agora obriga o compilador a confirmar que a assinatura realmente sobrescreve a da superclasse.  
Esse teste usa uma variável `Atendimento` de propósito, reproduzindo o polimorfismo do endpoint `/resumo`.

### 5. Singleton manual vs bean do Spring (Aula 14)

`GeradorProtocolo` deve ter uma única instância e um contador compartilhado em toda a aplicação.  
O método antigo chamava `new GeradorProtocolo()` sem preencher o campo estático `instancia`.  
Por isso, cada `proximo()` podia retornar 1, em vez de formar uma sequência global.  
Agora `getInstancia()` guarda o objeto, e o acesso sincronizado evita duas instâncias ou incrementos perdidos entre threads.  
`AgendaService` é um bean `@Service`; o container Spring controla seu ciclo de vida e, por padrão, mantém uma instância.  
Nos testes, o Mockito o cria por `@InjectMocks`, sem depender desse ciclo de vida do Spring.

### 6. Cobertura de testes: onde parar? (Aula 15)

Os testes de cancelamento agendado e preço fixo da consulta ficaram verdes logo de início.  
Mesmo assim, devem permanecer: protegem regras explícitas caso uma mudança futura as quebre.  
Os outros quatro testes revelaram preço invertido, duração sobrecarregada, data passada aceita e cancelamento inválido.  
Em prazo curto, eu priorizaria regras de negócio e caminhos de erro com impacto, como não salvar em conflito ou status inválido.  
Também manteria caminhos felizes essenciais, pois um sistema que só recusa tudo poderia passar em testes negativos.  
Não buscaria 100% de linhas por número: este conjunto cobre o contrato relevante, e a gravação JPA foi verificada em H2.

## Parte 5 — Execução e verificação

Requisito: JDK 17 ou superior. Rode `mvn test` na raiz; os testes unitários usam Mockito e dispensam banco e rede.

Nesta execução, o compilador JDK 17 e o JUnit 5 Console rodaram **26 testes com 0 falhas**. O Maven não conseguiu baixar artefatos novos por restrição de rede deste ambiente; por isso a suíte foi executada diretamente com as mesmas dependências JUnit e Mockito, sem alterar o `pom.xml`. Um smoke test separado iniciou o contexto Spring Boot com H2, criou a tabela `atendimentos` e confirmou `repository.save()` com ID gerado.
