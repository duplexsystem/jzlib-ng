/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Native method support for java.util.zip.CRC32
 */

#include "jni.h"
#include "zlib.h"
#include "Utils/ApacheUtils.h"
#include "Utils/JavaUtils.h"

#include "io_github_duplexsystem_jzlibng_FastCRC32.h"

static unsigned long (*dlsym_crc32)(unsigned long crc, const unsigned char *buf, unsigned int len);

JNIEXPORT jint JNICALL
Java_io_github_duplexsystem_jzlibng_FastCRC32_update(JNIEnv *env, jclass cls, jint crc, jint b)
{
    Bytef buf[1];

    buf[0] = (Bytef)b;
    return dlsym_crc32(crc, buf, 1);
}

JNIEXPORT jint JNICALL
Java_io_github_duplexsystem_jzlibng_FastCRC32_updateBytes0(JNIEnv *env, jclass cls, jint crc,
                                         jarray b, jint off, jint len)
{
    Bytef *buf = (*env)->GetPrimitiveArrayCritical(env, b, 0);
    if (buf) {
        crc = dlsym_crc32(crc, buf + off, len);
        (*env)->ReleasePrimitiveArrayCritical(env, b, buf, 0);
    }
    return crc;
}

JNIEXPORT jint
ZIP_CRC32(jint crc, const jbyte *buf, jint len)
{
    return dlsym_crc32(crc, (Bytef*)buf, len);
}

JNIEXPORT jint JNICALL
Java_io_github_duplexsystem_jzlibng_FastCRC32_updateByteBuffer0(JNIEnv *env, jclass cls, jint crc,
                                              jlong address, jint off, jint len)
{
    Bytef *buf = (Bytef *)jlong_to_ptr(address);
    if (buf) {
        crc = dlsym_crc32(crc, buf + off, len);
    }
    return crc;
}
JNIEXPORT void JNICALL Java_io_github_duplexsystem_jzlibng_FastCRC32_initSymbols
  (JNIEnv *env, jclass cls, jstring libname)
{
    void *lib = loadLib(env, libname);

    LOAD_DYNAMIC_SYMBOL(dlsym_crc32, env, lib, "crc32");
}