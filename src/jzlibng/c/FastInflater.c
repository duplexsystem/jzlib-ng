#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <zlib.h>
#include <dlfcn.h>

#define DEF_MEM_LEVEL 8
#define GZIP_ENCODING 16

/* A helper macro to dlsym the requisite dynamic symbol and bail-out on error. */
#define LOAD_DYNAMIC_SYMBOL(func_ptr, env, handle, symbol) \
  if ((func_ptr = do_dlsym(env, handle, symbol)) == NULL) { \
    return; \
  }

static jfieldID levelID;
static jfieldID strategyID;
static jfieldID setParamsID;
static jfieldID finishID;
static jfieldID finishedID;
static jfieldID bufID, offID, lenID;

static int (*dlsym_deflateInit2_)(z_streamp, int, int, int, int, int, char*, int);
static int (*dlsym_deflate)(z_streamp, int);
static int (*dlsym_deflateSetDictionary)(z_streamp, const Bytef *, uInt);
static int (*dlsym_deflateReset)(z_streamp);
static int (*dlsym_deflateEnd)(z_streamp);
static int (*dlsym_deflateParams)(z_streamp, int, int);
static jint throwRuntimeException(JNIEnv *, char *);
static jint throwException(JNIEnv *, char *, char *);


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
  	throwRuntimeException(env, "env error");
  	return NULL;
  }
  char *error = NULL;
  void *func_ptr = dlsym(handle, symbol);
  if ((error = dlerror()) != NULL) {
  	throwRuntimeException(env, "library not found");
  	return NULL;
  }
  return func_ptr;
}


static jint throwRuntimeException(JNIEnv *env, char *message) {
    jclass exClass;
    return throwException(env, "java/lang/RuntimeException", message);
}

static jint throwException(JNIEnv *env, char *className, char *message) {
    jclass exClass;
    exClass = (*env)->FindClass( env, className);
    return (*env)->ThrowNew( env, exClass, message );
}

JNIEXPORT void JNICALL Java_com_bluedevel_zlib_FastInflater_initIDs
  (JNIEnv *env, jclass cls, jstring libname) {
      const char *str = (*env)->GetStringUTFChars(env, libname, 0);
      void *libz = dlopen(str, RTLD_LAZY | RTLD_GLOBAL);
      (*env)->ReleaseStringUTFChars(env, libname, str);

      if (!libz) {
          throwRuntimeException(env, "Cannot load libz");
          return;
      }
      if(dlerror() != NULL) {
          throwRuntimeException(env, "Error loading load libz");
      }

      // load symbols dynamically, so as not to use the jvm-provided functions
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateInit2_, env, libz, "deflateInit2_");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflate, env, libz, "deflate");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateParams, env, libz, "deflateParams");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateSetDictionary, env, libz, "deflateSetDictionary");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateReset, env, libz, "deflateReset");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateParams, env, libz, "deflateParams");
      LOAD_DYNAMIC_SYMBOL(dlsym_deflateEnd, env, libz, "deflateEnd");

      levelID = (*env)->GetFieldID(env, cls, "level", "I");
      strategyID = (*env)->GetFieldID(env, cls, "strategy", "I");
      setParamsID = (*env)->GetFieldID(env, cls, "setParams", "Z");
      finishID = (*env)->GetFieldID(env, cls, "finish", "Z");
      finishedID = (*env)->GetFieldID(env, cls, "finished", "Z");
      bufID = (*env)->GetFieldID(env, cls, "buf", "[B");
      offID = (*env)->GetFieldID(env, cls, "off", "I");
      lenID = (*env)->GetFieldID(env, cls, "len", "I");
}

/*
JNIEXPORT jlong JNICALL Java_com_bluedevel_zlib_FastDeflater_init
  (JNIEnv *env, jclass clazz, jint level, jint strategy, jboolean nowrap) {
    z_stream *strm = calloc(1, sizeof(z_stream));

    if (strm == 0) {
        throwException(env, "java/lang/OutOfMemoryError", "calloc");
        return 0;
    } else {
        int wbits = nowrap ? (MAX_WBITS | GZIP_ENCODING) : -MAX_WBITS;
        switch (dlsym_deflateInit2_(strm, level, Z_DEFLATED,
                             wbits,
                             DEF_MEM_LEVEL, strategy,
                             "1.2.8", sizeof(z_stream))) {
          case Z_OK:
            return (uintptr_t) strm;
          case Z_MEM_ERROR:
            free(strm);
            throwException(env, "java/lang/OutOfMemoryError", "free");
            return 0;
          case Z_STREAM_ERROR:
            free(strm);
            throwException(env, "java/lang/OutOfMemoryError", "z_stream free");
            return 0;
          default:
            free(strm);
            throwRuntimeException(env, "unexpected value");
            return 0;
        }
    }

    // never get here
    return 0;
}

JNIEXPORT void JNICALL Java_com_bluedevel_zlib_FastDeflater_setDictionary
  (JNIEnv *env, jclass clazz, jlong addr, jbyteArray b, jint off, jint len) {
      throwException(env, "java/lang/UnsupportedOperationException", "unsupported");
  }

JNIEXPORT jint JNICALL Java_com_bluedevel_zlib_FastDeflater_deflateBytes
  (JNIEnv *env, jobject this, jlong addr, jbyteArray b, jint off, jint len, jint flush) {
        z_stream *strm = (uintptr_t) addr;

    jarray this_buf = (*env)->GetObjectField(env, this, bufID);
    jint this_off = (*env)->GetIntField(env, this, offID);
    jint this_len = (*env)->GetIntField(env, this, lenID);
    jbyte *in_buf;
    jbyte *out_buf;
    int res;
    if ((*env)->GetBooleanField(env, this, setParamsID)) {
        int level = (*env)->GetIntField(env, this, levelID);
        int strategy = (*env)->GetIntField(env, this, strategyID);
        in_buf = (*env)->GetPrimitiveArrayCritical(env, this_buf, 0);
        if (in_buf == NULL) {
            // Throw OOME only when length is not zero
            if (this_len != 0)
                return throwException(env, "java/lang/OutOfMemoryError", "can't access in_buf");
        }
        out_buf = (*env)->GetPrimitiveArrayCritical(env, b, 0);
        if (out_buf == NULL) {
            (*env)->ReleasePrimitiveArrayCritical(env, this_buf, in_buf, 0);
            if (len != 0)
                return throwException(env, "java/lang/OutOfMemoryError", "can't access out_buf");
        }

        strm->next_in = (Bytef *) (in_buf + this_off);
        strm->next_out = (Bytef *) (out_buf + off);
        strm->avail_in = this_len;
        strm->avail_out = len;
        res = dlsym_deflateParams(strm, level, strategy);
        (*env)->ReleasePrimitiveArrayCritical(env, b, out_buf, 0);
        (*env)->ReleasePrimitiveArrayCritical(env, this_buf, in_buf, 0);

        switch (res) {
        case Z_OK:
            (*env)->SetBooleanField(env, this, setParamsID, JNI_FALSE);
            this_off += this_len - strm->avail_in;
            (*env)->SetIntField(env, this, offID, this_off);
            (*env)->SetIntField(env, this, lenID, strm->avail_in);
            return len - strm->avail_out;
        case Z_BUF_ERROR:
            (*env)->SetBooleanField(env, this, setParamsID, JNI_FALSE);
            return 0;
        default:
            return throwException(env, "java/lang/InternalError", strm->msg);
        }
    } else {
        jboolean finish = (*env)->GetBooleanField(env, this, finishID);
        in_buf = (*env)->GetPrimitiveArrayCritical(env, this_buf, 0);
        if (in_buf == NULL) {
            if (this_len != 0)
                return throwException(env, "java/lang/OutOfMemoryError", "in_buf null");
        }
        out_buf = (*env)->GetPrimitiveArrayCritical(env, b, 0);
        if (out_buf == NULL) {
            (*env)->ReleasePrimitiveArrayCritical(env, this_buf, in_buf, 0);
            if (len != 0)
                return throwException(env, "java/lang/OutOfMemoryError", "out_buf null");
        }

        strm->next_in = (Bytef *) (in_buf + this_off);
        strm->next_out = (Bytef *) (out_buf + off);
        strm->avail_in = this_len;
        strm->avail_out = len;
        res = dlsym_deflate(strm, finish ? Z_FINISH : flush);
        (*env)->ReleasePrimitiveArrayCritical(env, b, out_buf, 0);
        (*env)->ReleasePrimitiveArrayCritical(env, this_buf, in_buf, 0);

        switch (res) {
        case Z_STREAM_END:
            (*env)->SetBooleanField(env, this, finishedID, JNI_TRUE);
            // fall through
        case Z_OK:
            this_off += this_len - strm->avail_in;
            (*env)->SetIntField(env, this, offID, this_off);
            (*env)->SetIntField(env, this, lenID, strm->avail_in);
            return len - strm->avail_out;
        case Z_BUF_ERROR:
            return 0;
        default:
            throwException(env, "java/lang/InternalError", strm->msg);
            return 0;
        }
    }
}

JNIEXPORT void JNICALL Java_com_bluedevel_zlib_FastDeflater_reset
  (JNIEnv *env, jclass clazz, jlong addr) {
    if (dlsym_deflateReset((z_stream *) addr) != Z_OK) {
        throwException(env, "java/lang/InternalError", "reset error");
    }
}

JNIEXPORT void JNICALL Java_com_bluedevel_zlib_FastDeflater_end
  (JNIEnv *env, jclass clazz, jlong addr) {
    if (dlsym_deflateEnd((z_stream *) addr) == Z_STREAM_ERROR) {
        throwException(env, "java/lang/InternalError", "end error");
    } else {
        free((z_stream *) (addr));
    }
  }
*/