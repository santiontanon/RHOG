/*
 * Creator: Santi Ontanon Villar
 */
/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dlg.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;

/**
 * The Class Symbol.
 */
public class Label implements Serializable {

    static HashMap<String, StringBuffer> sSymbolHash = new HashMap<String, StringBuffer>();

    /**
     * The m sym.
     */
    StringBuffer mSym;

    /**
     * Instantiates a new symbol.
     *
     * @param sym the sym
     * @throws FeatureTermException the feature term exception
     */
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

    /**
     * Instantiates a new symbol.
     *
     * @param sym the sym
     */
    public Label(Label sym) {
        mSym = sym.mSym;
    }

    /**
     * Gets the.
     *
     * @return the string
     */
    public String get() {
        return mSym.toString();
    }

    /**
     * Sets the.
     *
     * @param str the str
     */
    public void set(String str) {
        mSym = new StringBuffer(str);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /**
     * Equals.
     *
     * @param str the str
     * @return true, if successful
     */
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

    /**
     * Equals.
     *
     * @param str the str
     * @return true, if successful
     */
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

    /**
     * Equals.
     *
     * @param sym the sym
     * @return true, if successful
     */
    public boolean equals(Label sym) {
        if (sym==null) return false;
        return mSym == sym.mSym;
    }

    /**
     * Arrange string.
     *
     * @param str the str
     */
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

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
     */
    public String toString() {
        return mSym.toString();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
     */

    public int hashCode() {
        if (mSym == null) {
            return 0;
        }
        return mSym.hashCode();
    }

}
