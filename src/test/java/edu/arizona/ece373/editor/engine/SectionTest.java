package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "Black");

    private Section buildIntroduction() {
        Section habitat = new Section("Habitat", plain);
        habitat.add(new Paragraph("Tigers live in Asia.", plain));
        habitat.add(new Paragraph("They like forests.", plain));

        Section intro = new Section("Introduction", plain);
        intro.add(new Paragraph("This report covers tigers.", plain));
        intro.add(habitat);
        intro.add(new Paragraph("Read on for more.", plain));
        return intro;
    }

    // Counts

    @Test
    void emptySectionCountsOnlyTitle() {
        Section s = new Section("Title", plain);
        assertEquals(1, s.countWords());
        assertEquals(5, s.countChars());
        assertEquals(0, s.countParagraphs());
        assertTrue(s.getChildren().isEmpty());
    }

    @Test
    void nestedCountsIncludeAllLevels() {
        Section intro = buildIntroduction();
        assertEquals(4, intro.countParagraphs());
        assertEquals(17, intro.countWords());
        assertEquals(100, intro.countChars());
    }

    @Test
    void getTextPutsTitleFirstWithNewlines() {
        Section intro = buildIntroduction();
        assertEquals("Introduction\n"
                + "This report covers tigers.\n"
                + "Habitat\n"
                + "Tigers live in Asia.\n"
                + "They like forests.\n"
                + "Read on for more.", intro.getText());
    }

    // Removal & Adding

    @Test
    void removeReturnsChildAndShiftsOthers() {
        Section s = new Section("S", plain);
        Paragraph a = new Paragraph("a", plain);
        Paragraph b = new Paragraph("b", plain);
        s.add(a);
        s.add(b);
        assertSame(a, s.remove(0));
        assertEquals(List.of(b), s.getChildren());
    }

    @Test
    void addAtIndexInsertsInOrder() {
        Section s = new Section("S", plain);
        Paragraph a = new Paragraph("a", plain);
        Paragraph b = new Paragraph("b", plain);
        Paragraph c = new Paragraph("c", plain);
        s.add(a);
        s.add(c);
        s.add(1, b);
        assertEquals(List.of(a, b, c), s.getChildren());
    }

    @Test
    void indexOfFindsChildOrReturnsMinusOne() {
        Section s = new Section("S", plain);
        Paragraph inside = new Paragraph("in", plain);
        s.add(inside);
        assertEquals(0, s.indexOf(inside));
        assertEquals(-1, s.indexOf(new Paragraph("out", plain)));
    }

    // Throws

    @Test
    void badIndexThrowsAndLeavesSectionUnchanged() {
        Section s = new Section("S", plain);
        Paragraph a = new Paragraph("a", plain);
        s.add(a);
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(5, new Paragraph("x", plain)));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(-1));
        assertEquals(List.of(a), s.getChildren());
    }

    @Test
    void nullChildThrows() {
        Section s = new Section("S", plain);
        assertThrows(NullPointerException.class, () -> s.add(null));
    }

    @Test
    void sectionCannotContainItself() {
        Section outer = new Section("Outer", plain);
        Section inner = new Section("Inner", plain);
        outer.add(inner);
        assertThrows(IllegalArgumentException.class, () -> outer.add(outer));
        assertThrows(IllegalArgumentException.class, () -> inner.add(outer));
    }

    @Test
    void getChildrenCannotBeModified() {
        Section s = buildIntroduction();
        assertThrows(UnsupportedOperationException.class,
                () -> s.getChildren().add(new Paragraph("x", plain)));
    }
}
