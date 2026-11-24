package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.Document;

/**
 * Converts a document into display text. Each implementation is one
 * rendering view, so new views can be added without changing the document
 * or the editing operations.
 */
public interface Renderer {

    /**
     * Renders the whole document as text, wrapping lines to the given width.
     *
     * @param document the document to render
     * @param breaker  the line-breaking method to use
     * @param width    the maximum line width in characters, at least 1
     * @return the rendered text, with lines separated by newlines
     * @throws IllegalArgumentException if width is less than 1
     */
    String render(Document document, LineBreaker breaker, int width);
}
