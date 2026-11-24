package edu.arizona.ece373.editor.presentation;

import java.util.List;

/**
 * Splits text into lines no wider than a given width. Implementations
 * define how words are distributed across lines, so new line-breaking
 * methods can be added without changing the renderers or the document.
 * This is the strategy role of the Strategy pattern.
 */
public interface LineBreaker {

    /**
     * Splits text into lines. Words are separated by whitespace, and runs of
     * whitespace are treated as a single space. A word longer than the width
     * is placed on its own line rather than being cut.
     *
     * @param text  the text to split
     * @param width the maximum line width in characters, at least 1
     * @return      the lines, in order; empty text produces one empty line
     * @throws IllegalArgumentException if width is less than 1
     */
    List<String> breakLines(String text, int width);
}
