package io.github.duplexsystem.jzlibng;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

public class FastGZIPOutputStream extends DeflaterOutputStream {

    public FastGZIPOutputStream(OutputStream s) {
        super(s, Interface.newDeflater(FastDeflater.DEFAULT_COMPRESSION, true));
    }

    public FastGZIPOutputStream(OutputStream s, int size) {
        super(s, Interface.newDeflater(FastDeflater.DEFAULT_COMPRESSION, true), size);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
