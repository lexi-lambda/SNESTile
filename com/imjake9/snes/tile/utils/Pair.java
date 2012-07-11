package com.imjake9.snes.tile.utils;


public class Pair<L, R> {
    
    private final L left;
    private final R right;
    
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    public L getLeft() {
        return left;
    }
    
    public R getRight() {
        return right;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Pair)) return false;
        Pair otherPair = (Pair) o;
        return left.equals(otherPair.left) && right.equals(otherPair.right);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 67 * hash + (this.right != null ? this.right.hashCode() : 0);
        return hash;
    }
    
}
