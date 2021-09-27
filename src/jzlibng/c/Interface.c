/*
 * Native method support for io.github.duplexsystem.jzlibng.Interface
 */

#include <dlfcn.h>
#include "cpuinfo_x86.h"

#include "io_github_duplexsystem_jzlibng_Interface.h"


JNIEXPORT jboolean JNICALL Java_io_github_duplexsystem_jzlibng_Interface_supportsExtensions
        (JNIEnv *env, jclass cls) {
    X86Features features = GetX86Info().features;
    return features.avx2 && features.pclmulqdq;
}

JNIEXPORT void JNICALL Java_io_github_duplexsystem_jzlibng_Interface_initSymbols
  (JNIEnv *env, jclass cls, jstring libname) {
      const char *str = (*env)->GetStringUTFChars(env, libname, 0);
      void *cpufeat = dlopen(str, RTLD_LAZY | RTLD_GLOBAL);
      (*env)->ReleaseStringUTFChars(env, libname, str);

      if (!cpufeat) {
          JNU_ThrowRuntimeException(env, "Cannot load library");
          return;
      }
      if(dlerror() != NULL) {
          JNU_ThrowRuntimeException(env, "Error loading load library");
      }
  }