package runner;

import java.util.Iterator;

public interface NullSaveContainer<T>
{
    boolean isNull();
    Iterator<T> iterator();
    int size();
}