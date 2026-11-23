package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A group of commands that runs and undoes as one change. Replace-all, move
 * and macro replays use this so a single undo reverses the whole thing. If
 * one step fails, the steps already done are undone so the document is left
 * the way it started.
 */
public class CompositeCommand implements EditCommand {

    private final List<EditCommand> steps;

    /**
     * Creates a composite from the given steps, run in order.
     *
     * @param steps the commands to group
     * @throws NullPointerException if steps or any step is null
     */
    public CompositeCommand(List<EditCommand> steps) {
        Objects.requireNonNull(steps, "steps must not be null");
        for (EditCommand step : steps) {
            Objects.requireNonNull(step, "steps must not contain null");
        }
        this.steps = List.copyOf(steps);
    }

    /**
     * Runs every step in order. If a step throws, the steps that already ran
     * are undone in reverse order and the exception is passed on.
     */
    @Override
    public void execute() {
        List<EditCommand> done = new ArrayList<>();
        try {
            for (EditCommand step : steps) {
                step.execute();
                done.add(step);
            }
        } catch (RuntimeException e) {
            for (int i = done.size() - 1; i >= 0; i--) {
                done.get(i).undo();
            }
            throw e;
        }
    }

    /**
     * Undoes every step in reverse order.
     */
    @Override
    public void undo() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            steps.get(i).undo();
        }
    }

    /**
     * Returns the steps in this composite.
     *
     * @return a read-only list of the steps, in order
     */
    public List<EditCommand> getSteps() {
        return steps;
    }
}
