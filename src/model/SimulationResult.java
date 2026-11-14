package model;

import java.util.List;
import java.util.Map;

public record SimulationResult(List<Expense> kept, List<Expense> removed,
                               Map<Integer, Long> totals, long priorityScore,
                               boolean solutionFound) {
}
