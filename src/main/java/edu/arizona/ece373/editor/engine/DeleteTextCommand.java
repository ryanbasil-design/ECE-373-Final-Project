package edu.arizona.ece373.editor.engine;

import java.util.List;
import java.util.Objects;

/**
 * Deletes a range of text from a paragraph. The removed runs are kept so
 * undo can put the text back with its original formatting.
 */
public class DeleteTextCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int start;
    private final int end;
    private List<TextRun> removed = List.of();   // filled in by execute()

    /**
     * Creates a command that deletes the text from start up to end.
     *
     * @param paragraph the paragraph to delete from
     * @param start     the first character to delete, inclusive
     * @param end       the end of the range, exclusive
     * @throws NullPointerException     if paragraph is null
     * @throws IllegalArgumentException if end is before start
     */
    public DeleteTextCommand(Paragraph paragraph, int start, int end) {
        this.paragraph = Objects.requireNonNull(paragraph, "paragraph must not be null");
        if (end < start) {
            throw new IllegalArgumentException("end must not be before start");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Deletes the range and remembers what was removed.
     *
     * @throws IndexOutOfBoundsException if the range is outside the paragraph
     */
    @Override
    public void execute() {
        removed = paragraph.deleteText(start, end - start);
    }

    /**
     * Puts the removed text back where it was, with its formatting.
     */
    @Override
    public void undo() {
        paragraph.insertRuns(start, removed);
    }
}
