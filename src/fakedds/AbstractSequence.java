package fakedds;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.Serializable;
import java.util.AbstractList;

public abstract class AbstractSequence extends AbstractList implements Sequence, Serializable {
    private static final long serialVersionUID = 5442580504635520860L;
    private static final RuntimeException NEGATIVE_MAXIMUM = new IllegalArgumentException("max size < 0");
    private static final RuntimeException NULL_ELEMENT_TYPE = new NullPointerException("null element type");
    private static final RuntimeException OUT_OF_BOUNDS = new IndexOutOfBoundsException();
    private final Class _elementType;

    public void setMaximum(int var1) {
        checkNegativeMaximum(var1);
        if (var1 != this.getMaximum()) {
            this.reallocate(var1);
        }

    }

    public Class getElementType() {
        return this._elementType;
    }

    public void add(int var1, Object var2) {
        Sequences.checkElementType(this._elementType, var2);
        this.shiftAndEnlarge(var1);
        this.set(var1, var2);
    }

    public boolean add(Object var1) {
        return super.add(var1);
    }

    public final Object remove(int var1) {
        Object var2 = this.get(var1);
        this.shiftLeftByOne(var1);
        this.decrementSize();
        return var2;
    }

    protected AbstractSequence(Class var1) {
        checkNullElementType(var1);
        this._elementType = var1;
    }

    protected final void shiftAndEnlarge(int var1) {
        this.checkBounds(var1, true);
        this.ensureSpaceForAdditionalElements(1);
        this.incrementSize();
        this.shiftRightByOne(var1);
    }

    protected void shiftRightByOne(int var1) {
        for(int var2 = this.size() - 2; var2 >= var1; --var2) {
            Object var3 = this.get(var2);
            this.set(var2 + 1, var3);
        }

    }

    protected void shiftLeftByOne(int var1) {
        int var2 = this.size() - 1;

        for(int var3 = var1; var3 < var2; ++var3) {
            Object var4 = this.get(var3 + 1);
            this.set(var3, var4);
        }

    }

    protected final void checkBounds(int var1, boolean var2) {
        if (var1 < 0) {
            Utilities.rethrow(OUT_OF_BOUNDS);
        }

        this.checkIndexUpperBound(var1, var2);
    }

    protected final void checkIndexUpperBound(int var1, boolean var2) {
        if (var2) {
            --var1;
        }

        if (var1 >= this.size()) {
            Utilities.rethrow(OUT_OF_BOUNDS);
        }

    }

    protected final void ensureSpaceForAdditionalElements(int var1) {
        int var2 = this.size() + var1;
        this.ensureMaximum(var2);
    }

    protected final void ensureMaximum(int var1) {
        if (var1 > this.getMaximum()) {
            this.reallocate(var1);
        }

    }

    protected abstract void reallocate(int var1);

    static void checkNegativeMaximum(int var0) {
        if (var0 < 0) {
            Utilities.rethrow(NEGATIVE_MAXIMUM);
        }

    }

    protected static void checkNullElementType(Class var0) {
        if (var0 == null) {
            Utilities.rethrow(NULL_ELEMENT_TYPE);
        }

    }

    abstract void incrementSize();

    abstract void decrementSize();
}
