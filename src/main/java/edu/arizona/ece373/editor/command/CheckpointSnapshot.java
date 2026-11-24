
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves a copy of a document so it can be restored later.
 */
public class CheckpointSnapshot {

    private final Document savedDocument;

    /**
     * makes a snapshot by copying the whole document
     *
     * @param document the document to copy
     */
    public CheckpointSnapshot(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        savedDocument = copyDocument(document);
    }

    /**
     * gets a separate copy of the saved document so the snapshot doesnt change
     *
     * @return a copy of the saved document
     */
    public Document getDocument() {
        return copyDocument(savedDocument);
    }

    // Copies the entire document
    private static Document copyDocument(Document original) {
        Document copy = new Document();

        for (DocumentElement element : original.getElements()) {
            copy.add(copyElement(element));
        }

        return copy;
    }

    // Copies either a paragraph or a section
    private static DocumentElement copyElement(DocumentElement element) {
        if (element instanceof Paragraph paragraph) {
            return copyParagraph(paragraph);
        }

        if (element instanceof Section section) {
            Section copy = new Section(
                    copyParagraph(section.getTitle())
            );

            for (DocumentElement child : section.getChildren()) {
                copy.add(copyElement(child));
            }

            return copy;
        }

        throw new IllegalArgumentException(
                "Unknown document element"
        );
    }

    // Copies a paragraph and keeps its formatting
    private static Paragraph copyParagraph(Paragraph paragraph) {
        List<TextRun> runs = new ArrayList<>();

        for (TextRun run : paragraph.getRuns()) {
            runs.add(new TextRun(
                    run.getText(),
                    run.getFormat()
            ));
        }

        return new Paragraph(runs);
    }
}
