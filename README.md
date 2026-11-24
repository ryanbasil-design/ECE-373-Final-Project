# ECE 373 Final Project: Document Editor

Team 9: Ryan Basil, Moamin Sami, Hunter Saltzberry, Noor Shaheen

A console document editor written in Java. You build a document out of sections, nested subsections and paragraphs, then edit, format, search, undo and redo, save checkpoints, record macros, and view it as plain or marked-up text. Commands can be typed in the console or read from a script file.

## Requirements

- Java 17 or newer
- Maven (the one built into IntelliJ works)

## Build and test

```
mvn clean test
```

This compiles everything and runs the full JUnit 5 suite. No other setup is needed.

## Run

In IntelliJ, run `edu.arizona.ece373.editor.command.EditorApp`.

From the command line, after `mvn clean test`:

```
java -cp target/classes edu.arizona.ece373.editor.command.EditorApp
java -cp target/classes edu.arizona.ece373.editor.command.EditorApp script.txt
```

With no arguments it reads commands from the console. With a file name it runs every command in the file.

## Commands

Paragraphs are numbered from 0 in document order, not counting section titles. Sections are numbered from 0 in document order. Text with spaces goes in quotes.

| Command | What it does |
|---|---|
| `section "Title"` | add a section at the end of the document |
| `section "Title" inside 0` | add a subsection inside section 0 |
| `paragraph "text"` / `paragraph "text" inside 0` | add a paragraph to the document or a section |
| `insert 0 5 "text"` | insert text into paragraph 0 at position 5 |
| `delete 0 2 7` | delete characters 2 to 7 from paragraph 0 |
| `move 0 1 4 1 0` | move characters 1 to 4 of paragraph 0 to paragraph 1, position 0 |
| `split 0 5` | split paragraph 0 into two at position 5 |
| `merge 0` | merge paragraph 0 with the paragraph after it |
| `format 0 0 5 bold` | styles: `bold`, `italic`, `unbold`, `unitalic` |
| `format 0 0 5 font Arial` | also `size 14` and `color red` |
| `format title 0 0 5 bold` | format the title of section 0 |
| `find "text" whole` | count matches; use `whole` or `substring` |
| `replace "old" "new" whole` | replace the first match |
| `replaceall "old" "new" substring` | replace every match (one undo) |
| `undo` / `redo` | undo or redo the last change |
| `checkpoint save name` / `checkpoint restore name` | save or restore a named checkpoint |
| `macro start name` / `macro stop` / `macro replay name` | record and replay commands (one undo per replay) |
| `view` / `view plain` / `view markup` | show the document |
| `width 40` | set the line width |
| `breaker greedy` / `breaker balanced` | choose how lines are broken |
| `stats` | word, character and paragraph counts |
| `help` / `exit` | list commands / quit |

## Example script

```
section "Introduction"
paragraph "This program edits documents." inside 0
format title 0 0 12 bold
replaceall "program" "system" whole
width 30
view markup
undo
stats
```

## Design

The code is split into three packages, matching the Software Design Document:

- `engine`: the document model (Document, Section, Paragraph, TextRun, CharacterFormat) and the editing commands with their undo history (EditCommand, CommandHistory, CompositeCommand)
- `command`: reading and running commands (EditorApp, CommandParser, HistoryEditingBridge), search and replace, macros and checkpoints
- `presentation`: rendering, line breaking and statistics (DocumentView, Renderer, LineBreaker, DocumentStatistics)

Design patterns used: Command (EditCommand and its subclasses with CommandHistory), Composite (Section and Paragraph as DocumentElements, and CompositeCommand), Strategy (Renderer and LineBreaker), and Observer (DocumentStatistics watching the Document).
