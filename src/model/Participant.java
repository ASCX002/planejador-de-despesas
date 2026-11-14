package model;

import java.io.Serializable;

public final class Participant implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int code;
    private final String name;
    private final long budgetCents;

    public Participant(int code, String name, long budgetCents) {
        this.code = code;
        this.name = name;
        this.budgetCents = budgetCents;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getBudgetCents() {
        return budgetCents;
    }
}
