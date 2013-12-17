/*
 * $HeadURL: http://juliusdavies.ca/svn/not-yet-commons-ssl/tags/commons-ssl-0.3.11/src/java/org/apache/commons/ssl/Util.java $
 * $Revision: 132 $
 * $Date: 2008-01-11 21:20:26 -0800 (Fri, 11 Jan 2008) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Credit Union Central of British Columbia
 * @author <a href="http://www.cucbc.com/">www.cucbc.com</a>
 * @author <a href="mailto:juliusdavies@cucbc.com">juliusdavies@cucbc.com</a>
 * @since 28-Feb-2006
 */
public class Util {
    public final static int SIZE_KEY = 0;
    public final static int LAST_READ_KEY = 1;

    public static boolean isYes(String yesString) {
        if (yesString == null) {
            return false;
        }
        String s = yesString.trim().toUpperCase();
        return "1".equals(s) || "YES".equals(s) || "TRUE".equals(s) ||
            "ENABLE".equals(s) || "ENABLED".equals(s) || "Y".equals(s) ||
            "ON".equals(s);
    }

    public static String trim(final String s) {
        if (s == null || "".equals(s)) {
            return s;
        }
        int i = 0;
        int j = s.length() - 1;
        while (isWhiteSpace(s.charAt(i))) {
            i++;
        }
        while (isWhiteSpace(s.charAt(j))) {
            j--;
        }
        return j >= i ? s.substring(i, j + 1) : "";
    }

    public static boolean isWhiteSpace(final char c) {
        switch (c) {
            case 0:
            case ' ':
            case '\t':
            case '\n':
            case '\r':
            case '\f':
                return true;
            default:
                return false;
        }
    }

    public static void pipeStream(InputStream in, OutputStream out)
        throws IOException {
        pipeStream(in, out, true);
    }

    public static void pipeStream(InputStream in, OutputStream out,
                                  boolean autoClose)
        throws IOException {
        byte[] buf = new byte[8192];
        IOException ioe = null;
        try {
            int bytesRead = in.read(buf);
            while (bytesRead >= 0) {
                if (bytesRead > 0) {
                    out.write(buf, 0, bytesRead);
                }
                bytesRead = in.read(buf);
            }
        } finally {
            // Probably it's best to let consumer call "close", but I'm usually
            // the consumer, and I want to be lazy.  [Julius, November 20th, 2006]
            try {
                in.close();
            } catch (IOException e) {
                ioe = e;
            }
            if (autoClose) {
                try {
                    out.close();
                } catch (IOException e) {
                    ioe = e;
                }
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    public static byte[] streamToBytes(final ByteArrayInputStream in,
                                       int maxLength) {
        byte[] buf = new byte[maxLength];
        int[] status = fill(buf, 0, in);
        int size = status[SIZE_KEY];
        if (buf.length != size) {
            byte[] smallerBuf = new byte[size];
            System.arraycopy(buf, 0, smallerBuf, 0, size);
            buf = smallerBuf;
        }
        return buf;
    }

    public static byte[] streamToBytes(final InputStream in, int maxLength)
        throws IOException {
        byte[] buf = new byte[maxLength];
        int[] status = fill(buf, 0, in);
        int size = status[SIZE_KEY];
        if (buf.length != size) {
            byte[] smallerBuf = new byte[size];
            System.arraycopy(buf, 0, smallerBuf, 0, size);
            buf = smallerBuf;
        }
        return buf;
    }

    public static byte[] streamToBytes(final InputStream in) throws IOException {
        byte[] buf = new byte[4096];
        try {
            int[] status = fill(buf, 0, in);
            int size = status[SIZE_KEY];
            int lastRead = status[LAST_READ_KEY];
            while (lastRead != -1) {
                buf = resizeArray(buf);
                status = fill(buf, size, in);
                size = status[SIZE_KEY];
                lastRead = status[LAST_READ_KEY];
            }
            if (buf.length != size) {
                byte[] smallerBuf = new byte[size];
                System.arraycopy(buf, 0, smallerBuf, 0, size);
                buf = smallerBuf;
            }
        } finally {
            in.close();
        }
        return buf;
    }

    public static byte[] streamToBytes(final ByteArrayInputStream in) {
        byte[] buf = new byte[4096];
        int[] status = fill(buf, 0, in);
        int size = status[SIZE_KEY];
        int lastRead = status[LAST_READ_KEY];
        while (lastRead != -1) {
            buf = resizeArray(buf);
            status = fill(buf, size, in);
            size = status[SIZE_KEY];
            lastRead = status[LAST_READ_KEY];
        }
        if (buf.length != size) {
            byte[] smallerBuf = new byte[size];
            System.arraycopy(buf, 0, smallerBuf, 0, size);
            buf = smallerBuf;
        }
        // in.close();  <-- this is a no-op on ByteArrayInputStream.
        return buf;
    }

    public static int[] fill(final byte[] buf, final int offset,
                             final InputStream in)
        throws IOException {
        int read = in.read(buf, offset, buf.length - offset);
        int lastRead = read;
        if (read == -1) {
            read = 0;
        }
        while (lastRead != -1 && read + offset < buf.length) {
            lastRead = in.read(buf, offset + read, buf.length - read - offset);
            if (lastRead != -1) {
                read += lastRead;
            }
        }
        return new int[]{offset + read, lastRead};
    }

    public static int[] fill(final byte[] buf, final int offset,
                             final ByteArrayInputStream in) {
        int read = in.read(buf, offset, buf.length - offset);
        int lastRead = read;
        if (read == -1) {
            read = 0;
        }
        while (lastRead != -1 && read + offset < buf.length) {
            lastRead = in.read(buf, offset + read, buf.length - read - offset);
            if (lastRead != -1) {
                read += lastRead;
            }
        }
        return new int[]{offset + read, lastRead};
    }

    public static byte[] resizeArray(final byte[] bytes) {
        byte[] biggerBytes = new byte[bytes.length * 2];
        System.arraycopy(bytes, 0, biggerBytes, 0, bytes.length);
        return biggerBytes;
    }

    public static String pad(String s, final int length, final boolean left) {
        if (s == null) {
            s = "";
        }
        int diff = length - s.length();
        if (diff == 0) {
            return s;
        } else if (diff > 0) {
            StringBuffer sb = new StringBuffer();
            if (left) {
                for (int i = 0; i < diff; i++) {
                    sb.append(' ');
                }
            }
            sb.append(s);
            if (!left) {
                for (int i = 0; i < diff; i++) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        } else {
            return s;
        }
    }
}
