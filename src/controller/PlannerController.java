package controller;

import model.AlternativeSimulator;
import model.Planning;
import model.SimulationResult;
import persistence.PlanningRepository;
import view.PlannerFrame;

import java.io.IOException;
import java.util.List;

public final class PlannerController {
    private final PlanningRepository repository;
    private Planning planning = new Planning();
    private PlannerFrame view;

    public PlannerController(PlanningRepository repository) {
        this.repository = repository;
    }

    public void setView(PlannerFrame view) {
        this.view = view;
        view.refresh(planning);
    }

    public Planning getPlanning() {
        return planning;
    }

    public void addParticipant(int code, String name, long budgetCents) {
        planning.addParticipant(code, name, budgetCents);
        refresh();
    }

    public void updateParticipant(int code, String name, long budgetCents) {
        planning.updateParticipant(code, name, budgetCents);
        refresh();
    }

    public void removeParticipant(int code) {
        planning.removeParticipant(code);
        refresh();
    }

    public String addExpense(String description, long amountCents, boolean perPerson,
                             List<Integer> codes, boolean essential, int priority) {
        String id = planning.addExpense(description, amountCents, perPerson, codes,
                essential, priority);
        refresh();
        return id;
    }

    public void updateExpense(String id, String description, long amountCents,
                              boolean perPerson, List<Integer> codes,
                              boolean essential, int priority) {
        planning.updateExpense(id, description, amountCents, perPerson, codes,
                essential, priority);
        refresh();
    }

    public void removeExpense(String id) {
        planning.removeExpense(id);
        refresh();
    }

    public void save() throws IOException {
        repository.save(planning);
    }

    public void load() throws IOException, ClassNotFoundException {
        Planning loaded = repository.load();
        planning = loaded;
        refresh();
    }

    public void loadAtStartup() {
        try {
            load();
        } catch (java.nio.file.NoSuchFileException missingFile) {
            // A first run starts with an empty planning.
        } catch (IOException | ClassNotFoundException exception) {
            view.showError("Não foi possível carregar o arquivo. O planejamento atual foi mantido: "
                    + exception.getMessage());
        }
    }

    public void loadReferencePlanning() {
        planning = Planning.referencePlanning();
        refresh();
    }

    public void startNewPlanning() {
        planning = new Planning();
        refresh();
    }

    public SimulationResult simulate() {
        return new AlternativeSimulator().findBest(planning);
    }

    private void refresh() {
        if (view != null) {
            view.refresh(planning);
        }
    }
}
