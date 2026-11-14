package tests;

import model.AlternativeSimulator;
import model.Expense;
import model.Planning;
import model.SimulationResult;
import controller.PlannerController;
import persistence.FilePlanningRepository;

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
        check(planning.getExpenses().size() == before,
                "Uma entrada inválida alterou o planejamento.");
    }

    private static void expenseDeletionAndBudgetUpdate() {
        Planning planning = Planning.referencePlanning();
        planning.updateParticipant(1, "Ana", 8000);
        check(planning.totalsByParticipant().get(1) == 7500,
                "A alteração do orçamento não deve alterar o custo.");
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
            Planning original = Planning.referencePlanning();
            FilePlanningRepository repository = new FilePlanningRepository(file);
            repository.save(original);
            Planning loaded = repository.load();
            check(loaded.getTotalCents() == 21000
                            && loaded.totalsByParticipant().equals(original.totalsByParticipant())
                            && loaded.getExpenses().size() == 4,
                    "Persistência não recuperou todos os dados.");
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
