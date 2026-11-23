
package edu.arizona.ece373.editor.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads editor commands and separates them into arguments.
 */
public class CommandParser {

    private static final Set<String> COMMANDS = Set.of(
            "section", "paragraph", "insert", "delete",
            "replace", "move", "split", "merge",
            "format", "undo", "redo", "checkpoint",
            "find", "replaceall", "macro", "view",
            "width", "breaker", "stats", "help", "exit"
    );

    // Holds the command name and its arguments
    public record ParsedCommand(String name, List<String> arguments) {
        public ParsedCommand {
            arguments = List.copyOf(arguments);
        }
    }

    // Takes a line of user input and parses it
    public ParsedCommand parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Command cannot be empty");
        }

        List<String> words = tokenize(input);

        String name = words.get(0).toLowerCase();

        if (!COMMANDS.contains(name)) {
            throw new IllegalArgumentException(
                    "Unknown command: " + name);
        }

        List<String> arguments =
                new ArrayList<>(words.subList(1, words.size()));

        validate(name, arguments);

        return new ParsedCommand(name, arguments);
    }

    // Splits input while keeping quoted text together
    private List<String> tokenize(String input) {
        List<String> words = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean started = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                started = true;
            }
            else if (Character.isWhitespace(c) && !inQuotes) {
                if (started) {
                    words.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
            }
            else {
                current.append(c);
                started = true;
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException(
                    "Missing closing quotation mark");
        }

        if (started) {
            words.add(current.toString());
        }

        return words;
    }

    // Validates basic command structure
    private void validate(String name, List<String> arguments) {

        if (name.equals("undo") || name.equals("redo")
                || name.equals("stats") || name.equals("help")
                || name.equals("exit")) {

            if (!arguments.isEmpty()) {
                throw new IllegalArgumentException(
                        name + " does not take arguments");
            }
        }

        if (name.equals("checkpoint")) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException(
                        "Usage: checkpoint save/restore name");
            }

            String action = arguments.get(0);

            if (!action.equals("save") && !action.equals("restore")) {
                throw new IllegalArgumentException(
                        "Invalid checkpoint action");
            }

            if (arguments.get(1).isBlank()) {
                throw new IllegalArgumentException(
                        "Checkpoint name cannot be empty");
            }
        }

        if (name.equals("macro")) {
            if (arguments.isEmpty()) {
                throw new IllegalArgumentException(
                        "Macro action is required");
            }

            String action = arguments.get(0);

            if (!Set.of("start", "stop", "replay").contains(action)) {
                throw new IllegalArgumentException(
                        "Invalid macro action");
            }

            int expected = action.equals("stop") ? 1 : 2;

            if (arguments.size() != expected) {
                throw new IllegalArgumentException(
                        "Incorrect macro arguments");
            }

            if (expected == 2 && arguments.get(1).isBlank()) {
                throw new IllegalArgumentException(
                        "Macro name cannot be empty");
            }
        }

        if (name.equals("find")) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException(
                        "Usage: find \"text\" whole/substring");
            }

            if (arguments.get(0).isEmpty()) {
                throw new IllegalArgumentException(
                        "Search text cannot be empty");
            }

            if (!Set.of("whole", "substring")
                    .contains(arguments.get(1))) {
                throw new IllegalArgumentException(
                        "Invalid search mode");
            }
        }
    }
}
