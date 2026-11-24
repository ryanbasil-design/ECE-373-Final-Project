package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.TextRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentViewTest {

    private static final CharacterFormat DEF = CharacterFormat.DEFAULT;
    private final CharacterFormat bold =
            new CharacterFormat(true, false, DEF.getSize(), DEF.getFont(), DEF.getColor());

    // One paragraph: "aaa bb cc ddddd" with "bb" in bold
    private Document buildDocument() {
        Document doc = new Document();
        doc.add(new Paragraph(List.of(
                new TextRun("aaa ", DEF),
                new TextRun("bb", bold),
                new TextRun(" cc ddddd", DEF))));
        return doc;
    }

    @Test
    void defaultsArePlainGreedyAndWidth80() {
        DocumentView view = new DocumentView(buildDocument());
        assertInstanceOf(PlainTextRenderer.class, view.getRenderer());
        assertInstanceOf(GreedyLineBreaker.class, view.getLineBreaker());
        assertEquals(80, view.getWidth());
        assertEquals("aaa bb cc ddddd", view.render());
    }

    @Test
    void switchingRendererChangesOutput() {
        DocumentView view = new DocumentView(buildDocument());
        view.setRenderer(new MarkupRenderer());
        assertEquals("aaa [bold]bb[/] cc ddddd", view.render());
    }

    @Test
    void switchingWidthAndBreakerChangesOutput() {
        DocumentView view = new DocumentView(buildDocument());
        view.setWidth(9);
        assertEquals("aaa bb cc\nddddd", view.render());

        view.setLineBreaker(new BalancedLineBreaker());
        assertEquals("aaa bb\ncc ddddd", view.render());
    }

    @Test
    void renderReflectsLatestDocumentChanges() {
        Document doc = new Document();
        DocumentView view = new DocumentView(doc);
        assertEquals("", view.render());

        doc.add(new Paragraph("new text", DEF));
        assertEquals("new text", view.render());
    }

    @Test
    void invalidWidthThrowsAndKeepsOldWidth() {
        DocumentView view = new DocumentView(buildDocument());
        view.setWidth(40);
        assertThrows(IllegalArgumentException.class, () -> view.setWidth(0));
        assertEquals(40, view.getWidth());
    }

    @Test
    void nullSettingsThrow() {
        DocumentView view = new DocumentView(buildDocument());
        assertThrows(NullPointerException.class, () -> view.setRenderer(null));
        assertThrows(NullPointerException.class, () -> view.setLineBreaker(null));
        assertThrows(NullPointerException.class, () -> new DocumentView(null));
    }
}
