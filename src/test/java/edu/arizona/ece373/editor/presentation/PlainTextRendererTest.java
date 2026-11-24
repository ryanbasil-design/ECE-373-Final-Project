package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.Section;
import edu.arizona.ece373.editor.engine.TextRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlainTextRendererTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "black");
    private final CharacterFormat bold = new CharacterFormat(true, false, 12, "Arial", "black");
    private final Renderer renderer = new PlainTextRenderer();
    private final LineBreaker greedy = new GreedyLineBreaker();

    @Test
    void rendersSectionAndParagraphsWithBlankLinesBetween() {
        Section intro = new Section("Intro", plain);
        intro.add(new Paragraph("Hello world", plain));
        Document doc = new Document();
        doc.add(intro);
        doc.add(new Paragraph("The end.", plain));

        assertEquals("1 Intro\n\nHello world\n\nThe end.",
                renderer.render(doc, greedy, 80));
    }

    @Test
    void numbersNestedSectionsByDepth() {
        Section habitat = new Section("Habitat", plain);
        habitat.add(new Paragraph("a", plain));
        Section intro = new Section("Introduction", plain);
        intro.add(habitat);
        intro.add(new Section("Diet", plain));

        Document doc = new Document();
        doc.add(intro);
        doc.add(new Section("Results", plain));

        assertEquals("1 Introduction\n\n1.1 Habitat\n\na\n\n1.2 Diet\n\n2 Results",
                renderer.render(doc, greedy, 80));
    }

    @Test
    void paragraphsBetweenSectionsDoNotAffectNumbering() {
        Document doc = new Document();
        doc.add(new Section("A", plain));
        doc.add(new Paragraph("x", plain));
        doc.add(new Section("B", plain));

        assertEquals("1 A\n\nx\n\n2 B", renderer.render(doc, greedy, 80));
    }

    @Test
    void wrapsParagraphsUsingTheGivenLineBreaker() {
        Document doc = new Document();
        doc.add(new Paragraph("aaa bb cc ddddd", plain));

        assertEquals("aaa bb cc\nddddd", renderer.render(doc, greedy, 9));
        assertEquals("aaa bb\ncc ddddd", renderer.render(doc, new BalancedLineBreaker(), 9));
    }

    @Test
    void formattingIsNotShown() {
        Document doc = new Document();
        doc.add(new Paragraph(List.of(
                new TextRun("Hello ", plain),
                new TextRun("big", bold),
                new TextRun(" world", plain))));

        assertEquals("Hello big world", renderer.render(doc, greedy, 80));
    }

    @Test
    void emptyDocumentRendersAsEmptyString() {
        assertEquals("", renderer.render(new Document(), greedy, 80));
    }

    @Test
    void invalidInputThrows() {
        Document doc = new Document();
        assertThrows(NullPointerException.class, () -> renderer.render(null, greedy, 80));
        assertThrows(NullPointerException.class, () -> renderer.render(doc, null, 80));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(doc, greedy, 0));
    }
}
