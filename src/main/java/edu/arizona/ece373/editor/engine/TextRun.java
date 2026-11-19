package edu.arizona.ece373.editor.engine;

import java.util.Objects;

/**
 * Represents a continuous piece of text within a paragraph that shares
 * the same character format.
 *
 * Instances are immutable. Editing operations in paragraphs replace runs with new
 * TextRun objects instead of modifying existing ones.
 */
public class TextRun {

    private final String text;
    private final CharacterFormat format;

    /**
     * Creates a text run with the given text and format.
     *
     * @param text    The text content of this run
     * @param format  The character format applied to the text
     * @throws        NullPointerException if text or format is null
     */
    public TextRun(String text, CharacterFormat format) {
        this.text = Objects.requireNonNull(text, "text must not be null");
        this.format = Objects.requireNonNull(format, "format must not be null");
    }

    /**
     * Returns the text content of this run.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the character format applied to the text.
     *
     * @return the format
     */
    public CharacterFormat getFormat() {
        return format;
    }

    /**
     * Returns the number of characters in this run.
     *
     * @return the length of the text
     */
    public int length() {
        return text.length();
    }

    /**
     * Compares this run to another object. Two runs are equal when
     * their text and format both match.
     *
     * @param o   the reference object with which to compare.
     * @return    true if o is a TextRun with the same text and format
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TextRun textRun = (TextRun) o;
        return Objects.equals(getText(), textRun.getText()) && Objects.equals(getFormat(), textRun.getFormat());
    }

    /**
     * Returns a hash code based on the text and format, so runs
     * that are equal always have the same hash code.
     *
     * @return the hash code for this run
     */
    @Override
    public int hashCode() {
        return Objects.hash(getText(), getFormat());
    }
}
