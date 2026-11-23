package edu.arizona.ece373.editor.engine;

import java.util.List;
import java.util.Objects;

/**
 * Inserts text into a paragraph at a character position. Plain text takes
 * the format of the text around it; a list of runs keeps its own formats,
 * which is how moved text keeps its formatting.
 */
public class InsertTextCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int position;
    private final List<TextRun> runs;   // null when inserting plain text
    private final String text;
    private final int length;

    /**
     * Creates a command that inserts plain text.
     *
     * @param paragraph the paragraph to insert into
     * @param position  where to insert, from 0 up to the paragraph length
     * @param text      the text to insert
     * @throws NullPointerException if paragraph or text is null
     */
    public InsertTextCommand(Paragraph paragraph, int position, String text) {
        this.paragraph = Objects.requireNonNull(paragraph, "paragraph must not be null");
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.position = position;
        this.runs = null;
        this.length = text.length();
    }

    /**
     * Creates a command that inserts formatted runs.
     *
     * @param paragraph the paragraph to insert into
     * @param position  where to insert, from 0 up to the paragraph length
     * @param runs      the runs to insert, each keeping its format
     * @throws NullPointerException if paragraph or runs is null
     */
    public InsertTextCommand(Paragraph paragraph, int position, List<TextRun> runs) {
        this.paragraph = Objects.requireNonNull(paragraph, "paragraph must not be null");
        this.runs = List.copyOf(Objects.requireNonNull(runs, "runs must not be null"));
        this.position = position;
        int total = 0;
        StringBuilder sb = new StringBuilder();
        for (TextRun run : this.runs) {
            total += run.length();
            sb.append(run.getText());
        }
        this.length = total;
        this.text = sb.toString();
    }

    /**
     * Inserts the text into the paragraph.
     *
     * @throws IndexOutOfBoundsException if the position is outside the paragraph
     */
    @Override
    public void execute() {
        if (runs == null) {
            paragraph.insertText(position, text);
        } else {
            paragraph.insertRuns(position, runs);
        }
    }

    /**
     * Removes the inserted text.
     */
    @Override
    public void undo() {
        paragraph.deleteText(position, length);
    }
}
