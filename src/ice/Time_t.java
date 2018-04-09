package ice;

import fakedds.Copyable;

import java.io.Serializable;

public class Time_t implements Copyable, Serializable
{

    public int sec = 0;
    public int nanosec = 0;


    public Time_t() {

    }


    public Time_t(Time_t other) {

        this();
        copy_from(other);
    }



    public static Object create() {
        Time_t self;
        self = new Time_t();

        self.clear();

        return self;
    }

    public void clear() {

        sec = 0;

        nanosec = 0;

    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }



        if(getClass() != o.getClass()) {
            return false;
        }

        Time_t otherObj = (Time_t)o;



        if(sec != otherObj.sec) {
            return false;
        }

        if(nanosec != otherObj.nanosec) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;

        __result += (int)sec;

        __result += (int)nanosec;

        return __result;
    }


    /**
     * This is the implementation of the <code>Copyable</code> interface.
     * This method will perform a deep copy of <code>src</code>
     * This method could be placed into <code>Time_tTypeSupport</code>
     * rather than here by using the <code>-noCopyable</code> option
     * to rtiddsgen.
     *
     * @param src The Object which contains the data to be copied.
     * @return Returns <code>this</code>.
     * @exception NullPointerException If <code>src</code> is null.
     * @exception ClassCastException If <code>src</code> is not the
     * same type as <code>this</code>.
     * @see Copyable#copy_from(java.lang.Object)
     */
    public Object copy_from(Object src) {


        Time_t typedSrc = (Time_t) src;
        Time_t typedDst = this;

        typedDst.sec = typedSrc.sec;

        typedDst.nanosec = typedSrc.nanosec;

        return this;
    }



    public String toString(){
        return toString("", 0);
    }

    public long timestampMilli() {
        return (long)sec * 1000L + (long)nanosec / 1000000L;
    }

    public long timestampNano() {
        return (long)sec * 1000000000L + (long)nanosec;
    }


    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();


        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }


        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("sec: ").append(sec).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("nanosec: ").append(nanosec).append("\n");

        return strBuffer.toString();
    }

}

