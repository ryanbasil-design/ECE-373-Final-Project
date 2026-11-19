package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CharacterFormatTest {

    private final CharacterFormat tester = new CharacterFormat(true, false, 12, "Arial", "Red");

    @Test
    void gettersReturnConstructorValues () {
        assertTrue(tester.isBold());
        assertFalse(tester.isItalic());
        assertEquals(12, tester.getSize());
        assertEquals("Arial", tester.getFont());
        assertEquals("Red", tester.getColor());
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, 12, Arial, Red"  ,
            "true , true , 12, Arial, Red"  ,
            "true , false, 15, Arial, Red"  ,
            "true , false, 12, TNR  , Red"  ,
            "true , false, 12, Arial, Black"
    })
    void formatsDifferingInOneFieldAreNotEqual(boolean bold, boolean italic, int size, String font, String color) {
        assertNotEquals(tester, new CharacterFormat(bold, italic, size, font, color));
    }

    @Test
    void sameValuesAreEqualWithSameHashCode() {
        CharacterFormat testCopy = new CharacterFormat(true, false, 12, "Arial", "Red");
        assertEquals(tester, testCopy);
        assertEquals(tester.hashCode(), testCopy.hashCode());
    }

    @Test
    void notEqualToNull() {
        assertNotEquals(null, tester);
    }

}
