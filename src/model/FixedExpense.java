package model;

import java.util.List;

public final class FixedExpense extends Expense {
    private static final long serialVersionUID = 1L;

    public FixedExpense(String id, String description, long amountCents,
                        List<Integer> participantCodes, boolean essential, int priority) {
        super(id, description, amountCents, participantCodes, essential, priority);
    }

    @Override
    public long shareFor(int participantCode) {
        List<Integer> codes = getParticipantCodes().stream().sorted().toList();
        int index = codes.indexOf(participantCode);
        if (index < 0) {
            return 0;
        }
        long base = getAmountCents() / codes.size();
        long remainder = getAmountCents() % codes.size();
        return base + (index < remainder ? 1 : 0);
    }

    @Override
    public long getTotalCents() {
        return getAmountCents();
    }

    @Override
    public String getType() {
        return "Fixa";
    }
}
