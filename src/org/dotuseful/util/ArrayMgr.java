/*
 * Created on 25/6/2004
 *
 * Copyright (C) 2004 Denis Krukovsky. All rights reserved.
 * ====================================================================
 * The Software License (based on Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Denis Krukovsky (dkrukovsky at yahoo.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "dot useful" and "Denis Krukovsky" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact dkrukovsky at yahoo.com.
 *
 * 5. Products derived from this software may not be called "useful",
 *    nor may "useful" appear in their name, without prior written
 *    permission of Denis Krukovsky.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.dotuseful.util;

/**
 * This class contains various methods for manipulating arrays in addition to
 * <code>java.util.Arrays</code> class.
 * 
 * @author dkrukovsky
 * @see java.util.Arrays
 */
public class ArrayMgr {
    // Suppresses default constructor, ensuring non-instantiability.
    private ArrayMgr() {
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the boolean
     * array being searched, and the target is the boolean array being searched
     * for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(boolean[] source, boolean[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the byte
     * array being searched, and the target is the byte array being searched
     * for.
     * 
     * @param source
     *            the bytes being searched.
     * @param target
     *            the bytes being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(byte[] source, byte[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the char
     * array being searched, and the target is the char array being searched
     * for.
     * 
     * @param source
     *            the chars being searched.
     * @param target
     *            the chars being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(char[] source, char[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the short
     * array being searched, and the target is the short array being searched
     * for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(short[] source, short[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the int
     * array being searched, and the target is the int array being searched for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(int[] source, int[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the array of
     * long being searched, and the target is the array of long being searched
     * for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(long[] source, long[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the double
     * array being searched, and the target is the double array being searched
     * for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(double[] source, double[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the double
     * array being searched, and the target is the double array being searched
     * for.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(float[] source, float[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen) && (source[i + j] == target[j])) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }

    /**
     * Returns the index within source array of the first occurrence of the
     * target array, starting at the specified index. The source is the array of
     * Object being searched, and the target is the array of Object being
     * searched for. Two objects <tt>e1</tt> and <tt>e2</tt> are considered
     * <i>equal </i> if <tt>(e1==null ? e2==null
     * : e1.equals(e2))</tt>.
     * 
     * @param source
     *            the array being searched.
     * @param target
     *            the array being searched for.
     * @param fromIndex
     *            the index to begin searching from.
     * @return the index within source array of the first occurrence of the
     *         target array, starting at the specified index.
     */
    public static int indexOf(Object[] source, Object[] target, int fromIndex) {
        int sourceLen = source.length;
        int targetLen = target.length;
        if ((fromIndex > sourceLen) || (fromIndex < 0)) {
            throw new IllegalArgumentException(fromIndex
                    + " is not between 0 and " + sourceLen);
        } else {
            if (targetLen == 0) {
                return fromIndex;
            } else {
                int i = fromIndex;
                int max = sourceLen - targetLen;
                int j;
                while (i <= max) {
                    j = 0;
                    while ((j < targetLen)
                            && (source[i + j] == null ? target[j] == null
                                    : source[i + j].equals(target[j]))) {
                        j++;
                    }
                    if (j == targetLen) {
                        return i;
                    } else {
                        i++;
                    }
                }
                return -1;
            }
        }
    }
}