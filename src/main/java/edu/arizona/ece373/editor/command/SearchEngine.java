
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Searches for text in a document.
 */
public class SearchEngine {

    /**
     * stores where a match was found
     *
     * @param paragraph the paragraph with the match
     * @param start where the match starts
     * @param length how long the match is
     */
    public record SearchMatch(Paragraph paragraph,
                              int start, int length) {
    }

    /**
     * describes one replacement without changing the document
     *
     * @param paragraph the paragraph to change
     * @param start where the match starts
     * @param length how long the match is
     * @param newText the text to put in
     */
    public record Replacement(Paragraph paragraph,
                              int start,
                              int length,
                              String newText) {
    }

    /**
     * plans a replacement for the first match only
     *
     * @param document the document to search
     * @param searchText the text to find
     * @param replacement the new text
     * @param wholeWord true to only match whole words
     * @return the replacement, or an empty list if nothing matched
     */
    public List<Replacement> planReplaceFirst(
            Document document,
            String searchText,
            String replacement,
            boolean wholeWord) {

        if (replacement == null) {
            throw new IllegalArgumentException(
                    "Replacement text cannot be null");
        }

        List<SearchMatch> matches =
                find(document, searchText, wholeWord);

        if (matches.isEmpty()) {
            return List.of();
        }

        SearchMatch first = matches.get(0);

        return List.of(new Replacement(
                first.paragraph(),
                first.start(),
                first.length(),
                replacement));
    }

    /**
     * plans replacements for every match, last match first so earlier positions dont shift
     *
     * @param document the document to search
     * @param searchText the text to find
     * @param replacement the new text
     * @param wholeWord true to only match whole words
     * @return the replacements in reverse order
     */
    public List<Replacement> planReplaceAll(
            Document document,
            String searchText,
            String replacement,
            boolean wholeWord) {

        if (replacement == null) {
            throw new IllegalArgumentException(
                    "Replacement text cannot be null");
        }

        List<SearchMatch> matches =
                find(document, searchText, wholeWord);

        List<Replacement> replacements = new ArrayList<>();

        for (int i = matches.size() - 1; i >= 0; i--) {
            SearchMatch match = matches.get(i);

            replacements.add(new Replacement(
                    match.paragraph(),
                    match.start(),
                    match.length(),
                    replacement));
        }

        return replacements;
    }


    /**
     * searches the whole document including section titles
     *
     * @param document the document to search
     * @param searchText the text to find
     * @param wholeWord true to only match whole words
     * @return every match in document order
     */
    public List<SearchMatch> find(Document document,
                                  String searchText,
                                  boolean wholeWord) {

        Objects.requireNonNull(document, "document cannot be null");

        if (searchText == null || searchText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Search text cannot be empty");
        }

        List<SearchMatch> matches = new ArrayList<>();

        for (DocumentElement element : document.getElements()) {
            searchElement(element, searchText, wholeWord, matches);
        }

        return matches;
    }

    // Searches a paragraph or section
    private void searchElement(DocumentElement element,
                               String searchText,
                               boolean wholeWord,
                               List<SearchMatch> matches) {

        if (element instanceof Paragraph paragraph) {
            searchParagraph(paragraph, searchText, wholeWord, matches);
        }
        else if (element instanceof Section section) {

            // Search the section title
            searchParagraph(section.getTitle(),
                    searchText, wholeWord, matches);

            // Search everything inside the section
            for (DocumentElement child : section.getChildren()) {
                searchElement(child, searchText, wholeWord, matches);
            }
        }
    }

    // Finds matches inside one paragraph
    private void searchParagraph(Paragraph paragraph,
                                 String searchText,
                                 boolean wholeWord,
                                 List<SearchMatch> matches) {

        String text = paragraph.getText();
        int position = 0;

        while (position <= text.length() - searchText.length()) {

            int index = text.indexOf(searchText, position);

            if (index == -1) {
                break;
            }

            int end = index + searchText.length();

            if (!wholeWord || isWholeWord(text, index, end)) {
                matches.add(new SearchMatch(
                        paragraph, index, searchText.length()));
            }

            position = end;
        }
    }

    // Checks whether a match is a complete word
    private boolean isWholeWord(String text, int start, int end) {

        boolean validStart = start == 0
                || !isWordCharacter(text.charAt(start - 1));

        boolean validEnd = end == text.length()
                || !isWordCharacter(text.charAt(end));

        return validStart && validEnd;
    }

    // Letters, numbers, and underscores count as word characters
    private boolean isWordCharacter(char character) {
        return Character.isLetterOrDigit(character)
                || character == '_';
    }
}
