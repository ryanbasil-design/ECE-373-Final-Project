package edu.arizona.ece373.editor.engine;

import java.util.Objects;

public class CharacterFormat {

    // Final variables
    private final boolean italic;
    private final boolean bold;
    private final int size;
    private final String font;
    private final String color;

    // Create the CharacterFormat
    public CharacterFormat(boolean bold, boolean italic, int size, String font, String color){
        this.bold = bold;
        this.italic = italic;
        this.size = size;
        this.font = font;
        this.color = color;
    }

    // Getters
    public boolean isItalic() {
        return italic;
    }

    public boolean isBold() {
        return bold;
    }

    public int getSize() {
        return size;
    }

    public String getFont() {
        return font;
    }

    public String getColor() {
        return color;
    }

    // Generate equals() and hashCode() on IntelliJ
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CharacterFormat that = (CharacterFormat) o;
        return isItalic() == that.isItalic() && isBold() == that.isBold() && getSize() == that.getSize() && Objects.equals(getFont(), that.getFont()) && Objects.equals(getColor(), that.getColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isItalic(), isBold(), getSize(), getFont(), getColor());
    }
}
