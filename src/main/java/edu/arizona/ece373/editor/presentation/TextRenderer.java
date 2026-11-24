package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.DocumentElement;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for renderers that walk the document tree and produce text.
 */
public abstract class TextRenderer implements Renderer {

    /**
     * renders the whole document by walking through every section and paragraph
     *
     * @param document the document to render
     * @param breaker how lines get broken
     * @param width the max characters per line
     * @return the rendered text
     */
    @Override
    public final String render(Document document, LineBreaker breaker, int width) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(breaker, "breaker must not be null");
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1: " + width);
        }

        List<String> blocks = new ArrayList<>();
        renderElements(document.getElements(), "", breaker, width, blocks);
        return String.join("\n\n", blocks);
    }

    /**
     * Converts a paragraph's text runs into the text for this view.
     * Used for both paragraphs and section titles.
     *
     * @param paragraph the paragraph to convert
     * @return the paragraph's text as this view displays it, before wrapping
     */
    protected abstract String renderRuns(Paragraph paragraph);

    /** Renders a list of elements, numbering sections under the given prefix. */
    private void renderElements(List<DocumentElement> elements, String prefix,
                                LineBreaker breaker, int width, List<String> blocks) {
        int sectionNumber = 1;
        for (DocumentElement element : elements) {
            if (element instanceof Section section) {
                String number;
                if (prefix.isEmpty()) {
                    number = String.valueOf(sectionNumber);
                } else {
                    number = prefix + "." + sectionNumber;
                }
                sectionNumber++;
                blocks.add(wrap(number + " " + renderRuns(section.getTitle()), breaker, width));
                renderElements(section.getChildren(), number, breaker, width, blocks);
            } else if (element instanceof Paragraph paragraph) {
                blocks.add(wrap(renderRuns(paragraph), breaker, width));
            }
        }
    }

    /** Wraps one block of text and joins its lines with newlines. */
    private String wrap(String text, LineBreaker breaker, int width) {
        return String.join("\n", breaker.breakLines(text, width));
    }
}
