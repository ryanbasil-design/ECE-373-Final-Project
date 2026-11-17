package edu.arizona.ece373.editor.engine;

import java.util.Objects;

/**
 *  Represents the character formatting applied to a text run:
 *  bold, italic, size, font, and color.
 *
 *  Instances are immutable. To change formatting, create a new CharacterFormat
 *  rather than modifying an existing one, so formats can be shared between
 *  text runs and saved for undo.
 */
public class CharacterFormat {

    private final boolean italic;
    private final boolean bold;
    private final int size;
    private final String font;
    private final String color;

    /**
     * Creates a character format with the following attributes.
     *
     * @param bold      true if the text is bold
     * @param italic    true if the text is italic
     * @param size      the font size in points
     * @param font      the font name, such as "Times"
     * @param color     the font color, such as "Red"
     */
    public CharacterFormat(boolean bold, boolean italic, int size, String font, String color){
        this.bold = bold;
        this.italic = italic;
        this.size = size;
        this.font = font;
        this.color = color;
    }

    /**
     * Returns the italic state of this format.
     *
     * @return true if the text is italic
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Returns the bold state of this format.
     *
     * @return true if the text is bold
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Returns the font size of this format.
     *
     * @return the font size in points
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the font name of this format.
     *
     * @return the font name
     */
    public String getFont() {
        return font;
    }

    /**
     * Returns the font color of this format.
     *
     * @return the font color
     */
    public String getColor() {
        return color;
    }

    /**
     * Compares this format to another object. Two formats are equal when
     * bold, italic, size, font, and color all match.
     *
     * @param o   the reference object with which to compare.
     * @return    true if o is a CharacterFormat with identical attributes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CharacterFormat that = (CharacterFormat) o;
        return isItalic() == that.isItalic() && isBold() == that.isBold() && getSize() == that.getSize() && Objects.equals(getFont(), that.getFont()) && Objects.equals(getColor(), that.getColor());
    }

    /**
     * Returns a hash code based on all five attributes, so formats
     * that are equal always have the same hash code.
     *
     * @return the hash code for this format
     */
    @Override
    public int hashCode() {
        return Objects.hash(isItalic(), isBold(), getSize(), getFont(), getColor());
    }
}
