# Cabe no bolso - Planejador de despesas compartilhadas

Aplicação Java Swing para organizar um planejamento compartilhado, dividir despesas fixas ou por pessoa, conferir orçamentos e persistir os dados localmente.

## Requisitos

- JDK 21 ou superior
- Nenhuma dependência externa

## Organização

- `src/model`: entidades, validações, cálculos e simulador.
- `src/view`: interface Swing e apresentação de valores monetários.
- `src/controller`: coordenação das ações entre interface e modelo.
- `src/persistence`: contrato e implementação de persistência por serialização.
- `src/tests`: testes executáveis sem bibliotecas externas.

## Compilar e executar

Na raiz do projeto:

```sh
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin App
```

O arquivo `planejamento.dat` é criado na pasta de execução ao salvar. A aplicação tenta recuperá-lo ao iniciar; na primeira execução, começa com um planejamento vazio. O botão **Carregar caso de referência** preenche o passeio do enunciado.

## Testes

```sh
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -ea -cp bin tests.PlanningTest
```

Os testes verificam o cenário principal (R$ 210,00), rateio de centavos, validações sem alteração de estado, edição de orçamento, exclusão, serialização e os cenários viável e inviável do simulador.

## Como usar

1. Cadastre participantes com código único, nome e orçamento.
2. Cadastre despesas indicando valor, tipo e participantes envolvidos. O rateio fixo distribui centavos restantes pelo menor código.
3. Consulte o resumo para conferir custo total, contribuição individual e orçamento.
4. Salve e carregue o planejamento pelos botões superiores.
5. Para o extra, marque despesas essenciais ou defina prioridade positiva para opcionais e use **Buscar melhor combinação**. O simulador apresenta uma solução sem alterar os dados originais e aceita até dez despesas opcionais.

## Modelagem e decisões

`Planning` mantém os participantes por código e despesas em uma lista; cada despesa guarda os códigos dos participantes vinculados. `Expense` é abstrata, com implementações polimórficas `FixedExpense` e `PerPersonExpense`. Valores são armazenados em centavos inteiros. A camada de apresentação encaminha eventos ao controlador, que aplica as operações ao modelo e atualiza as telas. `PlanningRepository` define o contrato de persistência e `FilePlanningRepository` o implementa.

O diagrama de classes está em [`docs/classes.puml`](docs/classes.puml). O caso de referência é carregado na interface para reprodução manual; os testes automatizados podem ser reproduzidos com o comando acima.
