package edu.arizona.ece373.editor.presentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreedyLineBreakerTest {

    private final LineBreaker breaker = new GreedyLineBreaker();

    @Test
    void fillsEachLineWithAsManyWordsAsFit() {
        assertEquals(List.of("The quick", "brown fox", "jumps"),
                breaker.breakLines("The quick brown fox jumps", 10));
    }

    @Test
    void lineExactlyAtWidthFits() {
        assertEquals(List.of("abc def"), breaker.breakLines("abc def", 7));
        assertEquals(List.of("abc", "def"), breaker.breakLines("abc def", 6));
    }

    @Test
    void shortTextStaysOnOneLine() {
        assertEquals(List.of("Hello world"), breaker.breakLines("Hello world", 80));
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
