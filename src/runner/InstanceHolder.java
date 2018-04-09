package runner;


import fakedds.InstanceHandle_t;

public class InstanceHolder<T> {
    public T data;
    public InstanceHandle_t handle;

    public InstanceHolder() {

    }

    public InstanceHolder(T t, InstanceHandle_t handle) {
        this.data = t;
        this.handle = handle;
    }

    @Override
    public String toString() {
        return "[data=" + data + ",handler=" + handle + "]";
    }
}