package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Changes the character format of a range of text without changing the
 * text itself. The paragraph's previous runs are kept for undo.
 */
public class ApplyFormatCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int start;
    private final int end;
    private final UnaryOperator<CharacterFormat> change;
    private List<TextRun> before = List.of();   // runs before execute(), for undo

    /**
     * Creates a command that formats the text from start up to end.
     *
     * @param paragraph the paragraph to format (this can be a section title)
     * @param start     the first character to format, inclusive
     * @param end       the end of the range, exclusive
     * @param change    turns each piece's old format into its new format,
     *                  for example making it bold while keeping everything else
     * @throws NullPointerException if paragraph or change is null
     */
    public ApplyFormatCommand(Paragraph paragraph, int start, int end,
                              UnaryOperator<CharacterFormat> change) {
        this.paragraph = Objects.requireNonNull(paragraph, "paragraph must not be null");
        this.change = Objects.requireNonNull(change, "change must not be null");
        this.start = start;
        this.end = end;
    }

    /**
     * Applies the format change to every run inside the range.
     *
     * @throws IndexOutOfBoundsException if the range is outside the paragraph
     */
    @Override
    public void execute() {
        List<TextRun> middle = paragraph.getRuns(start, end);   // checks the range first
        before = paragraph.getRuns();
        List<TextRun> updated = new ArrayList<>(paragraph.getRuns(0, start));
        for (TextRun run : middle) {
            updated.add(new TextRun(run.getText(), change.apply(run.getFormat())));
        }
        updated.addAll(paragraph.getRuns(end, paragraph.countChars()));
        paragraph.setRuns(updated);
    }

    /**
     * Puts back the formatting the paragraph had before.
     */
    @Override
    public void undo() {
        paragraph.setRuns(before);
    }
}
