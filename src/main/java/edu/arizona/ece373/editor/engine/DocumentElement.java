package edu.arizona.ece373.editor.engine;

/**
 * Abstract parent of every element in the document tree.
 * Section and Paragraph both extend this class, which lets
 * a Section hold an ordered list of mixed children (paragraphs
 * and nested sections) and lets other components such as statistics,
 * search, and rendering treat every element the same way. This
 * is the component role of the Composite pattern
 */
public abstract class DocumentElement {

    /**
     * Returns the plain text content of this element.
     *
     * @return the text of this element
     */
    public abstract String getText();

    /**
     * Returns the number of words in this element.
     *
     * @return the word count
     */
    public abstract int countWords();

    /**
     * Returns the number of characters in this element, including spaces.
     *
     * @return the character count
     */
    public abstract int countChars();

    /**
     * Returns the number of paragraphs in this element.
     *
     * @return the paragraph count
     */
    public abstract int countParagraphs();
}
