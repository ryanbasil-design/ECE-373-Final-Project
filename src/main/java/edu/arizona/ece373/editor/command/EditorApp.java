
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main application for the document editor.
 * Reads commands and connects the editor components.
 */
public class EditorApp {

    private Document document;
    private final CommandParser parser;
    private final SearchEngine searchEngine;
    private final MacroRecorder macroRecorder;
    private final CheckpointManager checkpointManager;

    public EditorApp() {
        document = new Document();
        parser = new CommandParser();
        searchEngine = new SearchEngine();
        macroRecorder = new MacroRecorder();
        checkpointManager = new CheckpointManager();
    }

    public Document getDocument() {
        return document;
    }

    // Reads commands from a console or script
    public void run(Reader input, PrintStream output)
            throws IOException {

        BufferedReader reader = new BufferedReader(input);
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

                // Save successful commands during recording
                if (macroRecorder.isRecording()
                        && !command.name().equals("macro")) {
                    macroRecorder.recordCommand(line);
                }

            } catch (IllegalArgumentException |
                     IllegalStateException e) {
                output.println("Error: " + e.getMessage());
            }
        }
    }

    // Handles a command after parsing
    private void execute(CommandParser.ParsedCommand command,
                         PrintStream output) {

        String name = command.name();
        var args = command.arguments();

        switch (name) {

            case "help":
                output.println(
                        "Available commands: help, stats, find, "
                                + "checkpoint, macro, exit");
                break;

            case "stats":
                output.println(
                        "Words: " + document.countWords());
                output.println(
                        "Characters: " + document.countChars());
                output.println(
                        "Paragraphs: " + document.countParagraphs());
                break;

            case "find":
                boolean wholeWord =
                        args.get(1).equals("whole");

                int matches = searchEngine.find(
                        document, args.get(0), wholeWord).size();

                output.println("Matches found: " + matches);
                break;

            case "checkpoint":
                handleCheckpoint(args.get(0),
                        args.get(1), output);
                break;

            case "macro":
                handleMacro(args, output);
                break;

            default:
                throw new IllegalArgumentException(
                        "Command not implemented yet: " + name);
        }
    }

    // Saves and restores named checkpoints
    private void handleCheckpoint(String action,
                                  String name,
                                  PrintStream output) {

        if (action.equals("save")) {
            checkpointManager.saveCheckpoint(name, document);
            output.println("Checkpoint saved: " + name);
        }
        else if (action.equals("restore")) {
            document = checkpointManager.restoreCheckpoint(name);
            output.println("Checkpoint restored: " + name);
        }
    }

    // Handles macro recording
    private void handleMacro(java.util.List<String> args,
                             PrintStream output) {

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
                throw new IllegalStateException(
                        "Macro replay is not connected yet");

            default:
                throw new IllegalArgumentException(
                        "Unknown macro action");
        }
    }

    // Starts the application
    public static void main(String[] args) throws IOException {

        EditorApp app = new EditorApp();

        if (args.length == 0) {
            System.out.println("Document Editor started.");
            System.out.println("Type help for commands.");

            app.run(new InputStreamReader(System.in), System.out);
        }
        else if (args.length == 1) {
            try (BufferedReader reader =
                         Files.newBufferedReader(Path.of(args[0]))) {
                app.run(reader, System.out);
            }
        }
        else {
            System.err.println(
                    "Usage: EditorApp [script-file]");
        }
    }
}
