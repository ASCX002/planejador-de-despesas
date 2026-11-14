package persistence;

import model.Planning;

import java.io.IOException;

public interface PlanningRepository {
    void save(Planning planning) throws IOException;

    Planning load() throws IOException, ClassNotFoundException;
}
