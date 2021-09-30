/*
 * Native method support for io.github.duplexsystem.jzlibng.Interface
 */

#include <dlfcn.h>
#include "cpuinfo_x86.h"
#include "zlib.h"
#include "Utils/ApacheUtils.h"

#include "io_github_duplexsystem_jzlibng_Interface.h"

JNIEXPORT jboolean JNICALL Java_io_github_duplexsystem_jzlibng_Interface_supportsExtensions
        (JNIEnv *env, jclass cls) {
    X86Features features = GetX86Info().features;
    return features.avx2 && features.pclmulqdq;
}

JNIEXPORT void JNICALL Java_io_github_duplexsystem_jzlibng_Interface_initSymbols
  (JNIEnv *env, jclass cls, jstring libname) {
      void *lib = loadLib(env, libname);
  }