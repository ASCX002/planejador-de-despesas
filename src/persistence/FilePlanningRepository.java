package persistence;

import model.Planning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FilePlanningRepository implements PlanningRepository {
    private final Path file;

    public FilePlanningRepository(Path file) {
        this.file = file;
    }

    @Override
    public void save(Planning planning) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = Files.createTempFile(parent, "planejamento-", ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(
                    Files.newOutputStream(temporary))) {
                output.writeObject(planning);
            }
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @Override
    public Planning load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            Object value = input.readObject();
            if (!(value instanceof Planning planning)) {
                throw new IOException("O arquivo não contém um planejamento válido.");
            }
            return planning;
        }
    }
}
