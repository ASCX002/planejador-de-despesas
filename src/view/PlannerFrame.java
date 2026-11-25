package view;

import controller.PlannerController;
import model.Expense;
import model.Participant;
import model.Planning;
import model.SimulationResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PlannerFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient PlannerController controller;
    private final DefaultTableModel participantModel = tableModel(
            "Código", "Nome", "Orçamento", "Total", "Situação");
    private final DefaultTableModel expenseModel = tableModel(
            "ID", "Descrição", "Tipo", "Valor", "Participantes", "Classificação");
    private final JTable participantTable = new JTable(participantModel);
    private final JTable expenseTable = new JTable(expenseModel);
    private final JTextField codeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField budgetField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JTextField amountField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"Fixa", "Por pessoa"});
    private final JList<String> participantList = new JList<>();
    private final JCheckBox essentialCheck = new JCheckBox("Essencial");
    private final JTextField priorityField = new JTextField("1");
    private final JTextArea summaryArea = new JTextArea();
    private final JTextArea simulatorArea = new JTextArea();
    private String selectedExpenseId;

    public PlannerFrame(PlannerController controller) {
        super("Cabe no bolso - Planejador de despesas");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(createToolbar(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Participantes", createParticipantPanel());
        tabs.addTab("Despesas", createExpensePanel());
        tabs.addTab("Resumo", createSummaryPanel());
        tabs.addTab("Simulador opcional", createSimulatorPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAction(toolbar, "Carregar caso de referência", () -> {
            if (confirm("Substituir o planejamento atual pelo caso de referência?")) {
                controller.loadReferencePlanning();
            }
        });
        addAction(toolbar, "Começar do zero", () -> {
            if (confirm("Apagar participantes e despesas do planejamento atual e começar do zero?")) {
                controller.startNewPlanning();
                clearParticipantFields();
                clearExpenseFields();
                simulatorArea.setText("");
            }
        });
        addAction(toolbar, "Salvar", () -> runAction(() -> {
            controller.save();
            message("Planejamento salvo em planejamento.dat.");
        }));
        addAction(toolbar, "Carregar", () -> runAction(() -> {
            controller.load();
            message("Planejamento carregado.");
        }));
        return toolbar;
    }

    private JPanel createParticipantPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 4, 6, 4));
        form.setBorder(BorderFactory.createTitledBorder("Cadastro / edição"));
        form.add(new JLabel("Código"));
        form.add(new JLabel("Nome"));
        form.add(new JLabel("Orçamento (R$)"));
        form.add(new JLabel(""));
        form.add(codeField);
        form.add(nameField);
        form.add(budgetField);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        addAction(buttons, "Adicionar", () -> runAction(() -> {
            controller.addParticipant(Integer.parseInt(codeField.getText().trim()),
                    nameField.getText(), Money.parseCents(budgetField.getText()));
            clearParticipantFields();
        }));
        addAction(buttons, "Atualizar selecionado", () -> runAction(() -> {
            controller.updateParticipant(selectedParticipantCode(),
                    nameField.getText(), Money.parseCents(budgetField.getText()));
            clearParticipantFields();
        }));
        addAction(buttons, "Excluir selecionado", () -> runAction(() ->
                controller.removeParticipant(selectedParticipantCode())));
        form.add(buttons);
        root.add(form, BorderLayout.NORTH);
        participantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        participantTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && participantTable.getSelectedRow() >= 0) {
                int row = participantTable.convertRowIndexToModel(participantTable.getSelectedRow());
                codeField.setText(String.valueOf(participantModel.getValueAt(row, 0)));
                nameField.setText(String.valueOf(participantModel.getValueAt(row, 1)));
                Participant participant = controller.getPlanning().getParticipants()
                        .get(Integer.parseInt(codeField.getText()));
                budgetField.setText(Money.editValue(participant.getBudgetCents()));
                codeField.setEditable(false);
            }
        });
        root.add(new JScrollPane(participantTable), BorderLayout.CENTER);
        return root;
    }

    private JPanel createExpensePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(0, 4, 6, 4));
        form.setBorder(BorderFactory.createTitledBorder("Cadastro / edição de despesa"));
        form.add(new JLabel("Descrição"));
        form.add(new JLabel("Valor (R$)"));
        form.add(new JLabel("Tipo"));
        form.add(new JLabel("Prioridade opcional"));
        form.add(descriptionField);
        form.add(amountField);
        form.add(typeCombo);
        form.add(priorityField);
        form.add(new JLabel("Participantes (Ctrl/Shift para vários)"));
        form.add(new JLabel(""));
        form.add(new JLabel(""));
        form.add(new JLabel(""));
        participantList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane participantScroll = new JScrollPane(participantList);
        participantScroll.setPreferredSize(new java.awt.Dimension(220, 76));
        form.add(participantScroll);
        form.add(essentialCheck);
        form.add(new JLabel(""));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAction(buttons, "Adicionar despesa", () -> runAction(() -> {
            controller.addExpense(descriptionField.getText(), Money.parseCents(amountField.getText()),
                    typeCombo.getSelectedIndex() == 1, selectedCodes(),
                    essentialCheck.isSelected(), parsePriority());
            clearExpenseFields();
        }));
        addAction(buttons, "Atualizar selecionada", () -> runAction(() -> {
            if (selectedExpenseId == null) {
                throw new IllegalArgumentException("Selecione uma despesa na tabela.");
            }
            controller.updateExpense(selectedExpenseId, descriptionField.getText(),
                    Money.parseCents(amountField.getText()), typeCombo.getSelectedIndex() == 1,
                    selectedCodes(), essentialCheck.isSelected(), parsePriority());
            clearExpenseFields();
        }));
        addAction(buttons, "Excluir selecionada", () -> runAction(() -> {
            if (selectedExpenseId == null) {
                throw new IllegalArgumentException("Selecione uma despesa na tabela.");
            }
            controller.removeExpense(selectedExpenseId);
            clearExpenseFields();
        }));
        form.add(buttons);
        root.add(form, BorderLayout.NORTH);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && expenseTable.getSelectedRow() >= 0) {
                int row = expenseTable.convertRowIndexToModel(expenseTable.getSelectedRow());
                selectedExpenseId = String.valueOf(expenseModel.getValueAt(row, 0));
                Expense selected = controller.getPlanning().getExpenses().stream()
                        .filter(expense -> expense.getId().equals(selectedExpenseId))
                        .findFirst().orElse(null);
                if (selected != null) {
                    descriptionField.setText(selected.getDescription());
                    amountField.setText(Money.editValue(selected.getAmountCents()));
                    typeCombo.setSelectedIndex(selected.getType().equals("Por pessoa") ? 1 : 0);
                    essentialCheck.setSelected(selected.isEssential());
                    priorityField.setText(String.valueOf(selected.getPriority()));
                    participantList.clearSelection();
                    for (int index = 0; index < participantList.getModel().getSize(); index++) {
                        String item = participantList.getModel().getElementAt(index);
                        int code = Integer.parseInt(item.substring(0, item.indexOf(" - ")));
                        if (selected.getParticipantCodes().contains(code)) {
                            participantList.addSelectionInterval(index, index);
                        }
                    }
                }
            }
        });
        root.add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        return root;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        summaryArea.setEditable(false);
        summaryArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSimulatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addAction(actions, "Buscar melhor combinação", () -> runAction(() -> {
            SimulationResult result = controller.simulate();
            simulatorArea.setText(formatSimulation(result));
        }));
        panel.add(actions, BorderLayout.NORTH);
        simulatorArea.setEditable(false);
        panel.add(new JScrollPane(simulatorArea), BorderLayout.CENTER);
        return panel;
    }

    public void refresh(Planning planning) {
        selectedExpenseId = null;
        participantModel.setRowCount(0);
        expenseModel.setRowCount(0);
        Map<Integer, Long> totals = planning.totalsByParticipant();
        for (Participant participant : planning.getParticipants().values()) {
            long total = totals.get(participant.getCode());
            String status = total > participant.getBudgetCents()
                    ? "Excedido em " + Money.format(total - participant.getBudgetCents())
                    : "Dentro do orçamento";
            participantModel.addRow(new Object[]{participant.getCode(), participant.getName(),
                    Money.format(participant.getBudgetCents()), Money.format(total), status});
        }
        participantList.setListData(planning.getParticipants().values().stream()
                .map(person -> person.getCode() + " - " + person.getName())
                .toArray(String[]::new));
        for (Expense expense : planning.getExpenses()) {
            String people = expense.getParticipantCodes().stream().sorted()
                    .map(String::valueOf).reduce((left, right) -> left + ", " + right).orElse("");
            String classification = expense.isEssential()
                    ? "Essencial" : "Opcional (prioridade " + expense.getPriority() + ")";
            expenseModel.addRow(new Object[]{expense.getId(),
                    expense.getDescription(), expense.getType(),
                    Money.format(expense.getAmountCents()), people, classification});
        }
        updateSummary(planning, totals);
    }

    public void showError(String text) {
        JOptionPane.showMessageDialog(this, text, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void updateSummary(Planning planning, Map<Integer, Long> totals) {
        StringBuilder text = new StringBuilder("DESPESAS\n");
        for (Expense expense : planning.getExpenses()) {
            text.append("- ").append(expense.getDescription()).append(" (")
                    .append(expense.getType()).append("): ")
                    .append(Money.format(expense.getAmountCents()))
                    .append(" | rateio total: ").append(Money.format(expense.getTotalCents()))
                    .append('\n');
        }
        text.append("\nCUSTO TOTAL: ").append(Money.format(planning.getTotalCents()))
                .append("\n\nPARTICIPANTES\n");
        for (Participant person : planning.getParticipants().values()) {
            long total = totals.get(person.getCode());
            long difference = total - person.getBudgetCents();
            text.append(person.getCode()).append(" - ").append(person.getName())
                    .append(": total ").append(Money.format(total))
                    .append(" | orçamento ").append(Money.format(person.getBudgetCents()))
                    .append(" | ");
            if (difference > 0) {
                text.append("excedido em ").append(Money.format(difference));
            } else {
                text.append("dentro do orçamento (saldo ")
                        .append(Money.format(-difference)).append(')');
            }
            text.append('\n');
            for (Expense expense : planning.getExpenses()) {
                long share = expense.shareFor(person.getCode());
                if (share > 0) {
                    text.append("   ").append(expense.getDescription()).append(": ")
                            .append(Money.format(share)).append('\n');
                }
            }
        }
        summaryArea.setText(text.toString());
    }

    private String formatSimulation(SimulationResult result) {
        StringBuilder text = new StringBuilder();
        if (!result.solutionFound()) {
            text.append("Não há solução: mesmo mantendo apenas as despesas essenciais, ")
                    .append("algum orçamento é excedido.\n\n");
            appendExpenses(text, "Despesas essenciais", result.kept());
            for (Map.Entry<Integer, Long> entry : result.totals().entrySet()) {
                Participant person = controller.getPlanning().getParticipants().get(entry.getKey());
                long excess = entry.getValue() - person.getBudgetCents();
                if (excess > 0) {
                    text.append(person.getName()).append(" excede o orçamento em ")
                            .append(Money.format(excess)).append('\n');
                }
            }
            return text.toString();
        }
        text.append("Prioridade total máxima: ").append(result.priorityScore()).append("\n\n");
        appendExpenses(text, "Mantidas", result.kept());
        appendExpenses(text, "Retiradas", result.removed());
        text.append("\nNOVOS TOTAIS\n");
        for (Map.Entry<Integer, Long> entry : result.totals().entrySet()) {
            Participant person = controller.getPlanning().getParticipants().get(entry.getKey());
            text.append(person.getName()).append(": ").append(Money.format(entry.getValue()))
                    .append(" / orçamento ").append(Money.format(person.getBudgetCents()))
                    .append('\n');
        }
        return text.toString();
    }

    private static void appendExpenses(StringBuilder text, String title, List<Expense> expenses) {
        text.append(title).append(":\n");
        if (expenses.isEmpty()) {
            text.append("  (nenhuma)\n");
        } else {
            expenses.forEach(expense -> text.append("  - ")
                    .append(expense.getDescription()).append('\n'));
        }
    }

    private int selectedParticipantCode() {
        if (participantTable.getSelectedRow() < 0) {
            throw new IllegalArgumentException("Selecione um participante na tabela.");
        }
        int row = participantTable.convertRowIndexToModel(participantTable.getSelectedRow());
        return Integer.parseInt(String.valueOf(participantModel.getValueAt(row, 0)));
    }

    private List<Integer> selectedCodes() {
        List<Integer> codes = new ArrayList<>();
        for (String selected : participantList.getSelectedValuesList()) {
            codes.add(Integer.parseInt(selected.substring(0, selected.indexOf(" - "))));
        }
        return codes;
    }

    private int parsePriority() {
        try {
            return Integer.parseInt(priorityField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("A prioridade deve ser um inteiro positivo.", exception);
        }
    }

    private void clearParticipantFields() {
        codeField.setText("");
        nameField.setText("");
        budgetField.setText("");
        codeField.setEditable(true);
        participantTable.clearSelection();
    }

    private void clearExpenseFields() {
        selectedExpenseId = null;
        descriptionField.setText("");
        amountField.setText("");
        typeCombo.setSelectedIndex(0);
        participantList.clearSelection();
        essentialCheck.setSelected(false);
        priorityField.setText("1");
        expenseTable.clearSelection();
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void addAction(JPanel panel, String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private void runAction(CheckedAction action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showError(exception.getMessage() == null ? "Operação não concluída." :
                    exception.getMessage());
        } catch (IOException | ClassNotFoundException exception) {
            showError(exception.getMessage() == null ? "Operação não concluída." :
                    exception.getMessage());
        }
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text, "Planejador", JOptionPane.INFORMATION_MESSAGE);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws IOException, ClassNotFoundException;
    }
}
