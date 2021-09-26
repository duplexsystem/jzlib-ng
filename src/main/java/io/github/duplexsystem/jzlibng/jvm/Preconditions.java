package io.github.duplexsystem.jzlibng.jvm;

/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Preconditions {
    public static final BiFunction<String, List<Number>, ArrayIndexOutOfBoundsException>
            AIOOBE_FORMATTER = Preconditions.outOfBoundsExceptionFormatter(new Function<>() {
        @Override
        public ArrayIndexOutOfBoundsException apply(String s) {
            return new ArrayIndexOutOfBoundsException(s);
        }
    });

    public static <X extends RuntimeException>
    BiFunction<String, List<Number>, X> outOfBoundsExceptionFormatter(Function<String, X> f) {
        // Use anonymous class to avoid bootstrap issues if this method is
        // used early in startup
        return new BiFunction<String, List<Number>, X>() {
            @Override
            public X apply(String checkKind, List<Number> args) {
                return f.apply(outOfBoundsMessage(checkKind, args));
            }
        };
    }

    public static <X extends RuntimeException>
    int checkFromIndexSize(int fromIndex, int size, int length,
                           BiFunction<String, List<Number>, X> oobef) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex)
            throw outOfBoundsCheckFromIndexSize(oobef, fromIndex, size, length);
        return fromIndex;
    }

    private static RuntimeException outOfBoundsCheckFromIndexSize(
            BiFunction<String, List<Number>, ? extends RuntimeException> oobe,
            int fromIndex, int size, int length) {
        return outOfBounds(oobe, "checkFromIndexSize", fromIndex, size, length);
    }

    private static RuntimeException outOfBounds(
            BiFunction<String, List<Number>, ? extends RuntimeException> oobef,
            String checkKind,
            Number... args) {
        List<Number> largs = List.of(args);
        RuntimeException e = oobef == null
                ? null : oobef.apply(checkKind, largs);
        return e == null
                ? new IndexOutOfBoundsException(outOfBoundsMessage(checkKind, largs)) : e;
    }

    private static String outOfBoundsMessage(String checkKind, List<? extends Number> args) {
        if (checkKind == null && args == null) {
            return String.format("Range check failed");
        } else if (checkKind == null) {
            return String.format("Range check failed: %s", args);
        } else if (args == null) {
            return String.format("Range check failed: %s", checkKind);
        }

        int argSize = 0;
        switch (checkKind) {
            case "checkIndex":
                argSize = 2;
                break;
            case "checkFromToIndex":
            case "checkFromIndexSize":
                argSize = 3;
                break;
            default:
        }

        // Switch to default if fewer or more arguments than required are supplied
        switch ((args.size() != argSize) ? "" : checkKind) {
            case "checkIndex":
                return String.format("Index %d out of bounds for length %d",
                        args.get(0), args.get(1));
            case "checkFromToIndex":
                return String.format("Range [%d, %d) out of bounds for length %d",
                        args.get(0), args.get(1), args.get(2));
            case "checkFromIndexSize":
                return String.format("Range [%d, %<d + %d) out of bounds for length %d",
                        args.get(0), args.get(1), args.get(2));
            default:
                return String.format("Range check failed: %s %s", checkKind, args);
        }
    }
}
