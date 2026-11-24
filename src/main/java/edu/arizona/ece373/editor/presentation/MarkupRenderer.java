package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.TextRun;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the document with visible formatting markers.
 * Each text run whose format differs from CharacterFormat.DEFAULT is wrapped
 * in a tag listing only the differences, such as "[bold]big[/]" or
 * "[italic,color=red]warning[/]". Default text has no tags. Section titles
 * show their formatting the same way.
 * Tags never contain spaces, so line breaking cannot split a tag across
 * lines. Spaces in font names are written as underscores inside tags, for
 * example "font=Times_New_Roman". Tag characters count toward the line width.
 */
public class MarkupRenderer extends TextRenderer {

    /**
     * Returns the paragraph's text with a tag around each run that is not
     * in the default format.
     *
     * @param paragraph the paragraph to convert
     * @return the text with formatting markers
     */
    @Override
    protected String renderRuns(Paragraph paragraph) {
        StringBuilder sb = new StringBuilder();
        for (TextRun run : paragraph.getRuns()) {
            String tag = tagFor(run.getFormat());
            if (tag.isEmpty()) {
                sb.append(run.getText());
            } else {
                sb.append('[').append(tag).append(']')
                        .append(run.getText())
                        .append("[/]");
            }
        }
        return sb.toString();
    }

    /** Lists the attributes that differ from the default, separated by commas. */
    private String tagFor(CharacterFormat format) {
        CharacterFormat def = CharacterFormat.DEFAULT;
        List<String> parts = new ArrayList<>();
        if (format.isBold()) {
            parts.add("bold");
        }
        if (format.isItalic()) {
            parts.add("italic");
        }
        if (!format.getFont().equals(def.getFont())) {
            parts.add("font=" + format.getFont().replace(' ', '_'));
        }
        if (format.getSize() != def.getSize()) {
            parts.add("size=" + format.getSize());
        }
        if (!format.getColor().equals(def.getColor())) {
            parts.add("color=" + format.getColor());
        }
        return String.join(",", parts);
    }
}
