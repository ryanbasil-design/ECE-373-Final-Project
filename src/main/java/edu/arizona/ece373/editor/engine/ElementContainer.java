package edu.arizona.ece373.editor.engine;

import java.util.List;
import java.util.Objects;

/**
 * Something that holds an ordered list of document elements: either the
 * top level of a Document or a Section. Editing commands use this so the
 * same command works whether a paragraph is at the top level or nested
 * inside a section.
 */
public interface ElementContainer {

    /**
     * Returns the elements held, in order.
     *
     * @return a read-only list of the elements
     */
    List<DocumentElement> elements();

    /**
     * Inserts an element at the given index.
     *
     * @param index   where to insert, from 0 up to the number of elements
     * @param element the element to insert
     */
    void add(int index, DocumentElement element);

    /**
     * Removes and returns the element at the given index.
     *
     * @param index the index of the element to remove
     * @return the removed element
     */
    DocumentElement remove(int index);

    /**
     * Returns the index of an element, or -1 if it is not held here.
     *
     * @param element the element to look for
     * @return its index, or -1
     */
    int indexOf(DocumentElement element);

    /**
     * Wraps the top level of a document.
     *
     * @param document the document
     * @return a container for the document's top-level elements
     * @throws NullPointerException if document is null
     */
    static ElementContainer of(Document document) {
        Objects.requireNonNull(document, "document must not be null");
        return new ElementContainer() {
            /**
             * gets the elements in order
             *
             * @return the elements
             */
            public List<DocumentElement> elements() {
                return document.getElements();
            }

            /**
             * adds an element at an index
             *
             * @param index where to add it
             * @param element the element to add
             */
            public void add(int index, DocumentElement element) {
                document.add(index, element);
            }

            /**
             * removes the element at an index
             *
             * @param index the index to remove
             * @return the removed element
             */
            public DocumentElement remove(int index) {
                return document.remove(index);
            }

            /**
             * finds the index of an element
             *
             * @param element the element to look for
             * @return its index, or -1
             */
            public int indexOf(DocumentElement element) {
                return document.indexOf(element);
            }
        };
    }

    /**
     * Wraps the children of a section.
     *
     * @param section the section
     * @return a container for the section's children
     * @throws NullPointerException if section is null
     */
    static ElementContainer of(Section section) {
        Objects.requireNonNull(section, "section must not be null");
        return new ElementContainer() {
            /**
             * gets the elements in order
             *
             * @return the elements
             */
            public List<DocumentElement> elements() {
                return section.getChildren();
            }

            /**
             * adds an element at an index
             *
             * @param index where to add it
             * @param element the element to add
             */
            public void add(int index, DocumentElement element) {
                section.add(index, element);
            }

            /**
             * removes the element at an index
             *
             * @param index the index to remove
             * @return the removed element
             */
            public DocumentElement remove(int index) {
                return section.remove(index);
            }

            /**
             * finds the index of an element
             *
             * @param element the element to look for
             * @return its index, or -1
             */
            public int indexOf(DocumentElement element) {
                return section.indexOf(element);
            }
        };
    }
}
