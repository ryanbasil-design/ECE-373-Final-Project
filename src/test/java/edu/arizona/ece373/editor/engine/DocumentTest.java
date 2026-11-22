package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "black");

    /** Test observer that records how many times it was notified and by which document. */
    private static class CountingObserver implements DocumentObserver {
        int calls = 0;
        Document lastDocument = null;

        @Override
        public void onDocumentChanged(Document document) {
            calls++;
            lastDocument = document;
        }
    }

    private Document buildDocument() {
        Section intro = new Section("Intro", plain);
        intro.add(new Paragraph("Hello world", plain));

        Document doc = new Document();
        doc.add(intro);
        doc.add(new Paragraph("The end.", plain));
        return doc;
    }

    // Content

    @Test
    void newDocumentIsEmpty() {
        Document doc = new Document();
        assertTrue(doc.getElements().isEmpty());
        assertEquals("", doc.getText());
        assertEquals(0, doc.countWords());
        assertEquals(0, doc.countChars());
        assertEquals(0, doc.countParagraphs());
    }

    @Test
    void holdsSectionsAndParagraphsInOrder() {
        Document doc = new Document();
        Section s = new Section("S", plain);
        Paragraph p = new Paragraph("p", plain);
        doc.add(s);
        doc.add(p);
        assertEquals(List.of(s, p), doc.getElements());
    }

    @Test
    void addAtIndexAndRemove() {
        Document doc = new Document();
        Paragraph a = new Paragraph("a", plain);
        Paragraph b = new Paragraph("b", plain);
        doc.add(b);
        doc.add(0, a);
        assertEquals(List.of(a, b), doc.getElements());
        assertSame(a, doc.remove(0));
        assertEquals(List.of(b), doc.getElements());
    }

    @Test
    void badIndexThrows() {
        Document doc = new Document();
        assertThrows(IndexOutOfBoundsException.class, () -> doc.add(1, new Paragraph("x", plain)));
        assertThrows(IndexOutOfBoundsException.class, () -> doc.remove(0));
    }

    // Text and Counts

    @Test
    void getTextHasNoLeadingNewline() {
        assertEquals("Intro\nHello world\nThe end.", buildDocument().getText());
    }

    @Test
    void countsIncludeTitlesButNotAsParagraphs() {
        Document doc = buildDocument();
        assertEquals(5, doc.countWords());       // 1 title + 2 + 2
        assertEquals(24, doc.countChars());      // 5 + 11 + 8
        assertEquals(2, doc.countParagraphs());  // title not counted
    }

    // Observers

    @Test
    void observerIsNotifiedWithTheDocument() {
        Document doc = new Document();
        CountingObserver observer = new CountingObserver();
        doc.addObserver(observer);

        doc.notifyObservers();
        doc.notifyObservers();

        assertEquals(2, observer.calls);
        assertSame(doc, observer.lastDocument);
    }

    @Test
    void removedObserverIsNoLongerNotified() {
        Document doc = new Document();
        CountingObserver observer = new CountingObserver();
        doc.addObserver(observer);
        doc.notifyObservers();

        doc.removeObserver(observer);
        doc.notifyObservers();

        assertEquals(1, observer.calls);
    }

    @Test
    void editingAloneDoesNotNotify() {
        Document doc = new Document();
        CountingObserver observer = new CountingObserver();
        doc.addObserver(observer);
        doc.add(new Paragraph("text", plain));
        assertEquals(0, observer.calls);
    }

    @Test
    void nullObserverThrows() {
        assertThrows(NullPointerException.class, () -> new Document().addObserver(null));
    }
}
