package com.qcadoo.mes.utils;

/**
 * Represents pair key->value
 * 
 * @param <A>
 *            class of key
 * @param <B>
 *            class of value
 */
public final class Pair<A, B> {

    private final A key;

    private final B value;

    public Pair(final A key, final B value) {
        this.key = key;
        this.value = value;
    }

    public A getKey() {
        return key;
    }

    public B getValue() {
        return value;
    }

    /**
     * creates new Pair instance
     * 
     * @param key
     * @param value
     * @return new Pair containing key and value same as arguments
     */
    public static <A, B> Pair<A, B> of(final A key, final B value) {
        return new Pair<A, B>(key, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair other = (Pair) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Pair[%s,%s]", key, value);
    }

}
