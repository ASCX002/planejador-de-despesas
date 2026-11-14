package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AlternativeSimulator {
    public SimulationResult findBest(Planning planning) {
        List<Expense> essential = planning.getExpenses().stream()
                .filter(Expense::isEssential).toList();
        List<Expense> optional = planning.getExpenses().stream()
                .filter(expense -> !expense.isEssential()).toList();
        if (optional.size() > 10) {
            throw new IllegalArgumentException(
                    "O simulador aceita no máximo 10 despesas opcionais.");
        }

        Map<Integer, Long> essentialTotals = totals(planning, essential);
        if (!withinBudgets(planning, essentialTotals)) {
            return new SimulationResult(essential, optional, essentialTotals, 0, false);
        }

        long bestScore = -1;
        List<Expense> bestKept = List.of();
        Map<Integer, Long> bestTotals = Map.of();
        int combinations = 1 << optional.size();
        for (int mask = 0; mask < combinations; mask++) {
            List<Expense> candidate = new ArrayList<>(essential);
            long score = 0;
            for (int index = 0; index < optional.size(); index++) {
                if ((mask & (1 << index)) != 0) {
                    Expense expense = optional.get(index);
                    candidate.add(expense);
                    score = Math.addExact(score, expense.getPriority());
                }
            }
            Map<Integer, Long> totals = totals(planning, candidate);
            if (score > bestScore && withinBudgets(planning, totals)) {
                bestScore = score;
                bestKept = List.copyOf(candidate);
                bestTotals = totals;
            }
        }

        List<Expense> selectedExpenses = bestKept;
        List<Expense> removed = planning.getExpenses().stream()
                .filter(expense -> !selectedExpenses.contains(expense)).toList();
        return new SimulationResult(selectedExpenses, removed, bestTotals, bestScore, true);
    }

    private static Map<Integer, Long> totals(Planning planning, List<Expense> expenses) {
        Map<Integer, Long> totals = new LinkedHashMap<>();
        planning.getParticipants().keySet().forEach(code -> totals.put(code, 0L));
        for (Expense expense : expenses) {
            for (int code : expense.getParticipantCodes()) {
                totals.put(code, Math.addExact(totals.get(code), expense.shareFor(code)));
            }
        }
        return Map.copyOf(totals);
    }

    private static boolean withinBudgets(Planning planning, Map<Integer, Long> totals) {
        return planning.getParticipants().values().stream()
                .allMatch(person -> totals.get(person.getCode()) <= person.getBudgetCents());
    }
}
