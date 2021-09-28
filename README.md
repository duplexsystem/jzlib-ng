jzlib-ng is a reimplementation of some of Java's java.util.zip classes using zlib-ng. 

OpenJDK's java.util.zip use zlib for compression, while zlib is battle tested and works on just about every platform, it currently only accepts bug fixes, and it practically maintenance only. For high performance applications that don't care about things such as running on 16bit hardware there is a lot of performance left on the table when using plain zlib. In order to remedy that jzlib-ng reimplements some java.util.zip's classes and link's the JNI code against zlib-ng, a faster actively improved version of zlib.

Because a lot of this code is copied from OpenJDK I would like to keep it close to OpenJDK's src but perf improvements are welcome.