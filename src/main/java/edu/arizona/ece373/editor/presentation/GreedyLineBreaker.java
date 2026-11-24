package edu.arizona.ece373.editor.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Line breaker than places as many complete words as possible on each
 * line without exceeding the width.
 */
public class GreedyLineBreaker implements LineBreaker {

    /**
     * breaks the text by fitting as many words as possible on each line
     *
     * @param text the text to break
     * @param width the max characters per line
     * @return the lines in order
     */
    @Override
    public List<String> breakLines(String text, int width) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1: " + width);
        }
        List<String> lines = new ArrayList<>();
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            lines.add("");
            return lines;
        }

        StringBuilder line = new StringBuilder();
        for (String word : trimmed.split("\\s+")) {
            if (line.isEmpty()) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= width) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        lines.add(line.toString());
        return lines;
    }
}
