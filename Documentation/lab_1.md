# Lab 1: convolution matrix, sequential vs parallel

Applies a `k x k` convolution matrix `C` to an `n x m` matrix `F` and writes the result `V` to `output.txt`, in Java and C++. Outside the matrix the nearest border value is used (`f[-1][j] = f[0][j]`, etc.).

| File | What it does |
|---|---|
| `sequential.java`, `sequential_static.cpp`, `sequential_dynamic.cpp` | One thread computes all of `V`. |
| `parallel.java`, `parallel_static.cpp`, `parallel_dynamic.cpp` | `p` threads. **Horizontal** (`h`): each thread gets a band of rows. **Vertical** (`v`): each thread gets a band of columns. Bands differ by at most one row/column. Each thread writes only its own part of `V`, so no locks are needed. |
| `run.py` | Generates the inputs, compiles, runs everything and checks the results. |

Static: `int f[MAX][MAX]` global arrays. Dynamic: one `new int[m]` per row.

## Instructions

`python run.py` runs all test cases, `python run.py <i>` only test case `i` (0-4). For each test case the script:
1. Writes a random `MatrixF.txt` (values 0..255) and `ConvolutionM.txt` (values -5..5), used by all runs of that case.
2. Runs each sequential program 10 times, then each parallel program 10 times for every thread count and both variants.
3. After every parallel run, checks that `output.txt` is identical to the sequential output and stops with an error if it differs.
4. Prints the averages and saves every individual time to `times.csv`.

Manual use: `java parallel output.txt 4 h`, `./par_static output.txt 4 v`, `./seq_dynamic output.txt`.
Only the computation is timed (thread creation and join included for the parallel versions), not reading or writing files.

## Results

Machine: Intel Core Ultra 9 185H, 16 cores / 22 threads (the 10000x10000 case was run on a different machine). Average of 10 runs, in ms. All parallel outputs were identical to the sequential output. Averages are also in `Code/lab_1/averages.csv`.

### Java

| Matrix size | Variant | Threads | Avg time (ms) | Speedup (Tseq/Tpar) |
|---|---|---|---|---|
| 10x10, k=3 | sequential | 1 | 0.258 | 1.000 |
| 10x10, k=3 | horizontal | 4 | 59.844 | 0.004 |
| 10x10, k=3 | vertical | 4 | 61.660 | 0.004 |
| 1000x1000, k=3 | sequential | 1 | 29.154 | 1.000 |
| 1000x1000, k=3 | horizontal | 2 | 88.742 | 0.329 |
| 1000x1000, k=3 | horizontal | 4 | 101.232 | 0.288 |
| 1000x1000, k=3 | horizontal | 8 | 115.175 | 0.253 |
| 1000x1000, k=3 | horizontal | 16 | 150.302 | 0.194 |
| 1000x1000, k=3 | vertical | 2 | 85.466 | 0.341 |
| 1000x1000, k=3 | vertical | 4 | 87.222 | 0.334 |
| 1000x1000, k=3 | vertical | 8 | 134.138 | 0.217 |
| 1000x1000, k=3 | vertical | 16 | 192.779 | 0.151 |
| 10x10000, k=5 | sequential | 1 | 21.090 | 1.000 |
| 10x10000, k=5 | horizontal | 2 | 87.639 | 0.241 |
| 10x10000, k=5 | horizontal | 4 | 113.965 | 0.185 |
| 10x10000, k=5 | horizontal | 8 | 144.881 | 0.146 |
| 10x10000, k=5 | horizontal | 16 | 175.346 | 0.120 |
| 10x10000, k=5 | vertical | 2 | 88.489 | 0.238 |
| 10x10000, k=5 | vertical | 4 | 114.461 | 0.184 |
| 10x10000, k=5 | vertical | 8 | 101.549 | 0.208 |
| 10x10000, k=5 | vertical | 16 | 134.682 | 0.157 |
| 10000x10, k=5 | sequential | 1 | 13.504 | 1.000 |
| 10000x10, k=5 | horizontal | 2 | 70.084 | 0.193 |
| 10000x10, k=5 | horizontal | 4 | 83.284 | 0.162 |
| 10000x10, k=5 | horizontal | 8 | 138.822 | 0.097 |
| 10000x10, k=5 | horizontal | 16 | 218.578 | 0.062 |
| 10000x10, k=5 | vertical | 2 | 72.832 | 0.185 |
| 10000x10, k=5 | vertical | 4 | 85.181 | 0.159 |
| 10000x10, k=5 | vertical | 8 | 148.307 | 0.091 |
| 10000x10, k=5 | vertical | 16 | 144.537 | 0.093 |
| 10000x10000, k=5 | sequential | 1 | 6016.159 | 1.000 |
| 10000x10000, k=5 | horizontal | 2 | 4475.368 | 1.344 |
| 10000x10000, k=5 | horizontal | 4 | 2153.277 | 2.794 |
| 10000x10000, k=5 | horizontal | 8 | 1264.499 | 4.758 |
| 10000x10000, k=5 | horizontal | 16 | 815.514 | 7.377 |
| 10000x10000, k=5 | vertical | 2 | 6080.289 | 0.989 |
| 10000x10000, k=5 | vertical | 4 | 1786.811 | 3.367 |
| 10000x10000, k=5 | vertical | 8 | 1109.457 | 5.423 |
| 10000x10000, k=5 | vertical | 16 | 1133.712 | 5.307 |

### C++

| Matrix size | Allocation | Variant | Threads | Avg time (ms) | Speedup (Tseq/Tpar) |
|---|---|---|---|---|---|
| 10x10, k=3 | static | sequential | 1 | 0.028 | 1.000 |
| 10x10, k=3 | static | horizontal | 4 | 1.620 | 0.017 |
| 10x10, k=3 | static | vertical | 4 | 1.733 | 0.016 |
| 10x10, k=3 | dynamic | sequential | 1 | 0.002 | 1.000 |
| 10x10, k=3 | dynamic | horizontal | 4 | 1.666 | 0.001 |
| 10x10, k=3 | dynamic | vertical | 4 | 1.459 | 0.001 |
| 1000x1000, k=3 | static | sequential | 1 | 16.679 | 1.000 |
| 1000x1000, k=3 | static | horizontal | 2 | 14.174 | 1.177 |
| 1000x1000, k=3 | static | horizontal | 4 | 58.078 | 0.287 |
| 1000x1000, k=3 | static | horizontal | 8 | 13.270 | 1.257 |
| 1000x1000, k=3 | static | horizontal | 16 | 10.824 | 1.541 |
| 1000x1000, k=3 | static | vertical | 2 | 12.789 | 1.304 |
| 1000x1000, k=3 | static | vertical | 4 | 286.077 | 0.058 |
| 1000x1000, k=3 | static | vertical | 8 | 11.763 | 1.418 |
| 1000x1000, k=3 | static | vertical | 16 | 10.273 | 1.624 |
| 1000x1000, k=3 | dynamic | sequential | 1 | 14.520 | 1.000 |
| 1000x1000, k=3 | dynamic | horizontal | 2 | 14.770 | 0.983 |
| 1000x1000, k=3 | dynamic | horizontal | 4 | 6.983 | 2.079 |
| 1000x1000, k=3 | dynamic | horizontal | 8 | 6.758 | 2.149 |
| 1000x1000, k=3 | dynamic | horizontal | 16 | 6.871 | 2.113 |
| 1000x1000, k=3 | dynamic | vertical | 2 | 10.041 | 1.446 |
| 1000x1000, k=3 | dynamic | vertical | 4 | 6.859 | 2.117 |
| 1000x1000, k=3 | dynamic | vertical | 8 | 6.630 | 2.190 |
| 1000x1000, k=3 | dynamic | vertical | 16 | 6.808 | 2.133 |
| 10x10000, k=5 | static | sequential | 1 | 2.698 | 1.000 |
| 10x10000, k=5 | static | horizontal | 2 | 2.800 | 0.964 |
| 10x10000, k=5 | static | horizontal | 4 | 2.753 | 0.980 |
| 10x10000, k=5 | static | horizontal | 8 | 3.043 | 0.887 |
| 10x10000, k=5 | static | horizontal | 16 | 3.779 | 0.714 |
| 10x10000, k=5 | static | vertical | 2 | 2.785 | 0.969 |
| 10x10000, k=5 | static | vertical | 4 | 2.746 | 0.983 |
| 10x10000, k=5 | static | vertical | 8 | 3.151 | 0.856 |
| 10x10000, k=5 | static | vertical | 16 | 3.411 | 0.791 |
| 10x10000, k=5 | dynamic | sequential | 1 | 2.319 | 1.000 |
| 10x10000, k=5 | dynamic | horizontal | 2 | 2.582 | 0.898 |
| 10x10000, k=5 | dynamic | horizontal | 4 | 2.623 | 0.884 |
| 10x10000, k=5 | dynamic | horizontal | 8 | 2.728 | 0.850 |
| 10x10000, k=5 | dynamic | horizontal | 16 | 3.100 | 0.748 |
| 10x10000, k=5 | dynamic | vertical | 2 | 3.587 | 0.647 |
| 10x10000, k=5 | dynamic | vertical | 4 | 2.565 | 0.904 |
| 10x10000, k=5 | dynamic | vertical | 8 | 2.690 | 0.862 |
| 10x10000, k=5 | dynamic | vertical | 16 | 3.492 | 0.664 |
| 10000x10, k=5 | static | sequential | 1 | 23.253 | 1.000 |
| 10000x10, k=5 | static | horizontal | 2 | 15.326 | 1.517 |
| 10000x10, k=5 | static | horizontal | 4 | 12.064 | 1.927 |
| 10000x10, k=5 | static | horizontal | 8 | 11.333 | 2.052 |
| 10000x10, k=5 | static | horizontal | 16 | 11.412 | 2.038 |
| 10000x10, k=5 | static | vertical | 2 | 26.901 | 0.864 |
| 10000x10, k=5 | static | vertical | 4 | 26.002 | 0.894 |
| 10000x10, k=5 | static | vertical | 8 | 33.842 | 0.687 |
| 10000x10, k=5 | static | vertical | 16 | 34.477 | 0.674 |
| 10000x10, k=5 | dynamic | sequential | 1 | 2.430 | 1.000 |
| 10000x10, k=5 | dynamic | horizontal | 2 | 2.761 | 0.880 |
| 10000x10, k=5 | dynamic | horizontal | 4 | 2.630 | 0.924 |
| 10000x10, k=5 | dynamic | horizontal | 8 | 2.662 | 0.913 |
| 10000x10, k=5 | dynamic | horizontal | 16 | 3.792 | 0.641 |
| 10000x10, k=5 | dynamic | vertical | 2 | 2.883 | 0.843 |
| 10000x10, k=5 | dynamic | vertical | 4 | 2.982 | 0.815 |
| 10000x10, k=5 | dynamic | vertical | 8 | 2.685 | 0.905 |
| 10000x10, k=5 | dynamic | vertical | 16 | 3.205 | 0.758 |
| 10000x10000, k=5 | static | sequential | 1 | 3081.028 | 1.000 |
| 10000x10000, k=5 | static | horizontal | 2 | 1530.646 | 2.013 |
| 10000x10000, k=5 | static | horizontal | 4 | 816.735 | 3.772 |
| 10000x10000, k=5 | static | horizontal | 8 | 603.757 | 5.103 |
| 10000x10000, k=5 | static | horizontal | 16 | 474.462 | 6.494 |
| 10000x10000, k=5 | static | vertical | 2 | 1519.925 | 2.027 |
| 10000x10000, k=5 | static | vertical | 4 | 830.053 | 3.712 |
| 10000x10000, k=5 | static | vertical | 8 | 557.194 | 5.530 |
| 10000x10000, k=5 | static | vertical | 16 | 320.635 | 9.609 |
| 10000x10000, k=5 | dynamic | sequential | 1 | 2637.053 | 1.000 |
| 10000x10000, k=5 | dynamic | horizontal | 2 | 1286.935 | 2.049 |
| 10000x10000, k=5 | dynamic | horizontal | 4 | 792.194 | 3.329 |
| 10000x10000, k=5 | dynamic | horizontal | 8 | 590.657 | 4.465 |
| 10000x10000, k=5 | dynamic | horizontal | 16 | 345.194 | 7.639 |
| 10000x10000, k=5 | dynamic | vertical | 2 | 1410.064 | 1.870 |
| 10000x10000, k=5 | dynamic | vertical | 4 | 776.946 | 3.394 |
| 10000x10000, k=5 | dynamic | vertical | 8 | 511.045 | 5.160 |
| 10000x10000, k=5 | dynamic | vertical | 16 | 295.491 | 8.924 |


## Analysis

- **Small work (10x10, 10x10000, 10000x10):** the whole computation takes a few ms or less, so creating threads costs more than it saves. Sequential wins, and more threads only make it slower.
- **1000x1000:** C++ gets about 2x with 4 or more threads; beyond that the gain stops because the work per thread is too small. The two C++ static runs at p=4 (58 ms and 286 ms) are outliers, most likely caused by other load on the machine.
- **10000x10000:** the only case with enough work for every version to scale. Speedup keeps growing up to 16 threads: up to 9.6x in C++ and 7.4x in Java. It stays below the ideal 16x, mainly because the threads share the memory bandwidth and caches.
- **Horizontal vs vertical:** about the same for square matrices. For 10000x10000 with 16 threads, vertical is faster in C++ (295 vs 345 ms dynamic) but slower in Java (1134 vs 816 ms); Java vertical with 2 threads gave no speedup at all (0.989). For 10000x10 vertical is worse: each thread gets only 0-5 columns but still walks all 10000 rows, and threads write neighbouring values in the same rows (false sharing).
- **Java vs C++:** C++ is faster in every case; for 10000x10000 the C++ sequential version takes 2.6-3.1 s and the Java one 6.0 s. Java parallel times also include ~60 ms of fixed overhead (thread start plus the JVM setting up the first lambda), which is why Java parallel loses on every small case.
- **Static vs dynamic:** almost the same for 1000x1000; for 10000x10000 dynamic is faster (2637 vs 3081 ms sequential, 295 vs 321 ms best parallel). The static version scales better (9.6x vs 8.9x) because its sequential time is higher, but it is still slower in absolute time. For 10000x10 static is about 10x slower (23 ms vs 2.4 ms): each row of `f[MAX][MAX]` is 40 KB apart, so every row is on a different memory page, while the dynamic rows of 10 values sit next to each other.

**Best variant per size:**

| Matrix size | Best variant | Avg time (ms) |
|---|---|---|
| 10x10 | C++ dynamic, sequential | 0.002 |
| 1000x1000 | C++ dynamic, vertical, p=8 | 6.630 |
| 10x10000 | C++ dynamic, sequential | 2.319 |
| 10000x10 | C++ dynamic, sequential | 2.430 |
| 10000x10000 | C++ dynamic, vertical, p=16 |
