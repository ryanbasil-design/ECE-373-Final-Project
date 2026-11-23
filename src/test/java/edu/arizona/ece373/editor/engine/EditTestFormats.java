package edu.arizona.ece373.editor.engine;

/** Formats shared by the editing command tests. */
final class EditTestFormats {

    static final CharacterFormat PLAIN = new CharacterFormat(false, false, 12, "Arial", "black");
    static final CharacterFormat BOLD = new CharacterFormat(true, false, 12, "Arial", "black");

    private EditTestFormats() {
    }

    static CharacterFormat bold(CharacterFormat f) {
        return new CharacterFormat(true, f.isItalic(), f.getSize(), f.getFont(), f.getColor());
    }
}
