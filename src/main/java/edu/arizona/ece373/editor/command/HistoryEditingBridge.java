
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.function.Consumer;

/**
 * Connects editor commands to the editing history system.
 */
public class HistoryEditingBridge implements EditingBridge {

    private final Document document;
    private final CommandHistory history;

    public HistoryEditingBridge(Document document) {
        if (document == null) {
            throw new IllegalArgumentException(
                    "Document cannot be null");
        }

        this.document = document;
        this.history = new CommandHistory(document);
    }

    // Executes one editing command
    @Override
    public void execute(CommandParser.ParsedCommand command) {
        EditCommand edit = createCommand(command);
        history.execute(edit);
    }

    // Executes multiple commands as one undoable operation
    @Override
    public void executeBatch(
            List<CommandParser.ParsedCommand> commands) {

        if (commands == null || commands.isEmpty()) {
            return;
        }

        // Resolve commands when they run, not in advance.
        // This lets a macro create a paragraph and edit it.
        history.execute(
                new RecordedSequenceCommand(commands, null));
    }

    // Replays editing and display commands in original order
    public void executeMacro(
            List<CommandParser.ParsedCommand> commands,
            Consumer<CommandParser.ParsedCommand> displayCommand) {

        if (commands == null || commands.isEmpty()) {
            return;
        }

        history.execute(
                new RecordedSequenceCommand(
                        commands, displayCommand));
    }

    // Executes planned replacements as one history entry
    @Override
    public void executeReplacements(
            List<SearchEngine.Replacement> replacements) {

        if (replacements == null || replacements.isEmpty()) {
            return;
        }

        List<EditCommand> edits = new ArrayList<>();

        for (SearchEngine.Replacement replacement : replacements) {
            Paragraph paragraph = replacement.paragraph();
            int start = replacement.start();
            int end = start + replacement.length();

            if (!containsParagraph(
                    document.getElements(), paragraph)) {
                throw new IllegalArgumentException(
                        "Replacement paragraph is not in the document");
            }

            edits.add(new DeleteTextCommand(
                    paragraph, start, end));

            edits.add(new InsertTextCommand(
                    paragraph, start, replacement.newText()));
        }

        history.execute(new CompositeCommand(edits));
    }

    // Converts a parsed command into an editing command
    private EditCommand createCommand(
            CommandParser.ParsedCommand command) {

        List<String> args = command.arguments();

        switch (command.name()) {

            case "paragraph": {
                ElementContainer parent =
                        chooseParent(args, "paragraph");

                return new AddElementCommand(
                        parent,
                        new Paragraph(
                                args.get(0),
                                CharacterFormat.DEFAULT));
            }

            case "section": {
                ElementContainer parent =
                        chooseParent(args, "section");

                return new AddElementCommand(
                        parent,
                        new Section(
                                args.get(0),
                                CharacterFormat.DEFAULT));
            }

            case "insert": {
                int paragraphIndex =
                        Integer.parseInt(args.get(0));

                int position =
                        Integer.parseInt(args.get(1));

                Paragraph paragraph =
                        getParagraph(paragraphIndex);

                return new InsertTextCommand(
                        paragraph, position, args.get(2));
            }

            case "delete": {
                int paragraphIndex =
                        Integer.parseInt(args.get(0));

                int start =
                        Integer.parseInt(args.get(1));

                int end =
                        Integer.parseInt(args.get(2));

                Paragraph paragraph =
                        getParagraph(paragraphIndex);

                return new DeleteTextCommand(
                        paragraph, start, end);
            }

            case "split": {
                int paragraphIndex =
                        Integer.parseInt(args.get(0));

                int position =
                        Integer.parseInt(args.get(1));

                Paragraph paragraph =
                        getParagraph(paragraphIndex);

                ElementContainer parent =
                        findParent(document, paragraph);

                return new SplitParagraphCommand(
                        parent, paragraph, position);
            }

            case "merge": {
                int paragraphIndex =
                        Integer.parseInt(args.get(0));

                Paragraph paragraph =
                        getParagraph(paragraphIndex);

                ElementContainer parent =
                        findParent(document, paragraph);

                return new MergeParagraphsCommand(
                        parent, paragraph);
            }

            case "format": {
                if (args.size() < 4 || args.size() > 6) {
                    throw new IllegalArgumentException(
                            "Usage: format [title] index "
                                    + "start end style [value]");
                }

                boolean title = args.get(0).equals("title");
                int offset = title ? 1 : 0;

                if ((title
                        && args.size() != 5
                        && args.size() != 6)
                        || (!title
                        && args.size() != 4
                        && args.size() != 5)) {

                    throw new IllegalArgumentException(
                            "Invalid format arguments");
                }

                int paragraphIndex =
                        parseIndex(args.get(offset));

                int start =
                        parseIndex(args.get(offset + 1));

                int end =
                        parseIndex(args.get(offset + 2));

                String style =
                        args.get(offset + 3).toLowerCase();

                Paragraph paragraph = title
                        ? getSection(paragraphIndex).getTitle()
                        : getParagraph(paragraphIndex);

                UnaryOperator<CharacterFormat> change;

                switch (style) {

                    case "bold":
                    case "italic":
                    case "unbold":
                    case "unitalic": {

                        if (args.size() != offset + 4) {
                            throw new IllegalArgumentException(
                                    "This style does not take a value");
                        }

                        change = old -> new CharacterFormat(
                                style.equals("bold")
                                        ? true
                                        : style.equals("unbold")
                                        ? false
                                        : old.isBold(),

                                style.equals("italic")
                                        ? true
                                        : style.equals("unitalic")
                                        ? false
                                        : old.isItalic(),

                                old.getSize(),
                                old.getFont(),
                                old.getColor());
                        break;
                    }

                    case "font": {
                        String font =
                                requireFormatValue(
                                        args, style, offset);

                        change = old -> new CharacterFormat(
                                old.isBold(),
                                old.isItalic(),
                                old.getSize(),
                                font,
                                old.getColor());
                        break;
                    }

                    case "color": {
                        String color =
                                requireFormatValue(
                                        args, style, offset);

                        change = old -> new CharacterFormat(
                                old.isBold(),
                                old.isItalic(),
                                old.getSize(),
                                old.getFont(),
                                color);
                        break;
                    }

                    case "size": {
                        String value =
                                requireFormatValue(
                                        args, style, offset);

                        int size;

                        try {
                            size = Integer.parseInt(value);
                        }
                        catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "Font size must be a positive integer");
                        }

                        if (size <= 0) {
                            throw new IllegalArgumentException(
                                    "Font size must be a positive integer");
                        }

                        final int newSize = size;

                        change = old -> new CharacterFormat(
                                old.isBold(),
                                old.isItalic(),
                                newSize,
                                old.getFont(),
                                old.getColor());
                        break;
                    }

                    default:
                        throw new IllegalArgumentException(
                                "Unknown format: " + style);
                }

                return new ApplyFormatCommand(
                        paragraph, start, end, change);
            }

            case "move": {
                if (args.size() != 5) {
                    throw new IllegalArgumentException(
                            "Usage: move sourceParagraph "
                                    + "start end targetParagraph position");
                }

                Paragraph source =
                        getParagraph(parseIndex(args.get(0)));

                int start =
                        parseIndex(args.get(1));

                int end =
                        parseIndex(args.get(2));

                Paragraph target =
                        getParagraph(parseIndex(args.get(3)));

                int position =
                        parseIndex(args.get(4));

                return new MoveTextCommand(
                        source, start, end, target, position);
            }

            default:
                throw new IllegalArgumentException(
                        "Editing command not connected yet: "
                                + command.name());
        }
    }

    // Reads a value for font, size, or color
    private String requireFormatValue(
            List<String> args,
            String style,
            int offset) {

        if (args.size() != offset + 5
                || args.get(offset + 4).isBlank()) {

            throw new IllegalArgumentException(
                    "Usage: format [title] index "
                            + "start end " + style + " value");
        }

        return args.get(offset + 4);
    }

    // Parent may be the document or a selected section
    private ElementContainer chooseParent(
            List<String> args,
            String commandName) {

        if (args.size() == 1) {
            return ElementContainer.of(document);
        }

        if (args.size() != 3
                || !args.get(1).equals("inside")) {

            throw new IllegalArgumentException(
                    "Usage: " + commandName
                            + " \"text\" [inside sectionIndex]");
        }

        return ElementContainer.of(
                getSection(parseIndex(args.get(2))));
    }

    // Finds a section by its preorder index
    private Section getSection(int index) {

        List<Section> sections = new ArrayList<>();

        collectSections(
                document.getElements(), sections);

        if (index < 0 || index >= sections.size()) {
            throw new IllegalArgumentException(
                    "Section index does not exist: " + index);
        }

        return sections.get(index);
    }

    // Collects sections, including nested sections
    private void collectSections(
            List<DocumentElement> elements,
            List<Section> sections) {

        for (DocumentElement element : elements) {

            if (element instanceof Section section) {
                sections.add(section);

                collectSections(
                        section.getChildren(), sections);
            }
        }
    }

    /**
     * Runs macro commands in order.
     * All document edits are undone as one operation.
     */
    private class RecordedSequenceCommand
            implements EditCommand {

        private final List<CommandParser.ParsedCommand> commands;
        private final Consumer<CommandParser.ParsedCommand> display;

        private List<EditCommand> completed = List.of();

        RecordedSequenceCommand(
                List<CommandParser.ParsedCommand> commands,
                Consumer<CommandParser.ParsedCommand> display) {

            this.commands = List.copyOf(commands);
            this.display = display;
        }

        @Override
        public void execute() {

            List<EditCommand> done = new ArrayList<>();

            try {
                for (CommandParser.ParsedCommand cmd : commands) {

                    String name = cmd.name();

                    if (name.equals("replace")
                            || name.equals("replaceall")) {

                        List<String> args = cmd.arguments();
                        SearchEngine search = new SearchEngine();

                        List<SearchEngine.Replacement> plan;

                        if (name.equals("replace")) {
                            plan = search.planReplaceFirst(
                                    document,
                                    args.get(0),
                                    args.get(1),
                                    args.get(2).equals("whole"));
                        }
                        else {
                            plan = search.planReplaceAll(
                                    document,
                                    args.get(0),
                                    args.get(1),
                                    args.get(2).equals("whole"));
                        }

                        for (SearchEngine.Replacement replacement : plan) {

                            EditCommand remove =
                                    new DeleteTextCommand(
                                            replacement.paragraph(),
                                            replacement.start(),
                                            replacement.start()
                                                    + replacement.length());

                            remove.execute();
                            done.add(remove);

                            EditCommand insert =
                                    new InsertTextCommand(
                                            replacement.paragraph(),
                                            replacement.start(),
                                            replacement.newText());

                            insert.execute();
                            done.add(insert);
                        }
                    }
                    else if (isEditingName(name)) {

                        EditCommand edit = createCommand(cmd);

                        edit.execute();
                        done.add(edit);
                    }
                    else if (display != null) {

                        // Update statistics before displaying them
                        document.notifyObservers();
                        display.accept(cmd);
                    }
                    else {
                        throw new IllegalArgumentException(
                                "Unsupported macro command: " + name);
                    }
                }

                completed = done;
            }
            catch (RuntimeException e) {

                // Reverse changes if the macro fails
                for (int i = done.size() - 1; i >= 0; i--) {
                    done.get(i).undo();
                }

                document.notifyObservers();
                throw e;
            }
        }

        @Override
        public void undo() {

            for (int i = completed.size() - 1; i >= 0; i--) {
                completed.get(i).undo();
            }
        }
    }

    private boolean isEditingName(String name) {

        return switch (name) {

            case "paragraph", "section", "insert", "delete",
                 "split", "merge", "move", "format" -> true;

            default -> false;
        };
    }

    // Converts an argument to a non-negative index
    private int parseIndex(String value) {

        try {
            int index = Integer.parseInt(value);

            if (index < 0) {
                throw new IllegalArgumentException(
                        "Index cannot be negative");
            }

            return index;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Index must be a number");
        }
    }

    // Finds a paragraph by its document-wide index
    private Paragraph getParagraph(int index) {

        List<Paragraph> paragraphs = new ArrayList<>();

        collectParagraphs(
                document.getElements(), paragraphs);

        if (index < 0 || index >= paragraphs.size()) {
            throw new IllegalArgumentException(
                    "Paragraph index does not exist: " + index);
        }

        return paragraphs.get(index);
    }

    // Collects paragraphs inside nested sections
    private void collectParagraphs(
            List<DocumentElement> elements,
            List<Paragraph> paragraphs) {

        for (DocumentElement element : elements) {

            if (element instanceof Paragraph paragraph) {
                paragraphs.add(paragraph);
            }
            else if (element instanceof Section section) {
                collectParagraphs(
                        section.getChildren(), paragraphs);
            }
        }
    }

    // Checks whether a paragraph is in the document
    private boolean containsParagraph(
            List<DocumentElement> elements,
            Paragraph target) {

        for (DocumentElement element : elements) {

            if (element == target) {
                return true;
            }

            if (element instanceof Section section) {

                if (section.getTitle() == target) {
                    return true;
                }

                if (containsParagraph(
                        section.getChildren(), target)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Finds the parent of a paragraph
    private ElementContainer findParent(
            Document document,
            Paragraph paragraph) {

        if (document.getElements().contains(paragraph)) {
            return ElementContainer.of(document);
        }

        for (DocumentElement element : document.getElements()) {

            if (element instanceof Section section) {

                ElementContainer result =
                        findParentInSection(section, paragraph);

                if (result != null) {
                    return result;
                }
            }
        }

        throw new IllegalArgumentException(
                "Paragraph does not belong to the document");
    }

    // Searches nested sections
    private ElementContainer findParentInSection(
            Section section,
            Paragraph paragraph) {

        if (section.getChildren().contains(paragraph)) {
            return ElementContainer.of(section);
        }

        for (DocumentElement element : section.getChildren()) {

            if (element instanceof Section childSection) {

                ElementContainer result =
                        findParentInSection(
                                childSection, paragraph);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Moves formatted text using the original positions.
     * The entire move is one undoable operation.
     */
    private static class MoveTextCommand
            implements EditCommand {

        private final Paragraph source;
        private final Paragraph target;
        private final int start;
        private final int end;
        private final int destination;

        private List<TextRun> oldSource;
        private List<TextRun> oldTarget;

        MoveTextCommand(
                Paragraph source,
                int start,
                int end,
                Paragraph target,
                int destination) {

            this.source = source;
            this.target = target;
            this.start = start;
            this.end = end;
            this.destination = destination;
        }

        @Override
        public void execute() {

            int sourceLength = source.countChars();
            int targetLength = target.countChars();

            // Validate before modifying the document
            if (start < 0
                    || end < start
                    || end > sourceLength
                    || destination < 0
                    || destination > targetLength) {

                throw new IllegalArgumentException(
                        "Move range or destination is invalid");
            }

            oldSource = source.getRuns();

            oldTarget = target == source
                    ? oldSource
                    : target.getRuns();

            // Moving an empty range changes nothing
            if (start == end || (source == target
                    && destination >= start
                    && destination <= end)) {
                return;
            }

            List<TextRun> selected =
                    source.getRuns(start, end);

            List<TextRun> sourceWithout = new ArrayList<>();

            sourceWithout.addAll(
                    source.getRuns(0, start));

            sourceWithout.addAll(
                    source.getRuns(end, sourceLength));

            if (source == target) {

                // Destination refers to the original text
                int adjusted = destination > end
                        ? destination - (end - start)
                        : destination;

                source.setRuns(sourceWithout);
                source.insertRuns(adjusted, selected);
            }
            else {

                List<TextRun> newTarget = new ArrayList<>();

                newTarget.addAll(
                        target.getRuns(0, destination));

                newTarget.addAll(selected);

                newTarget.addAll(
                        target.getRuns(
                                destination, targetLength));

                source.setRuns(sourceWithout);
                target.setRuns(newTarget);
            }
        }

        @Override
        public void undo() {

            if (oldSource == null) {
                throw new IllegalStateException(
                        "Move was not executed");
            }

            source.setRuns(oldSource);

            if (target != source) {
                target.setRuns(oldTarget);
            }
        }
    }

    @Override
    public void undo() {
        history.undo();
    }

    @Override
    public void redo() {
        history.redo();
    }
}
