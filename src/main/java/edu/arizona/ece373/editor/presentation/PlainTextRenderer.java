package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.DocumentElement;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Renders the document as plain text with no formatting markers.
 * Section titles are numbered by depth, such as "1 Introduction"
 * and "1.1 Habitat", so nesting stays visible. Each title
 * and paragraph is wrapped separately and separated by a blank line.
 */
public class PlainTextRenderer extends TextRenderer {

    /**
     * Returns the paragraph's text with all formatting left out.
     *
     * @param paragraph the paragraph to convert
     * @return the plain text
     */
    @Override
    protected String renderRuns(Paragraph paragraph) {
        return paragraph.getText();
    }
}
