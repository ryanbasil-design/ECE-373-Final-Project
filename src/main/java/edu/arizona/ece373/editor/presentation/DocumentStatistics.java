package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.DocumentObserver;

/**
 * Keeps the word, character, and paragraph counts for a document current
 * by observing it. When the document announces a change, the counts are recalculated,
 * so editing operations never update statistics directly.
 * Counting rules: characters include spaces, section titles count toward
 * words and characters, and section titles are not counted as paragraphs.
 */
public class DocumentStatistics implements DocumentObserver{

    private int wordCount;
    private int charCount;
    private int paragraphCount;

    /**
     * Creates statistics for the given document, calculated immediately so
     * the counts are correct before the first change notification.
     * The caller is responsible for registering this object.
     *
     * @param document  the document to count
     */
    public DocumentStatistics(Document document) {
        recalculate(document);
    }

    /**
     * Recalculates all counts after the document changes.
     *
     * @param document the document that changed
     */
    @Override
    public void onDocumentChanged(Document document) {
        recalculate(document);
    }

    /**
     * Returns the current word count.
     *
     * @return  the number of words
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Returns the current character count, including spaces.
     *
     * @return  the number of characters
     */
    public int getCharCount() {
        return charCount;
    }

    /**
     * Returns the current paragraph count.
     *
     * @return  the number of paragraphs
     */
    public int getParagraphCount() {
        return paragraphCount;
    }

    /**
     * Returns the current statistics as one line ready to display.
     *
     * @return  the report
     */
    public String getReport() {
        return "Words: " + wordCount + ", Characters: " + charCount + ", Paragraphs: " + paragraphCount;
    }

    /** Reads all three counts from the document. */
    private void recalculate(Document document) {
        wordCount = document.countWords();
        charCount = document.countChars();
        paragraphCount = document.countParagraphs();
    }
}
