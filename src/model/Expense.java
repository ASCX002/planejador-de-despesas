package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String description;
    private final long amountCents;
    private final ArrayList<Integer> participantCodes;
    private final boolean essential;
    private final int priority;

    protected Expense(String id, String description, long amountCents,
                      List<Integer> participantCodes, boolean essential, int priority) {
        this.id = id;
        this.description = description;
        this.amountCents = amountCents;
        this.participantCodes = new ArrayList<>(participantCodes);
        this.essential = essential;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public List<Integer> getParticipantCodes() {
        return Collections.unmodifiableList(participantCodes);
    }

    public boolean isEssential() {
        return essential;
    }

    public int getPriority() {
        return priority;
    }

    public abstract long shareFor(int participantCode);

    public abstract long getTotalCents();

    public abstract String getType();
}
