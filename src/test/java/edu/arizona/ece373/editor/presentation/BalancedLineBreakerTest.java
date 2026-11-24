package edu.arizona.ece373.editor.presentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BalancedLineBreakerTest {

    private final LineBreaker breaker = new BalancedLineBreaker();

    @Test
    void evensOutLinesAtWidthNine() {
        assertEquals(List.of("aaa bb", "cc ddddd"),
                breaker.breakLines("aaa bb cc ddddd", 9));
    }

    @Test
    void evensOutLinesAtWidthSix() {
        assertEquals(List.of("aaa", "bb cc", "ddddd"),
                breaker.breakLines("aaa bb cc ddddd", 6));
    }

    @Test
    void producesDifferentOutputThanGreedy() {
        String text = "aaa bb cc ddddd";
        List<String> greedy = new GreedyLineBreaker().breakLines(text, 9);
        List<String> balanced = breaker.breakLines(text, 9);
        assertNotEquals(greedy, balanced);
    }

    @Test
    void textThatFitsStaysOnOneLine() {
        assertEquals(List.of("Hello world"), breaker.breakLines("Hello world", 80));
    }

    @Test
    void noLineExceedsWidthAndNoWordsAreLost() {
        String text = "The quick brown fox jumps over the lazy dog";
        int width = 12;
        List<String> lines = breaker.breakLines(text, width);

        for (String line : lines) {
            assertTrue(line.length() <= width, "line too long: \"" + line + "\"");
        }
        assertEquals(text, String.join(" ", lines));
    }

    @Test
    void longWordGetsItsOwnLine() {
        assertEquals(List.of("a", "extraordinary", "b"),
                breaker.breakLines("a extraordinary b", 5));
    }

    @Test
    void widthOfOnePutsEachWordOnItsOwnLine() {
        assertEquals(List.of("a", "b", "c"), breaker.breakLines("a b c", 1));
    }

    @Test
    void extraSpacesAreCollapsed() {
        assertEquals(List.of("a b"), breaker.breakLines("a    b", 10));
        assertEquals(List.of("hi"), breaker.breakLines("   hi   ", 10));
    }

    @Test
    void emptyOrBlankTextGivesOneEmptyLine() {
        assertEquals(List.of(""), breaker.breakLines("", 10));
        assertEquals(List.of(""), breaker.breakLines("    ", 10));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    void widthBelowOneThrows(int width) {
        assertThrows(IllegalArgumentException.class, () -> breaker.breakLines("text", width));
    }
}
