/*
 * Creator: Santi Ontanon Villar
 */
package dlg.util;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The Class Symbol.
 */
public class Label implements Serializable {

    static HashMap<String, StringBuffer> sSymbolHash = new HashMap<String, StringBuffer>();

    StringBuffer mSym;

    public Label(String sym) throws Exception {
        if (sym == null) {
            throw new Exception("null name in a Symbol!!!");
        }
        if (sSymbolHash.containsKey(sym)) {
            mSym = sSymbolHash.get(sym);
        } else {
            mSym = new StringBuffer(sym);
            sSymbolHash.put(sym, mSym);
        }
    }


    public Label(Label sym) {
        mSym = sym.mSym;
    }

    
    public String get() {
        return mSym.toString();
    }


    public void set(String str) {
        mSym = new StringBuffer(str);
    }


    public boolean equals(Object o) {
        if (o instanceof String) {
            return equals((String) o);
        } else if (o instanceof StringBuffer) {
            return equals((StringBuffer) o);
        } else if (o instanceof Label) {
            return equals((Label) o);
        }
        return false;
    }


    public boolean equals(String str) {
        if (mSym == null) {
            if (str == null) {
                return true;
            }
            return false;
        } else {
            if (str == null) {
                return false;
            }
            return (mSym.toString().equals(str));
        }
    }


    public boolean equals(StringBuffer str) {
        if (mSym == null) {
            if (str == null) {
                return true;
            }
            return false;
        } else {
            if (str == null) {
                return false;
            }
			// System.out.println("Symbol.equals: '" + m_sym + "' == '" + str + "'? -> " +
            // m_sym.toString().equals(str.toString()));
            return (mSym.toString().equals(str.toString()));
        }
    }


    public boolean equals(Label sym) {
        if (sym==null) return false;
        return mSym == sym.mSym;
    }


    static void arrangeString(StringBuffer str) {
        int len;

        while (str.charAt(0) == ' ' || str.charAt(0) == '\n' || str.charAt(0) == '\r' || str.charAt(0) == '\t') {
            str = str.deleteCharAt(0);
        }

        len = str.length();
        while (len > 1 && (str.charAt(len - 1) == ' ' || str.charAt(len - 1) == '\n' || str.charAt(len - 1) == '\r' || str.charAt(len - 1) == '\t')) {
            str = str.deleteCharAt(len - 1);
            len--;
        } /* while */

    }


    public String toString() {
        return mSym.toString();
    }


    public int hashCode() {
        if (mSym == null) {
            return 0;
        }
        return mSym.hashCode();
    }

}
