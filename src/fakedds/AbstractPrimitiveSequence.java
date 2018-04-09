package fakedds;

import java.io.*;

public abstract class AbstractPrimitiveSequence extends AbstractSequence implements Sequence, Externalizable, Copyable {
    private static final long serialVersionUID = -6374459295811621293L;
    protected int _size = 0;
    private boolean _loanedJavaElems = false;
    private final Class _primitiveType;

    public final Class getElementType() {
        return this._primitiveType;
    }

    public abstract void add(int var1, Object var2);

    public abstract Object get(int var1);

    public final void readExternal(ObjectInput var1) throws IOException {
        int var2 = var1.readInt();
        if (var2 < 0) {
            throw new StreamCorruptedException("unexpected sequence size: " + var2);
        } else {
            if (var2 > 0) {
                this.ensureMaximum(var2);
                this.readElements(var1, var2);
            }

            this.setSize(var2);
        }
    }

    public final void writeExternal(ObjectOutput var1) throws IOException {
        int var2 = this.size();
        var1.writeInt(var2);
        this.writeElements(var1, var2);
    }

    public void loan(Object var1, int var2) {
        if (this.getMaximum() != 0) {
            throw new UnsupportedOperationException("max size must be 0");
        } else {
            this.setPrimitiveArray(var1, var2);
            this._loanedJavaElems = true;
        }
    }

    public void unloan() {
        if (!this._loanedJavaElems) {
            throw new UnsupportedOperationException("buffer must be loaned");
        } else {
            this.setPrimitiveArray((Object)null, 0);
            this._loanedJavaElems = false;
        }
    }

    public final boolean hasOwnership() {
        return !this._loanedJavaElems;
    }

    public abstract Object getPrimitiveArray();

    public abstract void setPrimitiveArray(Object var1, int var2);

    public abstract Object set(int var1, Object var2);

    public final void clear() {
        this._size = 0;
    }

    public final void truncate(int var1) {
        if (var1 > this._size) {
            throw new IndexOutOfBoundsException(var1 + " > " + this._size);
        } else {
            this.setSize(var1);
        }
    }

    public final void setSize(int var1) {
        int var2 = this.getMaximum();
        if (var1 >= 0 && var1 <= var2) {
            this._size = var1;
        } else {
            throw new IndexOutOfBoundsException("New size " + var1 + " isn't in the range [0, " + var2 + ")");
        }
    }

    public final int size() {
        return this._size;
    }

    public final Object copy_from(Object var1) {
        AbstractPrimitiveSequence var2 = (AbstractPrimitiveSequence)var1;
        this.clear();
        this.ensureMaximum(var2.size());
        if (var2.size() > 0) {
            System.arraycopy(var2.getPrimitiveArray(), 0, this.getPrimitiveArray(), 0, var2.size());
        }

        this._size = var2.size();
        return this;
    }

    protected AbstractPrimitiveSequence(Class var1, Class var2, int var3) {
        super(var1);
        checkNullElementType(var2);
        this._primitiveType = var2;
        this.setMaximum(var3);
    }

    protected abstract void readElements(ObjectInput var1, int var2) throws IOException;

    protected abstract void writeElements(ObjectOutput var1, int var2) throws IOException;

    protected final boolean addAllPrimitive(Object var1, int var2, int var3) {
        boolean var4 = false;
        if (var3 > 0) {
            this.ensureSpaceForAdditionalElements(var3);
            Object var5 = this.getPrimitiveArray();
            System.arraycopy(var1, var2, var5, this._size, var3);
            this._size += var3;
            var4 = true;
        }

        return var4;
    }

    protected final void setPrimitive(int var1, Object var2, int var3, int var4) {
        if (var1 + var4 > this._size) {
            throw new IndexOutOfBoundsException(var1 + var4 + " > " + this._size);
        } else {
            Object var5 = this.getPrimitiveArray();
            System.arraycopy(var2, var3, var5, var1, var4);
        }
    }

    protected final Object toArrayPrimitive(Object var1, int var2) {
        Object var3 = var2 >= this._size && var1 != null ? var1 : this.createArray(this._size);
        Object var4 = this.getPrimitiveArray();
        if (this._size != 0) {
            System.arraycopy(var4, 0, var3, 0, this._size);
        }

        return var3;
    }

    protected abstract void setPrimitiveArray(Object var1);

    protected abstract Object createArray(int var1);

    protected final void shiftRightByOne(int var1) {
        Object var2 = this.getPrimitiveArray();
        System.arraycopy(var2, var1, var2, var1 + 1, this._size - var1 - 1);
    }

    protected void shiftLeftByOne(int var1) {
        Object var2 = this.getPrimitiveArray();
        System.arraycopy(var2, var1 + 1, var2, var1, this._size - var1 - 1);
    }

    protected final void reallocate(int var1) {
        if (this._loanedJavaElems) {
            throw new UnsupportedOperationException("buffer must not be loaned");
        } else {
            Object var2 = this.createArray(var1);
            this._size = Math.min(this._size, var1);
            Object var3 = this.getPrimitiveArray();
            if (var3 != null) {
                System.arraycopy(var3, 0, var2, 0, this._size);
            }

            this.setPrimitiveArray(var2);
        }
    }

    final void incrementSize() {
        ++this._size;
    }

    final void decrementSize() {
        --this._size;
    }
}
