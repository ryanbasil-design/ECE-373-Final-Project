package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A block of text made of an ordered list of text runs, where each run
 * has its own character format.
 * Text runs are immutable, so editing replaces runs with new ones instead
 * of modifying them. Neighboring runs with the same format are merged, and
 * empty runs are never stored
 */
public class Paragraph extends DocumentElement {

    private final List<TextRun> runs = new ArrayList<>();

    /** Holds which run a position falls in and the offset inside that run. */
    private record RunLocation(int runIndex, int offset) {}

    /**
     * Creates an empty paragraph.
     */
    public Paragraph() {
    }

    /**
     * Creates a paragraph containing the given text in a single format.
     *
     * @param text      the starting text
     * @param format    the format applied to the text
     */
    public Paragraph(String text, CharacterFormat format) {
        Objects.requireNonNull(text, "text must not be null");
        if (!text.isEmpty()) {
            runs.add(new TextRun(text, format));
        }
    }

    /**
     * Returns a read-only view of this paragraph's text runs, in order.
     *
     * @return  the runs of this paragraph
     */
    public List<TextRun> getRuns() {
        return List.copyOf(runs);
    }

    /**
     * Returns the plain text of this paragraph, without formatting.
     *
     * @return  the paragraph text
     */
    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (TextRun run : runs) {
            sb.append(run.getText());
        }
        return sb.toString();
    }

    /**
     * Returns the number of whitespace-separate words in this paragraph.
     *
     * @return  the word count
     */
    @Override
    public int countWords() {
        String trimmed = getText().trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    /**
     * Returns the number of characters in this paragraph, including spaces.
     *
     * @return the character count
     */
    @Override
    public int countChars() {
        int total = 0;
        for (TextRun run : runs) {
            total += run.length();
        }
        return total;
    }

    /**
     * Returns 1, since a paragraph counts as one paragraph.
     *
     * @return  1
     */
    @Override
    public int countParagraphs() {
        return 1;
    }

    /**
     * Inserts text at the given position. The inserted text takes the format
     * of the character before the position, or of the first run when inserting
     * at position 0. In an empty paragraph it uses CharacterFormat.DEFAULT.
     *
     *
     * @param position  the character position to insert at, from 0 to the paragraph length
     * @param text      the text to insert
     * @throws          IndexOutOfBoundsException if position is outside the paragraph
     * @throws          NullPointerException if text is null
     */
    public void insertText(int position, String text) {
        Objects.requireNonNull(text, "text must not be null");
        checkPosition(position);
        if (text.isEmpty()) {
            return;
        }
        if (runs.isEmpty()) {
            runs.add(new TextRun(text, CharacterFormat.DEFAULT));
            return;
        }
        RunLocation loc = findRun(position);
        TextRun old = runs.get(loc.runIndex());
        String oldText = old.getText();
        String updated = oldText.substring(0, loc.offset()) + text + oldText.substring(loc.offset());
        runs.set(loc.runIndex(), new TextRun(updated, old.getFormat()));
    }

    /**
     * Deletes a range of text and returns the removed pieces with their formats,
     * so an undo command can restore them.
     *
     * @param position  the starting character position
     * @param length    the number of characters to delete
     * @return          the removed text runs, in order
     * @throws          IndexOutOfBoundsException if the range extends outside the paragraph
     * @throws          IllegalArgumentException if length is negative
     */
    public List<TextRun> deleteText(int position, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative: " + length);
        }
        int end = position + length;
        if (position < 0 || end > countChars()) {
            throw new IndexOutOfBoundsException(
                    "range " + position + "-" + end + " is outside the paragraph (length " + countChars() + ")");
        }

        List<TextRun> kept = new ArrayList<>();
        List<TextRun> removed = new ArrayList<>();
        int runStart = 0;

        for (TextRun run : runs) {
            int runEnd = runStart + run.length();
            // Portion of this run that falls inside the deleted range, relative to the run
            int cutStart = Math.max(position, runStart) - runStart;
            int cutEnd = Math.min(end, runEnd) - runStart;

            if (cutStart >= cutEnd) {
                kept.add(run); // no overlap with the deleted range
            } else {
                String s = run.getText();
                String remaining = s.substring(0, cutStart) + s.substring(cutEnd);
                if (!remaining.isEmpty()) {
                    kept.add(new TextRun(remaining, run.getFormat()));
                }
                removed.add(new TextRun(s.substring(cutStart, cutEnd), run.getFormat()));
            }
            runStart = runEnd;
        }

        runs.clear();
        runs.addAll(mergeAdjacent(kept));
        return removed;
    }

    /** Finds the run containing a position; boundaries belong to the run before. */
    private RunLocation findRun(int position) {
        int remaining = position;
        for (int i = 0; i < runs.size(); i++) {
            int len = runs.get(i).length();
            if (remaining <= len) {
                return new RunLocation(i, remaining);
            }
            remaining -= len;
        }
        throw new IndexOutOfBoundsException(
                "position " + position + " is past the end of the paragraph (length " + countChars() + ")");
    }

    /** Throws if the position is not between 0 and the paragraph length. */
    private void checkPosition(int position) {
        if (position < 0 || position > countChars()) {
            throw new IndexOutOfBoundsException(
                    "position " + position + " is outside the paragraph (length " + countChars() + ")");
        }
    }

    /** Combines neighboring runs that share the same format. */
    private static List<TextRun> mergeAdjacent(List<TextRun> list) {
        List<TextRun> merged = new ArrayList<>();
        for (TextRun run : list) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).getFormat().equals(run.getFormat())) {
                TextRun last = merged.remove(merged.size() - 1);
                merged.add(new TextRun(last.getText() + run.getText(), last.getFormat()));
            } else {
                merged.add(run);
            }
        }
        return merged;
    }

    /**
     * Creates a paragraph from the given text runs, in order. Empty runs are
     * dropped and neighboring runs with the same format are merged.
     *
     * @param   runs the text runs to start with
     * @throws  NullPointerException if runs or any run in it is null
     */
    public Paragraph(List<TextRun> runs) {
        Objects.requireNonNull(runs, "runs must not be null");
        List<TextRun> nonEmpty = new ArrayList<>();
        for (TextRun run : runs) {
            Objects.requireNonNull(run, "runs must not contain null");
            if (run.length() > 0) {
                nonEmpty.add(run);
            }
        }
        this.runs.addAll(mergeAdjacent(nonEmpty));
    }

    /**
     * Returns copies of the runs that cover the range from start up to end,
     * keeping each piece's format. Runs that are only partly inside the range
     * are cut at the range edges.
     *
     * @param start the first character position, inclusive
     * @param end   the last character position, exclusive
     * @return the runs inside the range, in order
     * @throws IndexOutOfBoundsException if the range is outside the paragraph
     */
    public List<TextRun> getRuns(int start, int end) {
        checkRange(start, end);
        List<TextRun> slice = new ArrayList<>();
        int runStart = 0;
        for (TextRun run : runs) {
            int runEnd = runStart + run.length();
            int from = Math.max(start, runStart);
            int to = Math.min(end, runEnd);
            if (from < to) {
                slice.add(new TextRun(run.getText().substring(from - runStart, to - runStart),
                        run.getFormat()));
            }
            runStart = runEnd;
        }
        return slice;
    }

    /**
     * Inserts the given runs at a character position, keeping each run's own
     * format. This is used when text is moved so its formatting moves with it.
     *
     * @param position where to insert, from 0 up to the paragraph length
     * @param inserted the runs to insert, in order
     * @throws IndexOutOfBoundsException if position is outside the paragraph
     * @throws NullPointerException      if inserted is null
     */
    public void insertRuns(int position, List<TextRun> inserted) {
        Objects.requireNonNull(inserted, "inserted must not be null");
        checkPosition(position);
        List<TextRun> updated = new ArrayList<>(getRuns(0, position));
        updated.addAll(inserted);
        updated.addAll(getRuns(position, countChars()));
        setRuns(updated);
    }

    /**
     * Replaces every run in this paragraph with the given runs. Empty runs are
     * dropped and neighboring runs with the same format are merged. Editing
     * commands use this to put a paragraph back the way it was on undo.
     *
     * @param newRuns the runs the paragraph should hold
     * @throws NullPointerException if newRuns or any run in it is null
     */
    public void setRuns(List<TextRun> newRuns) {
        Objects.requireNonNull(newRuns, "newRuns must not be null");
        List<TextRun> nonEmpty = new ArrayList<>();
        for (TextRun run : newRuns) {
            Objects.requireNonNull(run, "runs must not contain null");
            if (run.length() > 0) {
                nonEmpty.add(run);
            }
        }
        runs.clear();
        runs.addAll(mergeAdjacent(nonEmpty));
    }

    private void checkRange(int start, int end) {
        if (start < 0 || end < start || end > countChars()) {
            throw new IndexOutOfBoundsException(
                    "range " + start + "-" + end + " is outside the paragraph (length " + countChars() + ")");
        }
    }
}
