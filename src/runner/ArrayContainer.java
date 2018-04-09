package runner;

import java.util.Iterator;

public class ArrayContainer<T> implements NullSaveContainer<T>
{
    ArrayContainer(T[] dt) {
        this(dt, dt == null ? 0 : dt.length);
    }

    ArrayContainer(T[] dt, int l) {
        this.dt = dt;
        this.l = l;
    }

    private final T[] dt;
    private final int l;

    public boolean isNull() { return dt == null; }
    public Iterator<T> iterator() { return new ArrayIterator<T>(dt, l); }
    public int size() { return l; }
}
