/*
 * Native method support for io.github.duplexsystem.jzlibng.Interface
 */

#include <dlfcn.h>
#include "cpuinfo_x86.h"
#include "zlib.h"
#include "Utils/ApacheUtils.h"

#include "io_github_duplexsystem_jzlibng_Interface.h"

static int (*dlsym_deflateInit2_)(z_stream *, int, int, int, int, int, char*, int);

JNIEXPORT jboolean JNICALL Java_io_github_duplexsystem_jzlibng_Interface_supportsExtensions
        (JNIEnv *env, jclass cls) {
    X86Features features = GetX86Info().features;
    return features.avx2 && features.pclmulqdq;
}

JNIEXPORT void JNICALL Java_io_github_duplexsystem_jzlibng_Interface_initSymbols
  (JNIEnv *env, jclass cls, jstring libname) {
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
  }