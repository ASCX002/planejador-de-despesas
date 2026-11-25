package tests;

import model.AlternativeSimulator;
import model.Expense;
import model.Planning;
import model.SimulationResult;
import controller.PlannerController;
import persistence.FilePlanningRepository;
import view.Money;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class PlanningTest {
    public static void main(String[] args) throws Exception {
        referenceCase();
        centDistribution();
        validationDoesNotChangeState();
        expenseDeletionAndBudgetUpdate();
        startNewPlanning();
        persistenceRoundTrip();
        failedLoadPreservesPlanning();
        simulatorCases();
        System.out.println("Todos os testes passaram.");
    }

    private static void referenceCase() {
        Planning planning = Planning.referencePlanning();
        Map<Integer, Long> totals = planning.totalsByParticipant();
        check(totals.equals(Map.of(1, 7500L, 2, 6000L, 3, 7500L)),
                "Totais do caso de referência incorretos.");
        check(planning.getTotalCents() == 21000, "Custo total deve ser R$ 210,00.");
        check(totals.get(1) - planning.getParticipants().get(1).getBudgetCents() == 500
                        && totals.get(3) - planning.getParticipants().get(3).getBudgetCents() == 500
                        && totals.get(2).equals(planning.getParticipants().get(2).getBudgetCents()),
                "Ana e Carla devem exceder o orçamento em R$ 5,00; Bruno deve ficar no limite.");
    }

    private static void centDistribution() {
        Planning planning = new Planning();
        planning.addParticipant(3, "Carla", 0);
        planning.addParticipant(1, "Ana", 0);
        planning.addParticipant(2, "Bruno", 0);
        planning.addExpense("Rateio", 10000, false, List.of(3, 1, 2), true, 0);
        Expense expense = planning.getExpenses().getFirst();
        check(expense.shareFor(1) == 3334 && expense.shareFor(2) == 3333
                && expense.shareFor(3) == 3333, "Rateio de centavos incorreto.");
    }

    private static void validationDoesNotChangeState() {
        Planning planning = Planning.referencePlanning();
        int before = planning.getExpenses().size();
        expectFailure(() -> planning.addExpense("Inválida", -1, false,
                List.of(1), true, 0));
        expectFailure(() -> planning.addExpense("Sem participantes", 100, false,
                List.of(), true, 0));
        expectFailure(() -> planning.addExpense("", 100, false,
                List.of(1), true, 0));
        expectFailure(() -> planning.addParticipant(1, "Código repetido", 10000));
        expectFailure(() -> planning.addParticipant(4, " ", 10000));
        expectFailure(() -> Money.parseCents(""));
        check(planning.getExpenses().size() == before,
                "Uma entrada inválida alterou o planejamento.");
        check(planning.getParticipants().size() == 3,
                "Um participante inválido ou com código duplicado alterou o planejamento.");
    }

    private static void expenseDeletionAndBudgetUpdate() {
        Planning planning = Planning.referencePlanning();
        planning.updateParticipant(1, "Ana", 8000);
        check(planning.getParticipants().get(1).getBudgetCents() == 8000
                        && planning.totalsByParticipant().get(1) == 7500,
                "A alteração do orçamento não deve alterar o custo e deve atualizar o limite.");
        String activityId = planning.getExpenses().stream()
                .filter(expense -> expense.getDescription().equals("Atividade recreativa"))
                .findFirst().orElseThrow().getId();
        planning.removeExpense(activityId);
        check(planning.getTotalCents() == 18000
                        && planning.totalsByParticipant().equals(Map.of(1, 6000L, 2, 6000L, 3, 6000L)),
                "Totais após excluir despesa incorretos.");
        expectFailure(() -> planning.removeParticipant(1));
    }

    private static void persistenceRoundTrip() throws Exception {
        Path file = Files.createTempFile("planejamento-test-", ".dat");
        try {
            PlannerController beforeRestart = new PlannerController(
                    new FilePlanningRepository(file));
            beforeRestart.loadReferencePlanning();
            beforeRestart.save();

            PlannerController afterRestart = new PlannerController(
                    new FilePlanningRepository(file));
            afterRestart.load();
            Planning loaded = afterRestart.getPlanning();
            check(loaded.getTotalCents() == 21000
                            && loaded.totalsByParticipant().equals(
                                    beforeRestart.getPlanning().totalsByParticipant())
                            && loaded.getParticipants().size() == 3
                            && loaded.getExpenses().size() == 4
                            && loaded.getExpenses().stream()
                                    .allMatch(expense -> !expense.getParticipantCodes().isEmpty()),
                    "Salvar e recriar o controlador não recuperou o planejamento completo.");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void startNewPlanning() throws Exception {
        Path file = Files.createTempFile("planejamento-novo-", ".dat");
        try {
            PlannerController controller = new PlannerController(new FilePlanningRepository(file));
            controller.loadReferencePlanning();
            controller.startNewPlanning();
            check(controller.getPlanning().getParticipants().isEmpty()
                            && controller.getPlanning().getExpenses().isEmpty()
                            && controller.getPlanning().getTotalCents() == 0,
                    "Começar do zero deve limpar participantes e despesas.");

            controller.loadReferencePlanning();
            check(controller.getPlanning().getTotalCents() == 21000,
                    "O carregamento do caso de referência deve continuar disponível.");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void failedLoadPreservesPlanning() throws Exception {
        Path file = Files.createTempFile("planejamento-invalid-", ".dat");
        try {
            Files.writeString(file, "arquivo inválido");
            PlannerController controller = new PlannerController(
                    new FilePlanningRepository(file));
            controller.loadReferencePlanning();
            boolean failed = false;
            try {
                controller.load();
            } catch (IOException | ClassNotFoundException expected) {
                failed = true;
            }
            check(failed, "O arquivo inválido deveria ser recusado.");
            check(controller.getPlanning().getTotalCents() == 21000,
                    "Falha ao carregar substituiu o planejamento em uso.");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void simulatorCases() {
        Planning planning = Planning.referencePlanning();
        SimulationResult result = new AlternativeSimulator().findBest(planning);
        check(result.solutionFound() && result.priorityScore() == 3,
                "O simulador não escolheu a maior prioridade viável.");
        check(result.kept().stream().anyMatch(e -> e.getDescription().equals("Atividade recreativa"))
                        && result.removed().stream().anyMatch(e -> e.getDescription().equals("Sobremesa"))
                        && result.totals().equals(Map.of(1, 6500L, 2, 5000L, 3, 6500L)),
                "Resultado do simulador diferente do caso extra.");

        Planning noSolution = Planning.referencePlanning();
        noSolution.updateParticipant(1, "Ana", 4000);
        SimulationResult impossible = new AlternativeSimulator().findBest(noSolution);
        check(!impossible.solutionFound() && impossible.totals().get(1) == 5000,
                "O caso sem solução não foi identificado corretamente.");
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Era esperada uma validação inválida.");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
