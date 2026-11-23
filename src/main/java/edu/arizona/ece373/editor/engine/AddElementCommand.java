package edu.arizona.ece373.editor.engine;

import java.util.Objects;

/**
 * Adds a section or paragraph to the document or to a section. Undo removes
 * it again.
 */
public class AddElementCommand implements EditCommand {

    private final ElementContainer parent;
    private final DocumentElement element;
    private final int index;

    /**
     * Creates a command that adds an element at the end of the parent.
     *
     * @param parent  where the element goes
     * @param element the section or paragraph to add
     * @throws NullPointerException if parent or element is null
     */
    public AddElementCommand(ElementContainer parent, DocumentElement element) {
        this(parent, element, -1);
    }

    /**
     * Creates a command that adds an element at a given index in the parent.
     *
     * @param parent  where the element goes
     * @param element the section or paragraph to add
     * @param index   the index to add at, or -1 to add at the end
     * @throws NullPointerException if parent or element is null
     */
    public AddElementCommand(ElementContainer parent, DocumentElement element, int index) {
        this.parent = Objects.requireNonNull(parent, "parent must not be null");
        this.element = Objects.requireNonNull(element, "element must not be null");
        this.index = index;
    }

    /**
     * Adds the element.
     *
     * @throws IndexOutOfBoundsException if the index is outside the parent
     * @throws IllegalArgumentException  if a section would end up inside itself
     */
    @Override
    public void execute() {
        int at = index < 0 ? parent.elements().size() : index;
        parent.add(at, element);
    }

    /**
     * Removes the element that was added.
     */
    @Override
    public void undo() {
        parent.remove(parent.indexOf(element));
    }
}
