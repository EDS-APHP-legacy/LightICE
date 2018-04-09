package runner;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class CollectionContainer<T> implements NullSaveContainer<T>
{
    CollectionContainer(Collection<T> dt) {
        this.dt = dt;
    }

    private final Collection<T> dt;

    public boolean isNull() { return dt == null; }
    public Iterator<T> iterator() { return dt==null? Collections.emptyIterator() : dt.iterator(); }
    public int size() { return dt == null ? 0 : dt.size(); }
}