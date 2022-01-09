package org.kruszows.imdbq.bktree;

import java.util.TreeMap;

public class BKTree {

    private BKNode root;
    private final TreeMap<String, BKNode> terms = new TreeMap<>();

    public BKNode closestMatchSearch(String query, int maxOffset) {
        if (terms.containsKey(query)) {
            return terms.get(query);
        }
        return root.closestMatchSearch(query, maxOffset, new BKNode());
    }

    public synchronized void add(String term, String associatedTerm) {
        if (term != null && !term.isEmpty()) {
            BKNode newNode = new BKNode(term, associatedTerm);
            if (root == null) {
                root = newNode;
            }
            else {
                newNode = root.add(newNode);
            }
            terms.put(term, newNode);
        }
    }

    public int size() {
        return terms.size();
    }

    @Override
    public String toString() {
        return root.toString();
    }

}
