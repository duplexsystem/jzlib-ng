#include "zlib.h"

#ifndef JZLIB_NG_DYNDEFS_H
#define JZLIB_NG_DYNDEFS_H

static int (*dlsym_deflateInit2_)(z_stream *, int, int, int, int, int, char*, int);
static int (*dlsym_deflate)(z_stream *, int);
static int (*dlsym_deflateSetDictionary)(z_stream *, const unsigned char *, unsigned int);
static int (*dlsym_deflateReset)(z_stream *);
static int (*dlsym_deflateEnd)(z_stream *);
static int (*dlsym_deflateParams)(z_stream *, int, int);

static int (*dlsym_inflateInit2_)(z_stream *, int, char*, int);
static int (*dlsym_inflate)(z_stream *, int);
static int (*dlsym_inflateSetDictionary)(z_stream *, const unsigned char *, unsigned int);
static int (*dlsym_inflateReset)(z_stream *);
static int (*dlsym_inflateEnd)(z_stream *);

#endif //JZLIB_NG_DYNDEFS_H
