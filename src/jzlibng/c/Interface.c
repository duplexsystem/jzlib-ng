/*
 * Native method support for io.github.duplexsystem.jzlibng.Interface
 */

#include <dlfcn.h>
#include "cpuinfo_x86.h"
#include "zlib.h"
#include "Utils/ApacheUtils.h"
#include "Utils/DynamicPointers.h"

#include "io_github_duplexsystem_jzlibng_Interface.h"

static jint throwRuntimeException(JNIEnv *, char *);
static jint throwException(JNIEnv *, char *, char *);


JNIEXPORT jboolean JNICALL Java_io_github_duplexsystem_jzlibng_Interface_supportsExtensions
        (JNIEnv *env, jclass cls) {
    X86Features features = GetX86Info().features;
    return features.avx2 && features.pclmulqdq;
}

JNIEXPORT void JNICALL Java_io_github_duplexsystem_jzlibng_Interface_initSymbols
  (JNIEnv *env, jclass cls, jstring libname, jboolean islibz) {
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

      if(islibz) {
            LOAD_DYNAMIC_SYMBOL(dlsym_deflateInit2_, env, lib, "deflateInit2_");
            LOAD_DYNAMIC_SYMBOL(dlsym_deflate, env, lib, "deflate");
            LOAD_DYNAMIC_SYMBOL(dlsym_deflateSetDictionary, env, lib, "deflateSetDictionary");
            LOAD_DYNAMIC_SYMBOL(dlsym_deflateReset, env, lib, "deflateReset");
            LOAD_DYNAMIC_SYMBOL(dlsym_deflateEnd, env, lib, "deflateEnd");
            LOAD_DYNAMIC_SYMBOL(dlsym_deflateParams, env, lib, "deflateParams");

            LOAD_DYNAMIC_SYMBOL(dlsym_inflateInit2_, env, lib, "inflateInit2_");
            LOAD_DYNAMIC_SYMBOL(dlsym_inflate, env, lib, "inflate");
            LOAD_DYNAMIC_SYMBOL(dlsym_inflateSetDictionary, env, lib, "inflateSetDictionary");
            LOAD_DYNAMIC_SYMBOL(dlsym_inflateReset, env, lib, "inflateReset");
            LOAD_DYNAMIC_SYMBOL(dlsym_inflateEnd, env, lib, "inflateEnd");
      }
  }