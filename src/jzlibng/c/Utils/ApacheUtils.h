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
