package runner;

import java.util.Iterator;
import java.util.function.Consumer;

public class ArrayIterator<T> implements Iterator<T>
{
    final T[] data;
    final int l;

    int currentIdx=0;

    public ArrayIterator(T[] data, int l) {
        this.data = data;
        this.l = l;
    }

    @Override
    public boolean hasNext() {
        return currentIdx<l;
    }

    @Override
    public T next() {
        return data[currentIdx++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }
}