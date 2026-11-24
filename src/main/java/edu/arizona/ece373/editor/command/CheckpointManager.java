
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.Document;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages named checkpoints for a document.
 */
public class CheckpointManager {

    private final Map<String, CheckpointSnapshot> checkpoints;

    // Creates an empty checkpoint manager
    public CheckpointManager() {
        checkpoints = new HashMap<>();
    }

    // Saves the current document using a checkpoint name
    public void saveCheckpoint(String name, Document document) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Checkpoint name cannot be empty"
            );
        }

        checkpoints.put(name, new CheckpointSnapshot(document));
    }

    // Returns a copy of the document saved at a checkpoint
    public Document restoreCheckpoint(String name) {
        if (!checkpoints.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Checkpoint does not exist: " + name
            );
        }

        return checkpoints.get(name).getDocument();
    }

    // Checks whether a checkpoint exists
    public boolean hasCheckpoint(String name) {
        return checkpoints.containsKey(name);
    }

    // Returns the number of saved checkpoints
    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
