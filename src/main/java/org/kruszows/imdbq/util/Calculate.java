package org.kruszows.imdbq.util;

import java.util.Arrays;

public class Calculate {

    public static int levenshteinDistance(String source, String destination) {
        if (source.isEmpty() && destination.isEmpty()) {
            return 0;
        }
        int[][] distancesByCharacterIndex = new int[source.length()+1][destination.length()+1];
        for (int i = 0; i <= source.length(); i++) {
            for (int j = 0; j <= destination.length(); j++) {
                if (i == 0) {
                    distancesByCharacterIndex[i][j] = j;
                }
                else if (j == 0) {
                    distancesByCharacterIndex[i][j] = i;
                }
                else {
                    int replacementDistance = distancesByCharacterIndex[i - 1][j - 1] + ((source.charAt(i - 1) == destination.charAt(j - 1)) ? 0 : 1);
                    int deletionDistance = distancesByCharacterIndex[i - 1][j] + 1;
                    int insertionDistance = distancesByCharacterIndex[i][j - 1] + 1;
                    distancesByCharacterIndex[i][j] = min(replacementDistance, deletionDistance, insertionDistance);
                }
            }
        }
        return distancesByCharacterIndex[source.length()][destination.length()];
    }

    public static int min(int ... values) {
        return Arrays.stream(values).min().orElse(Integer.MAX_VALUE);
    }

}
