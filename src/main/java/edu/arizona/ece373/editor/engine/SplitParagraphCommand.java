package edu.arizona.ece373.editor.engine;

import java.util.List;
import java.util.Objects;

/**
 * Splits one paragraph into two at a character position. The text after the
 * position moves into a new paragraph placed right after the original.
 */
public class SplitParagraphCommand implements EditCommand {

    private final ElementContainer parent;
    private final Paragraph paragraph;
    private final int position;
    private Paragraph created;                 // the new second paragraph
    private List<TextRun> before = List.of();  // original runs, for undo

    /**
     * Creates a command that splits a paragraph.
     *
     * @param parent    the document or section holding the paragraph
     * @param paragraph the paragraph to split
     * @param position  where to split, from 0 up to the paragraph length
     * @throws NullPointerException if parent or paragraph is null
     */
    public SplitParagraphCommand(ElementContainer parent, Paragraph paragraph, int position) {
        this.parent = Objects.requireNonNull(parent, "parent must not be null");
        this.paragraph = Objects.requireNonNull(paragraph, "paragraph must not be null");
        this.position = position;
    }

    /**
     * Splits the paragraph.
     *
     * @throws IndexOutOfBoundsException if the position is outside the paragraph
     * @throws IllegalArgumentException  if the paragraph is not in the parent
     */
    @Override
    public void execute() {
        int index = parent.indexOf(paragraph);
        if (index < 0) {
            throw new IllegalArgumentException("paragraph is not in the given parent");
        }
        List<TextRun> tail = paragraph.getRuns(position, paragraph.countChars());
        before = paragraph.getRuns();
        created = new Paragraph(tail);
        paragraph.deleteText(position, paragraph.countChars() - position);
        parent.add(index + 1, created);
    }

    /**
     * Removes the new paragraph and puts the original text back together.
     */
    @Override
    public void undo() {
        parent.remove(parent.indexOf(created));
        paragraph.setRuns(before);
    }
}
