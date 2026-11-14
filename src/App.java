import controller.PlannerController;
import persistence.FilePlanningRepository;
import view.PlannerFrame;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlannerController controller = new PlannerController(
                    new FilePlanningRepository(Path.of("planejamento.dat")));
            PlannerFrame frame = new PlannerFrame(controller);
            controller.setView(frame);
            frame.setVisible(true);
            controller.loadAtStartup();
        });
    }
}
