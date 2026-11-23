
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchEngineTest {

    @Test
    void testSubstringSearch() {
        Document document = new Document();
        document.add(new Paragraph(
                "Java JavaScript Java", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        List<SearchEngine.SearchMatch> matches =
                engine.find(document, "Java", false);

        assertEquals(3, matches.size());
        assertEquals(0, matches.get(0).start());
        assertEquals(5, matches.get(1).start());
        assertEquals(16, matches.get(2).start());
    }

    @Test
    void testWholeWordSearch() {
        Document document = new Document();
        document.add(new Paragraph(
                "Java JavaScript Java", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        List<SearchEngine.SearchMatch> matches =
                engine.find(document, "Java", true);

        assertEquals(2, matches.size());
    }

    @Test
    void testNoMatches() {
        Document document = new Document();
        document.add(new Paragraph(
                "Hello world", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        assertTrue(engine.find(document, "Python", false).isEmpty());
    }

    @Test
    void testNestedSections() {
        Document document = new Document();

        Section section = new Section(
                "Introduction", CharacterFormat.DEFAULT);

        Section subsection = new Section(
                "Background", CharacterFormat.DEFAULT);

        subsection.add(new Paragraph(
                "Java is useful", CharacterFormat.DEFAULT));

        section.add(subsection);
        document.add(section);

        SearchEngine engine = new SearchEngine();

        assertEquals(1,
                engine.find(document, "Java", true).size());

        assertEquals(1,
                engine.find(document, "Background", true).size());
    }

    @Test
    void testInvalidSearch() {
        SearchEngine engine = new SearchEngine();

        assertThrows(IllegalArgumentException.class,
                () -> engine.find(new Document(), "", false));
    }

    @Test
    void testPunctuation() {
        Document document = new Document();
        document.add(new Paragraph(
                "Java, Java! JavaScript", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        assertEquals(2,
                engine.find(document, "Java", true).size());
    }

    @Test
    void testPlanReplaceFirst() {
        Document document = new Document();
        document.add(new Paragraph(
                "Java and Java", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        var replacements = engine.planReplaceFirst(
                document, "Java", "Python", true);

        assertEquals(1, replacements.size());
        assertEquals(0, replacements.get(0).start());
        assertEquals("Python", replacements.get(0).newText());

        // Planning should not change the document
        assertEquals("Java and Java", document.getText());
    }

    @Test
    void testPlanReplaceAll() {
        Document document = new Document();
        document.add(new Paragraph(
                "Java and Java", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        var replacements = engine.planReplaceAll(
                document, "Java", "Python", true);

        assertEquals(2, replacements.size());

        // Later match comes first
        assertEquals(9, replacements.get(0).start());
        assertEquals(0, replacements.get(1).start());

        assertEquals("Java and Java", document.getText());
    }

    @Test
    void testReplaceNoMatches() {
        Document document = new Document();
        document.add(new Paragraph(
                "Hello world", CharacterFormat.DEFAULT));

        SearchEngine engine = new SearchEngine();

        var replacements = engine.planReplaceAll(
                document, "Java", "Python", true);

        assertTrue(replacements.isEmpty());
        assertEquals("Hello world", document.getText());
    }

}
