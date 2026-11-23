
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
}
