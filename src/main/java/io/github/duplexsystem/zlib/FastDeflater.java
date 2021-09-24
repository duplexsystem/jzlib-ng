/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package io.github.duplexsystem.zlib;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * drop in replacement for java.util.zip.Deflater
 */
public class FastDeflater {

    private final ZStreamRef zsRef;
    private byte[] buf = new byte[0];
    private int off, len;
    private int level, strategy;
    private boolean setParams;
    private boolean finish, finished;
    private long bytesRead;
    private long bytesWritten;

    public static final int DEFLATED = 8;
    public static final int NO_COMPRESSION = 0;
    public static final int BEST_SPEED = 1;
    public static final int BEST_COMPRESSION = 9;
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * Compression strategy best used for data consisting mostly of small
     * values with a somewhat random distribution. Forces more Huffman coding
     * and less string matching.
     */
    public static final int FILTERED = 1;

    /**
     * Compression strategy for Huffman coding only.
     */
    public static final int HUFFMAN_ONLY = 2;

    public static final int DEFAULT_STRATEGY = 0;
    public static final int NO_FLUSH = 0;
    public static final int SYNC_FLUSH = 2;
    public static final int FULL_FLUSH = 3;

    static {
        String libzName = System.getProperty("libz.name");
        if(System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            System.load(System.getProperty("user.home") + "/.m2/repository/com/bluedevel/fastzlib-dylib/1.0-SNAPSHOT/fastzlib-dylib-1.0-SNAPSHOT.dylib");
            if(libzName == null) {
                libzName = "libz.dylib";
            }
        } else {
            if(libzName == null) {
                libzName = "libz.so";
            }
            System.load(System.getProperty("user.home") + "/fastzlib/fastzlib-so-1.0-SNAPSHOT.so");
        }
        System.out.println("loading libz from " + libzName);
        initIDs(libzName);
    }

    public FastDeflater(int level, boolean nowrap) {
        this.level = level;
        this.strategy = DEFAULT_STRATEGY;
        this.zsRef = new ZStreamRef(init(level, DEFAULT_STRATEGY, nowrap));
    }

    public FastDeflater(int level) {
        this(level, false);
    }

    public FastDeflater() {
        this(DEFAULT_COMPRESSION, false);
    }

    public void setInput(byte[] b, int off, int len) {
        if (b== null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            this.buf = b;
            this.off = off;
            this.len = len;
        }
    }

    public void setInput(byte[] b) {
        setInput(b, 0, b.length);
    }

    public void setStrategy(int strategy) {
        switch (strategy) {
          case DEFAULT_STRATEGY:
          case FILTERED:
          case HUFFMAN_ONLY:
            break;
          default:
            throw new IllegalArgumentException();
        }
        synchronized (zsRef) {
            if (this.strategy != strategy) {
                this.strategy = strategy;
                setParams = true;
            }
        }
    }

    public void setLevel(int level) {
        if ((level < 0 || level > 9) && level != DEFAULT_COMPRESSION) {
            throw new IllegalArgumentException("invalid compression level");
        }
        synchronized (zsRef) {
            if (this.level != level) {
                this.level = level;
                setParams = true;
            }
        }
    }

    public boolean needsInput() {
        return len <= 0;
    }

    public void finish() {
        synchronized (zsRef) {
            finish = true;
        }
    }

    public boolean finished() {
        synchronized (zsRef) {
            return finished;
        }
    }

    public int deflate(byte[] b, int off, int len) {
        return deflate(b, off, len, NO_FLUSH);
    }

    public int deflate(byte[] b) {
        return deflate(b, 0, b.length, NO_FLUSH);
    }

    public int deflate(byte[] b, int off, int len, int flush) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (zsRef) {
            ensureOpen();
            if (flush == NO_FLUSH || flush == SYNC_FLUSH ||
                flush == FULL_FLUSH) {
                int thisLen = this.len;
                int n = deflateBytes(zsRef.address(), b, off, len, flush);
                bytesWritten += n;
                bytesRead += (thisLen - this.len);
                return n;
            }
            throw new IllegalArgumentException();
        }
    }

    public int getTotalIn() {
        return (int) getBytesRead();
    }

    public long getBytesRead() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesRead;
        }
    }

    public int getTotalOut() {
        return (int) getBytesWritten();
    }

    public long getBytesWritten() {
        synchronized (zsRef) {
            ensureOpen();
            return bytesWritten;
        }
    }

    public void reset() {
        synchronized (zsRef) {
            ensureOpen();
            reset(zsRef.address());
            finish = false;
            finished = false;
            off = len = 0;
            bytesRead = bytesWritten = 0;
        }
    }

    public void end() {
        synchronized (zsRef) {
            long addr = zsRef.address();
            zsRef.clear();
            if (addr != 0) {
                end(addr);
                buf = null;
            }
        }
    }

    protected void finalize() {
        end();
    }

    private void ensureOpen() {
        assert Thread.holdsLock(zsRef);
        if (zsRef.address() == 0)
            throw new NullPointerException("Deflater has been closed");
    }

    private static native void initIDs(String libName);
    private native static long init(int level, int strategy, boolean nowrap);
    private native int deflateBytes(long addr, byte[] b, int off, int len,
                                    int flush);
    private native static void reset(long addr);
    private native static void end(long addr);

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("/Users/kireet/ifconfig_0935.txt");
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int n;
        while((n = fis.read(buf, 0, buf.length)) > 0) {
            input.write(buf, 0, n);
        }
        fis.close();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FastDeflater def = new FastDeflater(DEFAULT_COMPRESSION, true);
        FastDeflaterOutputStream ostream = new FastDeflaterOutputStream(bos, def);

        ostream.write(input.toByteArray());
        System.out.println(def.getBytesWritten());
        ostream.close();
        def.end();
        InflaterInputStream is = new GZIPInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = r.readLine()) != null) {
            System.out.println("***: " + line);
        }
        r.close();
    }
}
