package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Planning implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<Integer, Participant> participants = new LinkedHashMap<>();
    private final ArrayList<Expense> expenses = new ArrayList<>();

    public void addParticipant(int code, String name, long budgetCents) {
        validateParticipant(code, name, budgetCents);
        if (participants.containsKey(code)) {
            throw new IllegalArgumentException("Já existe participante com esse código.");
        }
        participants.put(code, new Participant(code, name.trim(), budgetCents));
    }

    public void updateParticipant(int code, String name, long budgetCents) {
        validateParticipant(code, name, budgetCents);
        if (!participants.containsKey(code)) {
            throw new IllegalArgumentException("Participante não encontrado.");
        }
        participants.put(code, new Participant(code, name.trim(), budgetCents));
    }

    public void removeParticipant(int code) {
        if (expenses.stream().anyMatch(expense -> expense.getParticipantCodes().contains(code))) {
            throw new IllegalArgumentException(
                    "Não é possível excluir participante vinculado a uma despesa.");
        }
        if (participants.remove(code) == null) {
            throw new IllegalArgumentException("Participante não encontrado.");
        }
    }

    private static void validateParticipant(int code, String name, long budgetCents) {
        if (code <= 0) {
            throw new IllegalArgumentException("O código deve ser um inteiro positivo.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do participante.");
        }
        if (budgetCents < 0) {
            throw new IllegalArgumentException("O orçamento não pode ser negativo.");
        }
    }

    public String addExpense(String description, long amountCents, boolean perPerson,
                             List<Integer> codes, boolean essential, int priority) {
        String id = UUID.randomUUID().toString();
        Expense expense = createExpense(id, description, amountCents, perPerson, codes,
                essential, priority);
        expenses.add(expense);
        return id;
    }

    public void updateExpense(String id, String description, long amountCents,
                              boolean perPerson, List<Integer> codes,
                              boolean essential, int priority) {
        int index = findExpenseIndex(id);
        Expense updated = createExpense(id, description, amountCents, perPerson, codes,
                essential, priority);
        expenses.set(index, updated);
    }

    private Expense createExpense(String id, String description, long amountCents,
                                  boolean perPerson, List<Integer> codes,
                                  boolean essential, int priority) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da despesa.");
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        if (codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um participante.");
        }
        List<Integer> selected = new ArrayList<>(codes);
        if (selected.stream().anyMatch(code -> code == null || !participants.containsKey(code))) {
            throw new IllegalArgumentException("A despesa contém participante não cadastrado.");
        }
        if (selected.stream().distinct().count() != selected.size()) {
            throw new IllegalArgumentException("Não repita participantes na despesa.");
        }
        if (!essential && priority <= 0) {
            throw new IllegalArgumentException("A prioridade opcional deve ser positiva.");
        }
        if (perPerson) {
            return new PerPersonExpense(id, description.trim(), amountCents, selected,
                    essential, priority);
        }
        return new FixedExpense(id, description.trim(), amountCents, selected,
                essential, priority);
    }

    public void removeExpense(String id) {
        expenses.remove(findExpenseIndex(id));
    }

    private int findExpenseIndex(String id) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Despesa não encontrada.");
    }

    public Map<Integer, Participant> getParticipants() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(participants));
    }

    public List<Expense> getExpenses() {
        return List.copyOf(expenses);
    }

    public Map<Integer, Long> totalsByParticipant() {
        Map<Integer, Long> totals = new LinkedHashMap<>();
        participants.keySet().forEach(code -> totals.put(code, 0L));
        for (Expense expense : expenses) {
            for (int code : expense.getParticipantCodes()) {
                totals.put(code, Math.addExact(totals.get(code), expense.shareFor(code)));
            }
        }
        return Collections.unmodifiableMap(totals);
    }

    public long getTotalCents() {
        long total = 0;
        for (Expense expense : expenses) {
            total = Math.addExact(total, expense.getTotalCents());
        }
        return total;
    }

    public static Planning referencePlanning() {
        Planning planning = new Planning();
        planning.addParticipant(1, "Ana", 7000);
        planning.addParticipant(2, "Bruno", 6000);
        planning.addParticipant(3, "Carla", 7000);
        planning.addExpense("Transporte", 9000, false, List.of(1, 2, 3), true, 0);
        planning.addExpense("Almoço", 2000, true, List.of(1, 2, 3), true, 0);
        planning.addExpense("Atividade recreativa", 3000, false, List.of(1, 3), false, 3);
        planning.addExpense("Sobremesa", 1000, true, List.of(1, 2, 3), false, 1);
        return planning;
    }
}
