package com.github.dakusui.jcunit8.examples.pict;

/**
 * <a href="https://github.com/Microsoft/pict/issues/11">Issue-11 of PICT</a>
 * <code>
 * I'd like to be able to use parameters that are lists or sets.
 * <p>
 * Today I can do something like the following:
 * <p>
 * PLATFORM:   x86, ia64, amd64
 * CPUS:       Single, Dual, Quad
 * RAM:        128MB, 1GB, 4GB, 64GB
 * HDD:        SCSI, IDE
 * OS:         NT4, Win2K, WinXP, Win2K3
 * IE:         4.0, 5.0, 5.5, 6.0
 * APPS_size:  0, 1, 2
 * APPS_0:     n/a, SQLServer, Exchange, Office
 * APPS_1:     n/a, SQLServer, Exchange, Office
 * <p>
 * IF [APPS_size] = 0 THEN [APPS_0] = "n/a" AND [APPS_1] = "n/a";
 * IF [APPS_size] = 1 THEN [APPS_0] <> "n/a" AND [APPS_1] = "n/a";
 * IF [APPS_size] = 2 THEN [APPS_0] <> "n/a" AND [APPS_1] <> "n/a" AND [APPS_0] <> [APPS_1];
 * <p>
 * I'd like to be able to write something more succinct though. e.g.:
 * <p>
 * PLATFORM:   x86, ia64, amd64
 * CPUS:       Single, Dual, Quad
 * RAM:        128MB, 1GB, 4GB, 64GB
 * HDD:        SCSI, IDE
 * OS:         NT4, Win2K, WinXP, Win2K3
 * IE:         4.0, 5.0, 5.5, 6.0
 * APPS (Set): 0..2 of { SQLServer, Exchange, Office }
 * <p>
 * For a list the above example would not have [APPS_0] <> [APPS_1] and would have APPS (List) instead of APPS (Set) (or some similar grammar).
 * </code>
 */
public class Issue11 {
}
