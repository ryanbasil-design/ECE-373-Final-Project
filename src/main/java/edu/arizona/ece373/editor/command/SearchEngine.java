
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Searches for text in a document.
 */
public class SearchEngine {

    // Stores where a match was found
    public record SearchMatch(Paragraph paragraph,
                              int start, int length) {
    }

    // Searches the entire document
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
