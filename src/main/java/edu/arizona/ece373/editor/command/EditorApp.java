
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
import java.util.List;

/**
 * Starts the document editor and connects the command and presentation parts.
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


    public EditorApp() {
        document = new Document();
        parser = new CommandParser();
        searchEngine = new SearchEngine();
        macroRecorder = new MacroRecorder();
        checkpointManager = new CheckpointManager();

        documentView = new DocumentView(document);
        statistics = new DocumentStatistics(document);
        document.addObserver(statistics);
    }

    public Document getDocument() {
        return document;
    }

    // Connects the editing system when it is available
    public void setEditingBridge(EditingBridge bridge) {
        if (bridge == null) {
            throw new IllegalArgumentException(
                    "Editing bridge cannot be null");
        }

        editingBridge = bridge;
    }

    // Sends an editing request to the editing system
    private void executeEdit(CommandParser.ParsedCommand command) {
        if (editingBridge == null) {
            throw new IllegalStateException(
                    "Editing system is not connected yet");
        }

        editingBridge.execute(command);
    }


    // Reads commands entered by the user or provided by a script.
    public void run(Reader input, PrintStream output) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            try {
                CommandParser.ParsedCommand command = parser.parse(line);

                if (command.name().equals("exit")) {
                    output.println("Goodbye!");
                    break;
                }

                execute(command, output);

                // Record a successful command, but not macro controls themselves.
                if (macroRecorder.isRecording()
                        && !command.name().equals("macro")) {
                    macroRecorder.recordCommand(line);
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println("Error: " + e.getMessage());
            }
        }
    }

    // Handles commands after they have been parsed.
    private void execute(CommandParser.ParsedCommand command, PrintStream output) {
        String name = command.name();
        List<String> args = command.arguments();

        switch (name) {
            case "help":
                output.println("Available commands: help, stats, find, "
                        + "checkpoint, macro, view, width, breaker, exit");
                break;

            case "stats":
                output.println("Words: " + statistics.getWordCount());
                output.println("Characters: " + statistics.getCharCount());
                output.println("Paragraphs: " + statistics.getParagraphCount());
                break;

            case "find":
                boolean wholeWord = args.get(1).equals("whole");
                int matches = searchEngine.find(
                        document, args.get(0), wholeWord).size();
                output.println("Matches found: " + matches);
                break;

            case "checkpoint":
                handleCheckpoint(args.get(0), args.get(1), output);
                break;

            case "macro":
                handleMacro(args, output);
                break;

            case "view":
                handleView(args, output);
                break;

            case "width":
                if (args.size() != 1) {
                    throw new IllegalArgumentException("Usage: width number");
                }
                try {
                    int width = Integer.parseInt(args.get(0));
                    documentView.setWidth(width);
                    output.println("Width set to: " + width);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Width must be a number");
                }
                break;

            case "breaker":
                if (args.size() != 1) {
                    throw new IllegalArgumentException("Usage: breaker greedy/balanced");
                }
                if (args.get(0).equals("greedy")) {
                    documentView.setLineBreaker(new GreedyLineBreaker());
                } else if (args.get(0).equals("balanced")) {
                    documentView.setLineBreaker(new BalancedLineBreaker());
                } else {
                    throw new IllegalArgumentException("Unknown line breaker");
                }
                output.println("Line breaker updated.");
                break;


            case "undo":
                if (editingBridge == null) {
                    throw new IllegalStateException(
                            "Editing system is not connected yet");
                }
                editingBridge.undo();
                output.println("Undo completed.");
                break;

            case "redo":
                if (editingBridge == null) {
                    throw new IllegalStateException(
                            "Editing system is not connected yet");
                }
                editingBridge.redo();
                output.println("Redo completed.");
                break;

            case "section":
            case "paragraph":
            case "insert":
            case "delete":
            case "replace":
            case "replaceall":
            case "move":
            case "split":
            case "merge":
            case "format":
                executeEdit(command);
                output.println("Command executed.");
                break;

            default:
                throw new IllegalArgumentException(
                        "Command not implemented yet: " + name);

        }
    }
    private void handleView(List<String> args, PrintStream output) {
        if (args.isEmpty()) {
            output.println(documentView.render());
            return;
        }
        if (args.size() != 1) {
            throw new IllegalArgumentException("Usage: view [plain/markup]");
        }

        if (args.get(0).equals("plain")) {
            documentView.setRenderer(new PlainTextRenderer());
        } else if (args.get(0).equals("markup")) {
            documentView.setRenderer(new MarkupRenderer());
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
        output.println(documentView.render());
    }

    // Stores and restores document snapshots.
    private void handleCheckpoint(String action, String name,
                                  PrintStream output) {
        if (action.equals("save")) {
            checkpointManager.saveCheckpoint(name, document);
            output.println("Checkpoint saved: " + name);
        } else if (action.equals("restore")) {
            // Save the current view options before changing the document.
            Renderer renderer = documentView.getRenderer();
            LineBreaker breaker = documentView.getLineBreaker();
            int width = documentView.getWidth();

            Document restored = checkpointManager.restoreCheckpoint(name);
            document = restored;

            // Noor's view and observer must point at the restored document.
            documentView = new DocumentView(document, renderer, breaker, width);
            statistics = new DocumentStatistics(document);
            document.addObserver(statistics);

            output.println("Checkpoint restored: " + name);
        }
    }

    // Handles macro recording. Replay still needs the editing/history system.
    private void handleMacro(List<String> args, PrintStream output) {
        String action = args.get(0);
        switch (action) {
            case "start":
                macroRecorder.startRecording(args.get(1));
                output.println("Recording started.");
                break;
            case "stop":
                macroRecorder.stopRecording();
                output.println("Recording saved.");
                break;
            case "replay":
                throw new IllegalStateException("Macro replay is not connected yet");
            default:
                throw new IllegalArgumentException("Unknown macro action");
        }
    }

    public static void main(String[] args) throws IOException {
        EditorApp app = new EditorApp();
        if (args.length == 0) {
            System.out.println("Document Editor started.");
            System.out.println("Type help for commands.");
            app.run(new InputStreamReader(System.in), System.out);
        } else if (args.length == 1) {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]))) {
                app.run(reader, System.out);
            }
        } else {
            System.err.println("Usage: EditorApp [script-file]");
        }
    }
}
