
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.presentation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts the document editor and connects all the parts.
 */
public class EditorApp {

    private Document document;

    private final CommandParser parser;
    private final SearchEngine searchEngine;
    private final MacroRecorder macroRecorder;
    private final CheckpointManager checkpointManager;

    private DocumentView documentView;
    private DocumentStatistics statistics;
    private EditingBridge editingBridge;

    /**
     * makes the editor with an empty document and connects the view and statistics
     */
    public EditorApp() {

        document = new Document();

        parser = new CommandParser();
        searchEngine = new SearchEngine();
        macroRecorder = new MacroRecorder();
        checkpointManager = new CheckpointManager();

        documentView = new DocumentView(document);

        statistics = new DocumentStatistics(document);
        document.addObserver(statistics);

        // Connect Hunter's real editing system
        setEditingBridge(
                new HistoryEditingBridge(document));
    }

    /**
     * gets the document being edited
     *
     * @return the current document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * connects the editing and history system
     *
     * @param bridge the editing bridge to use
     */
    public void setEditingBridge(EditingBridge bridge) {

        if (bridge == null) {
            throw new IllegalArgumentException(
                    "Editing bridge cannot be null");
        }

        this.editingBridge = bridge;
    }

    /**
     * reads commands from the console or a script and runs them until exit
     *
     * @param input where the commands come from
     * @param output where results and errors are printed
     * @throws IOException if the input cant be read
     */
    public void run(
            Reader input,
            PrintStream output) throws IOException {

        BufferedReader reader =
                new BufferedReader(input);

        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isBlank()) {
                continue;
            }

            try {

                CommandParser.ParsedCommand command =
                        parser.parse(line);

                if (command.name().equals("exit")) {
                    output.println("Goodbye!");
                    break;
                }

                execute(command, output);

                // Record successful commands
                if (macroRecorder.isRecording()
                        && !command.name().equals("macro")) {

                    macroRecorder.recordCommand(line);
                }

            }
            catch (IllegalArgumentException
                   | IllegalStateException
                   | IndexOutOfBoundsException e) {

                output.println(
                        "Error: " + e.getMessage());
            }
        }
    }

    // Runs a parsed command
    private void execute(
            CommandParser.ParsedCommand command,
            PrintStream output) {

        String name = command.name();
        List<String> args = command.arguments();

        switch (name) {

            case "help":
                output.println(
                        "Available commands: section, paragraph, "
                                + "insert, delete, replace, replaceall, "
                                + "move, split, merge, format, undo, redo, "
                                + "checkpoint, find, macro, view, width, "
                                + "breaker, stats, help, exit");
                break;

            case "stats":
                output.println(
                        "Words: " + statistics.getWordCount());

                output.println(
                        "Characters: " + statistics.getCharCount());

                output.println(
                        "Paragraphs: "
                                + statistics.getParagraphCount());
                break;

            case "find": {

                boolean wholeWord =
                        args.get(1).equals("whole");

                int matches = searchEngine.find(
                        document,
                        args.get(0),
                        wholeWord).size();

                output.println(
                        "Matches found: " + matches);
                break;
            }

            case "checkpoint":
                handleCheckpoint(
                        args.get(0),
                        args.get(1),
                        output);
                break;

            case "macro":
                handleMacro(args, output);
                break;

            case "view":
                handleView(args, output);
                break;

            case "width": {

                int width =
                        Integer.parseInt(args.get(0));

                documentView.setWidth(width);

                output.println(
                        "Width set to: " + width);
                break;
            }

            case "breaker":

                if (args.get(0).equals("greedy")) {

                    documentView.setLineBreaker(
                            new GreedyLineBreaker());
                }
                else if (args.get(0).equals("balanced")) {

                    documentView.setLineBreaker(
                            new BalancedLineBreaker());
                }
                else {
                    throw new IllegalArgumentException(
                            "Unknown line breaker");
                }

                output.println(
                        "Line breaker updated.");
                break;

            case "replace":
            case "replaceall":
                handleReplace(name, args, output);
                break;

            case "undo":
                requireEditingBridge();

                editingBridge.undo();

                output.println(
                        "Undo completed.");
                break;

            case "redo":
                requireEditingBridge();

                editingBridge.redo();

                output.println(
                        "Redo completed.");
                break;

            case "section":
            case "paragraph":
            case "insert":
            case "delete":
            case "move":
            case "split":
            case "merge":
            case "format":

                requireEditingBridge();

                editingBridge.execute(command);

                output.println(
                        "Command executed.");
                break;

            default:
                throw new IllegalArgumentException(
                        "Command not implemented: " + name);
        }
    }

    // Makes sure the bridge is connected
    private void requireEditingBridge() {

        if (editingBridge == null) {
            throw new IllegalStateException(
                    "Editing system is not connected yet");
        }
    }

    // Displays the document
    private void handleView(
            List<String> args,
            PrintStream output) {

        if (args.isEmpty()) {
            output.println(documentView.render());
            return;
        }

        if (args.get(0).equals("plain")) {

            documentView.setRenderer(
                    new PlainTextRenderer());
        }
        else if (args.get(0).equals("markup")) {

            documentView.setRenderer(
                    new MarkupRenderer());
        }
        else {
            throw new IllegalArgumentException(
                    "Unknown view type");
        }

        output.println(documentView.render());
    }

    // Saves or restores a checkpoint
    private void handleCheckpoint(
            String action,
            String name,
            PrintStream output) {

        if (action.equals("save")) {

            checkpointManager.saveCheckpoint(
                    name, document);

            output.println(
                    "Checkpoint saved: " + name);
        }
        else if (action.equals("restore")) {

            // Save current view settings
            Renderer renderer =
                    documentView.getRenderer();

            LineBreaker breaker =
                    documentView.getLineBreaker();

            int width = documentView.getWidth();

            // Restore saved document
            Document restored =
                    checkpointManager.restoreCheckpoint(name);

            document = restored;

            // Reconnect the presentation layer
            documentView = new DocumentView(
                    document,
                    renderer,
                    breaker,
                    width);

            statistics =
                    new DocumentStatistics(document);

            document.addObserver(statistics);

            // Use a fresh history for the restored document
            setEditingBridge(
                    new HistoryEditingBridge(document));

            output.println(
                    "Checkpoint restored: " + name);
        }
        else {
            throw new IllegalArgumentException(
                    "Unknown checkpoint action");
        }
    }

    // Handles Replace First and Replace All
    private void handleReplace(
            String name,
            List<String> args,
            PrintStream output) {

        if (args.size() != 3) {
            throw new IllegalArgumentException(
                    "Usage: " + name
                            + " \"old text\" \"new text\" whole/substring");
        }

        String oldText = args.get(0);
        String newText = args.get(1);
        String mode = args.get(2);

        if (!mode.equals("whole")
                && !mode.equals("substring")) {

            throw new IllegalArgumentException(
                    "Search mode must be whole or substring");
        }

        boolean wholeWord = mode.equals("whole");

        List<SearchEngine.Replacement> replacements;

        if (name.equals("replace")) {

            replacements = searchEngine.planReplaceFirst(
                    document,
                    oldText,
                    newText,
                    wholeWord);
        }
        else {

            replacements = searchEngine.planReplaceAll(
                    document,
                    oldText,
                    newText,
                    wholeWord);
        }

        if (replacements.isEmpty()) {
            output.println("No matches found.");
            return;
        }

        requireEditingBridge();

        editingBridge.executeReplacements(replacements);

        output.println(
                "Replacements completed: "
                        + replacements.size());
    }

    // Starts, stops, or replays a macro
    private void handleMacro(
            List<String> args,
            PrintStream output) {

        String action = args.get(0);

        switch (action) {

            case "start":
                macroRecorder.startRecording(args.get(1));

                output.println(
                        "Recording started.");
                break;

            case "stop":
                macroRecorder.stopRecording();

                output.println(
                        "Recording saved.");
                break;

            case "replay":
                replayMacro(args.get(1), output);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown macro action");
        }
    }

    // Replays saved macro commands
    private void replayMacro(
            String name,
            PrintStream output) {

        if (macroRecorder.isRecording()) {
            throw new IllegalStateException(
                    "Stop recording before replaying a macro");
        }

        List<String> savedCommands =
                macroRecorder.getCommands(name);

        List<CommandParser.ParsedCommand> commands =
                new ArrayList<>();

        // Parse every command before replay
        for (String line : savedCommands) {
            commands.add(parser.parse(line));
        }

        if (commands.isEmpty()) {
            output.println(
                    "Macro replayed: " + name);
            return;
        }

        boolean allEditing = commands.stream()
                .allMatch(command ->
                        isEditingCommand(command.name()));

        boolean allReadOnly = commands.stream()
                .allMatch(command ->
                        isReadOnlyMacroCommand(command.name()));

        requireEditingBridge();

        if (allEditing) {

            // All edits become one undo-history entry
            editingBridge.executeBatch(commands);
        }
        else if (allReadOnly) {

            for (CommandParser.ParsedCommand command : commands) {
                execute(command, output);
            }
        }
        else if (editingBridge instanceof HistoryEditingBridge historyBridge) {

            // Check supported commands before replay
            for (CommandParser.ParsedCommand command : commands) {

                if (!isEditingCommand(command.name())
                        && !isReadOnlyMacroCommand(command.name())) {

                    throw new IllegalArgumentException(
                            "Unsupported macro command: "
                                    + command.name());
                }
            }

            // Run display commands in order,
            // while recording edits as one undoable action
            historyBridge.executeMacro(
                    commands,
                    command -> execute(command, output));
        }
        else {
            throw new IllegalStateException(
                    "Mixed macro replay needs "
                            + "the real editing bridge");
        }

        output.println(
                "Macro replayed: " + name);
    }

    // Checks whether a command edits the document
    private boolean isEditingCommand(String name) {

        return switch (name) {

            case "section", "paragraph", "insert", "delete",
                 "move", "split", "merge", "format",
                 "replace", "replaceall" -> true;

            default -> false;
        };
    }

    // Commands that do not edit the document
    private boolean isReadOnlyMacroCommand(String name) {

        return switch (name) {

            case "help", "stats", "find", "view",
                 "width", "breaker" -> true;

            default -> false;
        };
    }

    /**
     * starts the editor in console mode, or runs a script if a file is given
     *
     * @param args optional script file path
     * @throws IOException if the script file cant be read
     */
    public static void main(String[] args)
            throws IOException {

        EditorApp app = new EditorApp();

        if (args.length == 0) {

            System.out.println(
                    "Document Editor started.");

            System.out.println(
                    "Type help for commands.");

            app.run(
                    new InputStreamReader(System.in),
                    System.out);
        }
        else if (args.length == 1) {

            try (BufferedReader reader =
                         Files.newBufferedReader(
                                 Path.of(args[0]))) {

                app.run(reader, System.out);
            }
        }
        else {
            System.err.println(
                    "Usage: EditorApp [script-file]");
        }
    }
}
