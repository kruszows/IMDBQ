package org.kruszows.imdbq.bktree;

import org.apache.commons.lang3.StringUtils;
import org.kruszows.imdbq.util.Cache;
import org.kruszows.imdbq.util.Calculate;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class BKNode implements Comparable<BKNode> {

    private String term;
    private TreeSet<String> associatedTerms = new TreeSet<>();
    private Map<Integer, BKNode> children = new HashMap<>();

    public BKNode() {

    }
    public BKNode(String term, String associatedTerm) {
        this.term = term;
        this.associatedTerms.add(associatedTerm);
    }

    public String getTerm() {
        return term;
    }

    public TreeSet<String> getAssociatedTerms() {
        return this.associatedTerms;
    }

    public void set(String term, TreeSet<String> associatedTerms) {
        this.term = term;
        this.associatedTerms = associatedTerms;
    }

    protected BKNode nthChild(int n) {
        return children.get(n);
    }

    protected BKNode add(BKNode child) {
        if (child.getTerm().equals(getTerm())) {
            this.associatedTerms.addAll(child.getAssociatedTerms());
            return this;
        }
        int offset = Calculate.levenshteinDistance(getTerm(), child.getTerm());
        BKNode childAtOffset = nthChild(offset);
        if (childAtOffset == null) {
            children.put(offset, child);
            return child;
        }
        else {
            return childAtOffset.add(child);
        }
    }

    public BKNode closestMatchSearch(String query, int maxOffset, BKNode currentClosestNode) {
        int currentOffset = Cache.getAndMaybeSetOffset(query, getTerm());

        if (children.isEmpty() || currentOffset == 0) {
            return this;
        }

        if (currentOffset <= maxOffset && (currentClosestNode.getTerm() == null || currentOffset < Cache.getAndMaybeSetOffset(query, currentClosestNode.getTerm()))) {
            currentClosestNode.set(getTerm(), getAssociatedTerms());
        }

        int startIndex = Math.max(1, currentOffset - maxOffset);
        int endIndex = currentOffset + maxOffset;
        for (int i = startIndex; i < endIndex; i++) {
            BKNode child = nthChild(i);
            if (child == null) {
                continue;
            }
            child.closestMatchSearch(query, maxOffset, currentClosestNode);
        }

        return currentClosestNode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        buildString(stringBuilder, 0);
        return stringBuilder.toString();
    }

    public void buildString(StringBuilder stringBuilder, int level) {
        stringBuilder.append(String.format("%s\n", this.getTerm()));
        stringBuilder.append(StringUtils.repeat("\t", level));
        stringBuilder.append(String.format("└──%s\n", this.getAssociatedTerms()));
        for (int i : children.keySet()) {
            stringBuilder.append(StringUtils.repeat("\t", level+1));
            stringBuilder.append(String.format("%d: ", i));
            nthChild(i).buildString(stringBuilder, level + 1);
        }
    }

    @Override
    public int compareTo(BKNode that) {
        return this.getTerm().compareTo(that.getTerm());
    }

}
