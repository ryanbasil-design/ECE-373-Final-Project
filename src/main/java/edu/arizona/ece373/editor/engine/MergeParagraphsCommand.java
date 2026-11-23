package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Merges a paragraph with the paragraph right after it in the same parent.
 * The second paragraph's text is added to the end of the first, and the
 * second paragraph is removed.
 */
public class MergeParagraphsCommand implements EditCommand {

    private final ElementContainer parent;
    private final Paragraph first;
    private Paragraph second;                  // the paragraph that was merged in
    private List<TextRun> before = List.of();  // first paragraph's runs, for undo

    /**
     * Creates a command that merges a paragraph with the next one.
     *
     * @param parent the document or section holding the paragraph
     * @param first  the paragraph that the next one is merged into
     * @throws NullPointerException if parent or first is null
     */
    public MergeParagraphsCommand(ElementContainer parent, Paragraph first) {
        this.parent = Objects.requireNonNull(parent, "parent must not be null");
        this.first = Objects.requireNonNull(first, "first must not be null");
    }

    /**
     * Merges the two paragraphs.
     *
     * @throws IllegalArgumentException if the paragraph is not in the parent,
     *                                  or the next element is not a paragraph
     */
    @Override
    public void execute() {
        List<DocumentElement> elements = parent.elements();
        int index = elements.indexOf(first);
        if (index < 0) {
            throw new IllegalArgumentException("paragraph is not in the given parent");
        }
        if (index + 1 >= elements.size() || !(elements.get(index + 1) instanceof Paragraph next)) {
            throw new IllegalArgumentException("there is no paragraph right after this one to merge");
        }
        second = next;
        before = first.getRuns();
        List<TextRun> merged = new ArrayList<>(before);
        merged.addAll(second.getRuns());
        first.setRuns(merged);
        parent.remove(index + 1);
    }

    /**
     * Splits the paragraphs back apart.
     */
    @Override
    public void undo() {
        first.setRuns(before);
        parent.add(parent.indexOf(first) + 1, second);
    }
}
