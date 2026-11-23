package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.arizona.ece373.editor.engine.EditTestFormats.BOLD;
import static edu.arizona.ece373.editor.engine.EditTestFormats.PLAIN;
import static org.junit.jupiter.api.Assertions.*;

class ParagraphRunEditingTest {

    private Paragraph mixed() {
        return new Paragraph(List.of(new TextRun("Hello ", PLAIN), new TextRun("big", BOLD),
                new TextRun(" world", PLAIN)));
    }

    @Test
    void getRunsCutsRunsAtTheRangeEdges() {
        assertEquals(List.of(new TextRun("lo ", PLAIN), new TextRun("bi", BOLD)), mixed().getRuns(3, 8));
    }

    @Test
    void getRunsOfEmptyRangeIsEmpty() {
        assertTrue(mixed().getRuns(4, 4).isEmpty());
    }

    @Test
    void getRunsRejectsRangeOutsideParagraph() {
        Paragraph p = mixed();
        assertThrows(IndexOutOfBoundsException.class, () -> p.getRuns(-1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getRuns(5, 99));
        assertThrows(IndexOutOfBoundsException.class, () -> p.getRuns(5, 2));
    }

    @Test
    void insertRunsKeepsTheirFormat() {
        Paragraph p = new Paragraph("ab", PLAIN);
        p.insertRuns(1, List.of(new TextRun("X", BOLD)));
        assertEquals(List.of(new TextRun("a", PLAIN), new TextRun("X", BOLD), new TextRun("b", PLAIN)),
                p.getRuns());
    }

    @Test
    void insertRunsAtEndAndIntoEmptyParagraph() {
        Paragraph p = new Paragraph();
        p.insertRuns(0, List.of(new TextRun("hi", BOLD)));
        p.insertRuns(2, List.of(new TextRun("!", BOLD)));
        assertEquals(List.of(new TextRun("hi!", BOLD)), p.getRuns());
    }

    @Test
    void insertRunsRejectsBadPosition() {
        Paragraph p = new Paragraph("ab", PLAIN);
        assertThrows(IndexOutOfBoundsException.class, () -> p.insertRuns(3, List.of()));
    }

    @Test
    void setRunsDropsEmptyRunsAndMerges() {
        Paragraph p = mixed();
        p.setRuns(List.of(new TextRun("a", PLAIN), new TextRun("", BOLD), new TextRun("b", PLAIN)));
        assertEquals(List.of(new TextRun("ab", PLAIN)), p.getRuns());
    }
}
