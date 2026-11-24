
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.Document;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages named checkpoints for a document.
 */
public class CheckpointManager {

    private final Map<String, CheckpointSnapshot> checkpoints;

    /**
     * makes an empty checkpoint manager
     */
    public CheckpointManager() {
        checkpoints = new HashMap<>();
    }

    /**
     * saves a copy of the document under a checkpoint name
     *
     * @param name the checkpoint name
     * @param document the document to save
     */
    public void saveCheckpoint(String name, Document document) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Checkpoint name cannot be empty"
            );
        }

        checkpoints.put(name, new CheckpointSnapshot(document));
    }

    /**
     * gets a copy of the document saved under a checkpoint name
     *
     * @param name the checkpoint name
     * @return a copy of the saved document
     */
    public Document restoreCheckpoint(String name) {
        if (!checkpoints.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Checkpoint does not exist: " + name
            );
        }

        return checkpoints.get(name).getDocument();
    }

    /**
     * checks if a checkpoint with this name exists
     *
     * @param name the checkpoint name
     * @return true if it exists
     */
    public boolean hasCheckpoint(String name) {
        return checkpoints.containsKey(name);
    }

    /**
     * gets how many checkpoints are saved
     *
     * @return the number of checkpoints
     */
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
