package fakedds;

import java.io.*;


public final class FloatSeq extends AbstractPrimitiveSequence {
    private float[] _array;

    public FloatSeq() {
        this(0);
    }

    public FloatSeq(int var1) {
        super(Float.class, Float.TYPE, var1);
    }

    public FloatSeq(float[] var1) {
        this(var1.length);
        this.addAllFloat(var1);
    }

    public boolean addAllFloat(float[] var1, int var2, int var3) {
        return this.addAllPrimitive(var1, var2, var3);
    }

    public boolean addAllFloat(float[] var1) {
        int var2 = var1.length;
        return this.addAllFloat(var1, 0, var2);
    }

    public void addFloat(float var1) {
        this.ensureSpaceForAdditionalElements(1);
        this._array[super._size++] = var1;
    }

    public void addFloat(int var1, float var2) {
        this.shiftAndEnlarge(var1);
        this._array[var1] = var2;
    }

    public float getFloat(int var1) {
        this.checkIndexUpperBound(var1, false);
        return this._array[var1];
    }

    public float setFloat(int var1, float var2) {
        this.checkIndexUpperBound(var1, false);
        float var3 = this._array[var1];
        this._array[var1] = var2;
        return var3;
    }

    public void setFloat(int var1, float[] var2, int var3, int var4) {
        this.setPrimitive(var1, var2, var3, var4);
    }

    public float[] toArrayFloat(float[] var1) {
        int var2 = var1 == null ? 0 : var1.length;
        return (float[])((float[])this.toArrayPrimitive(var1, var2));
    }

    public static void readFloatArray(float[] var0, DataInput var1, int var2) throws IOException {
        for(int var3 = 0; var3 < var2; ++var3) {
            var0[var3] = var1.readFloat();
        }

    }

    public static void writeFloatArray(float[] var0, DataOutput var1, int var2) throws IOException {
        for(int var3 = 0; var3 < var2; ++var3) {
            var1.writeFloat(var0[var3]);
        }

    }

    public int getMaximum() {
        return this._array == null ? 0 : this._array.length;
    }

    public Object get(int var1) {
        float var2 = this.getFloat(var1);
        return new Float(var2);
    }

    public float[] getPrimitiveArray() {
        return this._array;
    }

    public void setPrimitiveArray(Object var1, int var2) {
        this._array = (float[])((float[])var1);
        this._size = var2;
    }

    public Object set(int var1, Object var2) {
        float var3 = this.setFloat(var1, ((Float)var2).floatValue());
        return new Float(var3);
    }

    public void add(int var1, Object var2) {
        this.addFloat(var1, ((Float)var2).floatValue());
    }

    public int hashCode() {
        int var1 = 0;

        for(int var2 = 0; var2 < super._size; ++var2) {
            var1 += (int)this._array[var2];
        }

        return var1;
    }

    protected Object createArray(int var1) {
        return new float[var1];
    }

    protected void setPrimitiveArray(Object var1) {
        this._array = (float[])((float[])var1);
    }

    protected void readElements(ObjectInput var1, int var2) throws IOException {
        readFloatArray(this._array, var1, var2);
    }

    protected void writeElements(ObjectOutput var1, int var2) throws IOException {
        if (this._array != null) {
            writeFloatArray(this._array, var1, var2);
        }

    }
}
