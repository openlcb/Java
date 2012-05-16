package org.openlcb;

// For annotations
import net.jcip.annotations.*; 
import edu.umd.cs.findbugs.annotations.*; 

/**
 * Common service methods (a library, not a class)
 * <p>
 * NodeID objects are immutable once created.
 *
 * @author  Bob Jacobsen   Copyright 2009, 2010, 2011 2012
 * @version $Revision$
 */
@Immutable
@ThreadSafe
public class Utilities {

    @CheckReturnValue
    @NonNull
    static public String toHexPair(int i) {
        String retval = "00"+Integer.toHexString(i).toUpperCase();
        return retval.substring(retval.length()-2);
    }

    @CheckReturnValue
    @NonNull
    static public String toHexSpaceString(int[] array) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buff.append(" ");
            buff.append(Utilities.toHexPair(array[i]));
        }
        String retval = new String(buff);
        return retval.substring(1);
    }
}
