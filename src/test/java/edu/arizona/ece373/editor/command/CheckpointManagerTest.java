
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CheckpointManagerTest {

    @Test
    void testSaveAndRestore() {
        Document document = new Document();
        Paragraph paragraph = new Paragraph(
                "Hello world", CharacterFormat.DEFAULT);
        document.add(paragraph);

        CheckpointManager manager = new CheckpointManager();
        manager.saveCheckpoint("first", document);

        // Change the original document
        paragraph.insertText(5, " new");

        // Restore the saved version
        Document restored = manager.restoreCheckpoint("first");

        assertEquals("Hello world", restored.getText());
        assertEquals("Hello new world", document.getText());
    }

    @Test
    void testCheckpointExists() {
        Document document = new Document();
        CheckpointManager manager = new CheckpointManager();

        assertFalse(manager.hasCheckpoint("save1"));

        manager.saveCheckpoint("save1", document);

        assertTrue(manager.hasCheckpoint("save1"));
        assertEquals(1, manager.getCheckpointCount());
    }

    @Test
    void testMissingCheckpoint() {
        CheckpointManager manager = new CheckpointManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.restoreCheckpoint("missing");
        });
    }

    @Test
    void testInvalidName() {
        CheckpointManager manager = new CheckpointManager();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.saveCheckpoint("", new Document());
        });
    }

    @Test
    void testNestedSections() {
        Document document = new Document();

        Section section = new Section(
                "Introduction", CharacterFormat.DEFAULT);
        Section subsection = new Section(
                "Background", CharacterFormat.DEFAULT);

        subsection.add(new Paragraph(
                "Some information", CharacterFormat.DEFAULT));
        section.add(subsection);
        document.add(section);

        CheckpointManager manager = new CheckpointManager();
        manager.saveCheckpoint("nested", document);

        // Remove the original section
        document.remove(0);

        Document restored = manager.restoreCheckpoint("nested");

        assertEquals(1, restored.getElements().size());
        assertEquals(1, restored.countParagraphs());
        assertEquals(4, restored.countWords());
    }
}
