package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextRunTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "Red");
    private final CharacterFormat bold = new CharacterFormat(true, false, 12, "Arial", "Red");
    private final TextRun run = new TextRun("Hello", plain);

    @Test
    void gettersReturnConstructorValues() {
        assertEquals("Hello", run.getText());
        assertEquals(plain, run.getFormat());
        assertEquals(5, run.length());
    }

    @Test
    void sameTextAndFormatAreEqualWithSameHashCode() {
        TextRun copy = new TextRun("Hello", plain);
        assertEquals(run, copy);
        assertEquals(run.hashCode(), copy.hashCode());
    }

    @Test
    void differentTextNotEqual() {
        assertNotEquals(run, new TextRun("Goodbye", plain));
    }

    @Test
    void differentFormatNotEqual() {
        assertNotEquals(run, new TextRun("Hello", bold));
    }

    @Test
    void emptyTextHasZeroLength() {
        assertEquals(0, new TextRun("", plain).length());
    }

    @Test
    void nullTextThrows() {
        assertThrows(NullPointerException.class, () -> new TextRun(null, plain));
    }

    @Test
    void nullFormatThrows() {
        assertThrows(NullPointerException.class, () -> new TextRun("hello", null));
    }
}
