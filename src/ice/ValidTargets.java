package ice;

import fakedds.Copyable;
import fakedds.WstringSeq;

import java.io.Serializable;


public class ValidTargets implements Copyable, Serializable
{

    public WstringSeq userData = new WstringSeq((128));


    public ValidTargets() {

    }


    public ValidTargets(ValidTargets other) {

        this();
        copy_from(other);
    }



    public static Object create() {
        ValidTargets self;
        self = new ValidTargets();

        self.clear();

        return self;
    }

    public void clear() {

        if (userData != null) {
            userData.clear();
        }

    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }



        if(getClass() != o.getClass()) {
            return false;
        }

        ValidTargets otherObj = (ValidTargets)o;



        if(!userData.equals(otherObj.userData)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;

        __result += userData.hashCode();

        return __result;
    }


    public Object copy_from(Object src) {
        ValidTargets typedSrc = (ValidTargets) src;
        ValidTargets typedDst = this;

        typedDst.userData.copy_from(typedSrc.userData);

        return this;
    }



    public String toString(){
        return toString("", 0);
    }


    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();


        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }


        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("userData: ");
        for(int i__ = 0; i__ < userData.size(); ++i__) {
            if (i__!=0) strBuffer.append(", ");
            strBuffer.append(userData.get(i__));
        }
        strBuffer.append("\n");

        return strBuffer.toString();
    }

}

