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
- `docs/classes.puml`: diagrama UML com atributos, operações e relações.

## Compilar e executar

Na raiz do projeto:

```sh
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin App
```

O arquivo `planejamento.dat` é criado na pasta de execução ao salvar. A aplicação tenta recuperá-lo ao iniciar; na primeira execução, começa com um planejamento vazio. O botão **Carregar caso de referência** preenche o passeio do enunciado, e **Começar do zero** limpa o planejamento atual após confirmação.

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
4. Salve e carregue o planejamento pelos botões superiores. Use **Começar do zero** para limpar os dados atuais sem remover o caso de referência.
5. Para o extra, marque despesas essenciais ou defina prioridade positiva para opcionais e use **Buscar melhor combinação**. O simulador apresenta uma solução sem alterar os dados originais e aceita até dez despesas opcionais.

### Roteiro de cadastro do caso principal

1. Clique em **Começar do zero** e confirme. Cadastre os participantes abaixo na aba **Participantes**:

   | Código | Nome | Orçamento |
   |---:|---|---:|
   | 1 | Ana | R$ 70,00 |
   | 2 | Bruno | R$ 60,00 |
   | 3 | Carla | R$ 70,00 |

2. Na aba **Despesas**, inclua cada linha. Para uma despesa fixa, selecione todos os participantes indicados; para **Por pessoa**, o valor é cobrado de cada participante selecionado. Use Ctrl para selecionar mais de uma pessoa.

   | Descrição | Tipo | Valor | Participantes |
   |---|---|---:|---|
   | Transporte | Fixa | R$ 90,00 | 1, 2 e 3 |
   | Almoço | Por pessoa | R$ 20,00 | 1, 2 e 3 |
   | Atividade recreativa | Fixa | R$ 30,00 | 1 e 3 |
   | Sobremesa | Por pessoa | R$ 10,00 | 1, 2 e 3 |

3. Na aba **Resumo**, confira o total de R$ 210,00: Ana e Carla contribuem com R$ 75,00 cada (R$ 5,00 acima do orçamento) e Bruno com R$ 60,00 (no limite).
4. Clique em **Salvar** para gravar o arquivo `planejamento.dat` no diretório de execução. Ao reiniciar a aplicação nesse mesmo diretório, os dados são carregados automaticamente. Use **Carregar** para recuperar o arquivo manualmente.

## Modelagem e decisões

O código separa modelo, interface, controlador e persistência. As justificativas das coleções e dos vínculos entre despesas e participantes estão na seção de modelagem do relatório técnico detalhado.

O diagrama de classes está no código PlantUML [`docs/classes.puml`](docs/classes.puml) e como imagem legível no [`relatório técnico detalhado`](docs/relatorio-tecnico-detalhado.pdf). O caso de referência também pode ser carregado pela interface; os testes automatizados podem ser reproduzidos com o comando acima.
