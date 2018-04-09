package fakedds;


import java.util.List;

public interface Sequence extends List {
    int getMaximum();

    void setMaximum(int var1);

    Class getElementType();
}
