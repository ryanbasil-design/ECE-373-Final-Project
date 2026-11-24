package edu.arizona.ece373.editor.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Line breaker that spreads words so all line lengths are as even as
 * possible.
 * It looks at the possible ways to break the paragraph within the width
 * and scores each one by taking the leftover space on every line, including
 * the last, squaring it, and adding the results. The layout with the lowest
 * score is chosen. Squaring makes large gaps cost much more than small ones,
 * so the lines come out even instead of one full line followed by a short one.
 * To stay fast on long text, it does not list every layout. It works
 * backwards from the end of the paragraph, remembers the best way to finish
 * from each word, and reuses those results.
 */
public class BalancedLineBreaker implements LineBreaker {

    @Override
    public List<String> breakLines(String text, int width) {
        if (width < 1) {
            throw new IllegalArgumentException("width must be at least 1: " + width);
        }
        List<String> lines = new ArrayList<>();
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = trimmed.split("\\s+");
        int n = words.length;

        long[] best = new long[n + 1];
        int[] next = new int[n + 1];
        best[n] = 0;

        for (int i = n - 1; i >= 0; i--) {
            best[i] = Long.MAX_VALUE;
            int lineLength = 0;

            for (int j = i + 1; j <= n; j++) {
                lineLength += words[j - 1].length();
                if (j - 1 > i) {
                    lineLength += 1;
                }

                if (lineLength > width && j - i > 1) {
                    break;
                }

                long leftover = Math.max(0, width - lineLength);
                long total = leftover * leftover + best[j];

                if (total < best[i]) {
                    best[i] = total;
                    next[i] = j;
                }
            }
        }

        int i = 0;
        while (i < n) {
            int j = next[i];
            lines.add(String.join(" ", Arrays.copyOfRange(words, i, j)));
            i = j;
        }
        return lines;
    }
}
