package org.kruszows.imdbq.util;

import java.util.Objects;

public class Couple<T> {
    private T value1;
    private T value2;

    public Couple(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T getValue1() {
        return value1;
    }

    public T getValue2() {
        return value2;
    }

    public void setValue1(T value1) {
        this.value1 = value1;
    }

    public void setValue2(T value2) {
        this.value2 = value2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Couple)) {
            return false;
        }
        Couple<?> oAsCouple = (Couple<?>) o;
        return getValue1().equals(oAsCouple.getValue1()) && getValue2().equals(oAsCouple.getValue2())
                || getValue1().equals(oAsCouple.getValue2()) && getValue2().equals(oAsCouple.getValue1());
    }

    @Override
    public int hashCode() {
        int value1Hash = value1.hashCode();
        int value2Hash = value2.hashCode();
        int maxHash = Math.max(value1Hash, value2Hash);
        int minHash = Math.min(value1Hash, value2Hash);
        return Objects.hash(minHash, maxHash);
    }
}
