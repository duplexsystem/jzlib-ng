/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
#include <dlfcn.h>
#include "jni.h"

#ifndef JZLIB_NG_SYMBOLUTILS_H
#define JZLIB_NG_SYMBOLUTILS_H

static jint JNU_ThrowRuntimeException(JNIEnv *, char *);
static jint JNU_ThrowOutOfMemoryError(JNIEnv *, char *);
static jint JNU_ThrowInternalError(JNIEnv *, char *);
static jint JNU_ThrowIllegalArgumentException(JNIEnv *, char *);
static jint JNU_ThrowException(JNIEnv *, char *, char *);

/* A helper macro to dlsym the requisite dynamic symbol and bail-out on error. */
#define LOAD_DYNAMIC_SYMBOL(func_ptr, env, handle, symbol) \
  if ((func_ptr = do_dlsym(env, handle, symbol)) == NULL) { \
    return;                                                \
    }

/**
* A helper function to dlsym a 'symbol' from a given library-handle.
*
* @param env jni handle to report contingencies.
* @param handle handle to the dlopen'ed library.
* @param symbol symbol to load.
* @return returns the address where the symbol is loaded in memory,
*         <code>NULL</code> on error.
*/
static void *do_dlsym(JNIEnv *env, void *handle, const char *symbol) {
    if (!env || !handle || !symbol) {
        JNU_ThrowRuntimeException(env, "ENV error");
        return NULL;
    }
    char *error = NULL;
    void *func_ptr = dlsym(handle, symbol);
    if ((error = dlerror()) != NULL) {
        JNU_ThrowRuntimeException(env, "Library not found");
        return NULL;
    }
    return func_ptr;
}

static void *loadLib(JNIEnv *env, jstring libname) {
      const char *str = (*env)->GetStringUTFChars(env, libname, 0);
      void *lib = dlopen(str, RTLD_LAZY | RTLD_GLOBAL);
      (*env)->ReleaseStringUTFChars(env, libname, str);

      if (!lib) {
            JNU_ThrowRuntimeException(env, "Cannot load library");
            return;
      }
      if(dlerror() != NULL) {
            JNU_ThrowRuntimeException(env, "Error loading load library");
      }

      return lib;
}

static jint JNU_ThrowRuntimeException(JNIEnv *env, char *message) {
    jclass exClass;
    return JNU_ThrowException(env, "java/lang/RuntimeException", message);
}

static jint JNU_ThrowOutOfMemoryError(JNIEnv *env, char *message) {
    jclass exClass;
    return JNU_ThrowException(env, "java/lang/OutOfMemoryError", message);
}

static jint JNU_ThrowInternalError(JNIEnv *env, char *message) {
    jclass exClass;
    return JNU_ThrowException(env, "java/lang/InternalError", message);
}

static jint JNU_ThrowIllegalArgumentException(JNIEnv *env, char *message) {
    jclass exClass;
    return JNU_ThrowException(env, "java/lang/IllegalArgumentException", message);
}

static jint JNU_ThrowException(JNIEnv *env, char *className, char *message) {
    jclass exClass;
    exClass = (*env)->FindClass( env, className);
    return (*env)->ThrowNew( env, exClass, message );
}

#endif //JZLIB_NG_SYMBOLUTILS_H
