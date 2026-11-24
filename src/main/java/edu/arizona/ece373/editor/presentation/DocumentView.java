package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.Document;

import java.util.Objects;

public class DocumentView {

    /** Line width used when none is specified. */
    public static final int DEFAULT_WIDTH = 80;

    private final Document document;
    private Renderer renderer;
    private LineBreaker lineBreaker;
    private int width;

    /**
     * Creates a view of the document using plain text, greedy line
     * breaking, and the default width.
     *
     * @param document the document to display
     * @throws NullPointerException if document is null
     */
    public DocumentView(Document document) {
        this(document, new PlainTextRenderer(), new GreedyLineBreaker(), DEFAULT_WIDTH);
    }

    /**
     * Creates a view of the document with the given settings.
     *
     * @param document    the document to display
     * @param renderer    the rendering view to use
     * @param lineBreaker the line-breaking method to use
     * @param width       the line width, at least 1
     * @throws NullPointerException     if any object argument is null
     * @throws IllegalArgumentException if width is less than 1
     */
    public DocumentView(Document document, Renderer renderer, LineBreaker lineBreaker, int width) {
        this.document = Objects.requireNonNull(document, "document must not be null");
        setRenderer(renderer);
        setLineBreaker(lineBreaker);
        setWidth(width);
    }

    /**
     * Renders the document as it currently is, using the current settings.
     *
     * @return the rendered text
     */
    public String render() {
        return renderer.render(document, lineBreaker, width);
    }

    /**
     * Switches to a different rendering view.
     *
     * @param renderer the view to use from now on
     * @throws NullPointerException if renderer is null
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    /**
     * Switches to a different line-breaking method.
     *
     * @param lineBreaker the method to use from now on
     * @throws NullPointerException if lineBreaker is null
     */
    public void setLineBreaker(LineBreaker lineBreaker) {
        this.lineBreaker = Objects.requireNonNull(lineBreaker, "lineBreaker must not be null");
    }

    /**
     * Changes the line width. The current width is kept if the new one is invalid.
     *
     * @param width the new width, at least 1
     * @throws IllegalArgumentException if width is less than 1
     */
    public void setWidth(int width) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1: " + width);
        }
        this.width = width;
    }

    /**
     * Returns the current rendering view.
     *
     * @return the renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the current line-breaking method.
     *
     * @return the line breaker
     */
    public LineBreaker getLineBreaker() {
        return lineBreaker;
    }

    /**
     * Returns the current line width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }
}