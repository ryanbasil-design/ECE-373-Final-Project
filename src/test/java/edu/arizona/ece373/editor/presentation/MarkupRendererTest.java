package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.Section;
import edu.arizona.ece373.editor.engine.TextRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarkupRendererTest {

    private static final CharacterFormat DEF = CharacterFormat.DEFAULT;
    private final CharacterFormat bold =
            new CharacterFormat(true, false, DEF.getSize(), DEF.getFont(), DEF.getColor());
    private final Renderer renderer = new MarkupRenderer();
    private final LineBreaker greedy = new GreedyLineBreaker();

    private Document docWith(Paragraph p) {
        Document doc = new Document();
        doc.add(p);
        return doc;
    }

    @Test
    void defaultTextHasNoMarkers() {
        assertEquals("Hello world",
                renderer.render(docWith(new Paragraph("Hello world", DEF)), greedy, 80));
    }

    @Test
    void boldRunIsTagged() {
        Paragraph p = new Paragraph(List.of(
                new TextRun("Hello ", DEF),
                new TextRun("big", bold),
                new TextRun(" world", DEF)));
        assertEquals("Hello [bold]big[/] world", renderer.render(docWith(p), greedy, 80));
    }

    @Test
    void allDifferencesAppearInFixedOrder() {
        CharacterFormat fancy = new CharacterFormat(true, true, 18, "Courier", "red");
        assertEquals("[bold,italic,font=Courier,size=18,color=red]x[/]",
                renderer.render(docWith(new Paragraph("x", fancy)), greedy, 80));
    }

    @Test
    void onlyChangedAttributesAreListed() {
        CharacterFormat red = new CharacterFormat(false, false, DEF.getSize(), DEF.getFont(), "red");
        assertEquals("[color=red]x[/]",
                renderer.render(docWith(new Paragraph("x", red)), greedy, 80));
    }

    @Test
    void spacesInFontNamesBecomeUnderscores() {
        CharacterFormat times = new CharacterFormat(false, false, DEF.getSize(), "Times New Roman", DEF.getColor());
        assertEquals("[font=Times_New_Roman]x[/]",
                renderer.render(docWith(new Paragraph("x", times)), greedy, 80));
    }

    @Test
    void sectionTitleFormattingIsShown() {
        Document doc = new Document();
        doc.add(new Section("Intro", bold));
        assertEquals("1 [bold]Intro[/]", renderer.render(doc, greedy, 80));
    }

    @Test
    void tagsAreNeverSplitAcrossLines() {
        Paragraph p = new Paragraph(List.of(
                new TextRun("aaa bb", bold),
                new TextRun(" cc", DEF)));
        // Words for wrapping: "[bold]aaa" (9), "bb[/]" (5), "cc" (2)
        assertEquals("[bold]aaa\nbb[/] cc", renderer.render(docWith(p), greedy, 10));
    }
}
