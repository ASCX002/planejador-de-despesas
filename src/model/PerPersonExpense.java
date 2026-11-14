package model;

import java.util.List;

public final class PerPersonExpense extends Expense {
    private static final long serialVersionUID = 1L;

    public PerPersonExpense(String id, String description, long amountCents,
                            List<Integer> participantCodes, boolean essential, int priority) {
        super(id, description, amountCents, participantCodes, essential, priority);
    }

    @Override
    public long shareFor(int participantCode) {
        return getParticipantCodes().contains(participantCode) ? getAmountCents() : 0;
    }

    @Override
    public long getTotalCents() {
        return Math.multiplyExact(getAmountCents(), getParticipantCodes().size());
    }

    @Override
    public String getType() {
        return "Por pessoa";
    }
}
