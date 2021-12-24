package org.kruszows.imdbq.util;

import org.kruszows.imdbq.bktree.BKNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cache {

    private static Cache instance = null;

    private final int QUERY_CAPACITY = 100;

    private Map<Couple<String>, Integer> offsets = new HashMap<>();
    private Map<String, BKNode> queries = new LinkedHashMap<>(QUERY_CAPACITY, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BKNode> eldest) {
            return size() > QUERY_CAPACITY;
        }
    };

    private Cache() {

    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public static int getAndMaybeSetOffset(String source, String destination) {
        Couple<String> endPoints = new Couple<>(source, destination);
        if (!getInstance().offsets.containsKey(endPoints)) {
            getInstance().offsets.put(endPoints, Calculate.levenshteinDistance(source, destination));
        }
        return getInstance().offsets.get(endPoints);
    }

    public static void addQuery(String query, BKNode closestMatchNode) {
        getInstance().queries.put(query, closestMatchNode);
    }

    public static BKNode getQuery(String query) {
        return getInstance().queries.get(query);
    }

    public static boolean hasQuery(String query) {
        return getInstance().queries.containsKey(query);
    }

}
