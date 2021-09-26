package io.github.duplexsystem.jzlibng;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public class FastGZIPInputStream extends InflaterInputStream {

    public FastGZIPInputStream(InputStream s) {
        super(s, Interface.newInflator(true));
    }

    public FastGZIPInputStream(InputStream s, int size) {
        super(s, Interface.newInflator(true), size);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
