
package edu.arizona.ece373.editor.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads editor commands and checks their arguments.
 */
public class CommandParser {

    private static final Set<String> COMMANDS = Set.of(
            "section", "paragraph", "insert", "delete",
            "replace", "replaceall", "move", "split",
            "merge", "format", "undo", "redo",
            "checkpoint", "find", "macro", "view",
            "width", "breaker", "stats", "help", "exit"
    );

    // Holds the command and its arguments
    public record ParsedCommand(String name, List<String> arguments) {
        public ParsedCommand {
            arguments = List.copyOf(arguments);
        }
    }

    // Converts user input into a command
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

    // Splits input without splitting text inside quotes
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

    // Checks the arguments for each command
    private void validate(String name, List<String> args) {

        switch (name) {

            case "help":
            case "stats":
            case "undo":
            case "redo":
            case "exit":
                requireCount(name, args, 0);
                break;

            case "checkpoint":
                requireCount(name, args, 2);

                if (!args.get(0).equals("save")
                        && !args.get(0).equals("restore")) {
                    throw new IllegalArgumentException(
                            "Invalid checkpoint action");
                }

                requireNonBlank(args.get(1),
                        "Checkpoint name");
                break;

            case "macro":
                validateMacro(args);
                break;

            case "find":
                requireCount(name, args, 2);
                requireNonBlank(args.get(0), "Search text");
                validateSearchMode(args.get(1));
                break;

            case "replace":
            case "replaceall":
                requireCount(name, args, 3);
                requireNonBlank(args.get(0), "Search text");
                validateSearchMode(args.get(2));
                break;

            case "view":
                if (args.size() > 1) {
                    throw new IllegalArgumentException(
                            "Usage: view [plain/markup]");
                }

                if (!args.isEmpty()
                        && !Set.of("plain", "markup")
                        .contains(args.get(0))) {
                    throw new IllegalArgumentException(
                            "Invalid view type");
                }
                break;

            case "width":
                requireCount(name, args, 1);
                requireNumber(args.get(0), "Width", false);
                break;

            case "breaker":
                requireCount(name, args, 1);

                if (!Set.of("greedy", "balanced")
                        .contains(args.get(0))) {
                    throw new IllegalArgumentException(
                            "Invalid line breaker");
                }
                break;

            case "insert":
                requireCount(name, args, 3);
                requireNumber(args.get(0), "Paragraph index", true);
                requireNumber(args.get(1), "Position", true);
                break;

            case "delete":
                requireCount(name, args, 3);
                requireNumber(args.get(0), "Paragraph index", true);
                requireNumber(args.get(1), "Start position", true);
                requireNumber(args.get(2), "End position", true);

                if (Integer.parseInt(args.get(2))
                        < Integer.parseInt(args.get(1))) {
                    throw new IllegalArgumentException(
                            "End position cannot be before start");
                }
                break;

            case "split":
                requireCount(name, args, 2);
                requireNumber(args.get(0), "Paragraph index", true);
                requireNumber(args.get(1), "Position", true);
                break;

            case "merge":
                requireCount(name, args, 1);
                requireNumber(args.get(0), "Paragraph index", true);
                break;

            case "move":
                requireMinimumCount(name, args, 3);
                break;

            case "section":
            case "paragraph":
            case "format":
                requireMinimumCount(name, args, 1);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported command: " + name);
        }
    }

    // Checks macro commands
    private void validateMacro(List<String> args) {
        requireMinimumCount("macro", args, 1);

        String action = args.get(0);

        if (!Set.of("start", "stop", "replay").contains(action)) {
            throw new IllegalArgumentException(
                    "Invalid macro action");
        }

        if (action.equals("stop")) {
            requireCount("macro stop", args, 1);
        }
        else {
            requireCount("macro " + action, args, 2);
            requireNonBlank(args.get(1), "Macro name");
        }
    }

    // Checks search mode
    private void validateSearchMode(String mode) {
        if (!Set.of("whole", "substring").contains(mode)) {
            throw new IllegalArgumentException(
                    "Search mode must be whole or substring");
        }
    }

    // Requires an exact number of arguments
    private void requireCount(String name,
                              List<String> args,
                              int count) {
        if (args.size() != count) {
            throw new IllegalArgumentException(
                    name + " requires " + count + " arguments");
        }
    }

    // Requires at least a certain number of arguments
    private void requireMinimumCount(String name,
                                     List<String> args,
                                     int count) {
        if (args.size() < count) {
            throw new IllegalArgumentException(
                    name + " requires at least "
                            + count + " arguments");
        }
    }

    // Checks for empty names or text
    private void requireNonBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    label + " cannot be empty");
        }
    }

    // Checks numeric arguments
    private void requireNumber(String value,
                               String label,
                               boolean allowZero) {
        try {
            int number = Integer.parseInt(value);

            if (number < 0 || (!allowZero && number == 0)) {
                throw new IllegalArgumentException(
                        label + " must be "
                                + (allowZero ? "non-negative" : "positive"));
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    label + " must be a number");
        }
    }

}
