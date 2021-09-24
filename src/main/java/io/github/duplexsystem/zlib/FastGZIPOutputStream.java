package io.github.duplexsystem.zlib;

import java.io.IOException;
import java.io.OutputStream;

public class FastGZIPOutputStream extends FastDeflaterOutputStream {

    public FastGZIPOutputStream(OutputStream s) {
        super(s, new FastDeflater(FastDeflater.DEFAULT_COMPRESSION, true));
    }

    @Override
    public void close() throws IOException {
        super.close();
        getDeflater().end();
    }
}
