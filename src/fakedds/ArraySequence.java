package fakedds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class ArraySequence extends ArrayList implements Sequence {
    private static final long serialVersionUID = -1941836975119605901L;
    private final Class _elementType;
    private int _maximum;

    public ArraySequence(Class var1) {
        this(var1, 0);
    }

    public ArraySequence(Class var1, int var2) {
        super(var2);
        this._maximum = 0;
        AbstractSequence.checkNullElementType(var1);
        this._elementType = var1;
        this.ensureCapacity(var2);
    }

    public ArraySequence(Class var1, Collection var2) {
        this(var1, var2.size());
        this.addAll(var2);
    }

    public final int getMaximum() {
        return this._maximum;
    }

    public final void setMaximum(int var1) {
        AbstractSequence.checkNegativeMaximum(var1);

        for(int var2 = this.size() - 1; var2 >= var1; --var2) {
            this.remove(var2);
        }

        if (this.size() == var1) {
            this.trimToSize();
        } else if (var1 > this.getMaximum()) {
            this.ensureCapacity(var1);
        } else {
            this._maximum = var1;
        }

    }

    public final Class getElementType() {
        return this._elementType;
    }

    public boolean addAll(Collection var1) {
        boolean var2 = false;
        if (var1 instanceof Sequence) {
            Sequences.checkType(this._elementType, ((Sequence)var1).getElementType());
            var2 = super.addAll(var1);
        } else {
            this.ensureCapacity(this.size() + var1.size());

            for(Iterator var3 = var1.iterator(); var3.hasNext(); var2 = this.add(var3.next()) || var2) {
                ;
            }
        }

        return var2;
    }

    public boolean addAll(int var1, Collection var2) {
        boolean var3 = true;
        if (var2 instanceof Sequence) {
            Sequences.checkType(this._elementType, ((Sequence)var2).getElementType());
            var3 = super.addAll(var1, var2);
        } else {
            this.ensureCapacity(this.size() + var2.size());
            Iterator var4 = var2.iterator();

            while(var4.hasNext()) {
                this.add(var1++, var4.next());
            }
        }

        return var3;
    }

    public boolean add(Object var1) {
        Sequences.checkElementType(this._elementType, var1);
        this.ensureCapacity(this.size() + 1);
        return super.add(var1);
    }

    public void add(int var1, Object var2) {
        Sequences.checkElementType(this._elementType, var2);
        if (var1 >= 0 && var1 <= this.size()) {
            this.ensureCapacity(this.size() + 1);
        }

        super.add(var1, var2);
    }

    public Object set(int var1, Object var2) {
        Sequences.checkElementType(this._elementType, var2);
        return super.set(var1, var2);
    }

    public final void ensureCapacity(int var1) {
        if (var1 > this._maximum) {
            super.ensureCapacity(var1);
            this._maximum = var1;
        }

    }

    public final void trimToSize() {
        super.trimToSize();
        this._maximum = this.size();
    }

    public boolean equals(Object var1) {
        if (this.getClass() != var1.getClass()) {
            return false;
        } else {
            Sequence var2 = (Sequence)var1;
            if (this.size() != var2.size()) {
                return false;
            } else {
                for(int var3 = 0; var3 < this.size(); ++var3) {
                    if (!Utilities.equalsNullSafe(this.get(var3), var2.get(var3))) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
