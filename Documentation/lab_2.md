# Lab 2: in-place convolution with a 3x3 kernel

Same filter as Lab 1 (3x3 kernel, nearest border value outside the matrix), but the result is written **back into `f`**. No second or temporary matrix is allocated, only a few row-sized vectors per thread.

| File | What it does |
|---|---|
| `filter.java`, `filter.cpp` | `p = 1`: sequential, on the main thread. `p > 1`: `p` threads, each gets a band of rows (bands differ by at most one row). |
| `run.py` | Generates `date.txt`, compiles, runs everything 10 times and checks the results. |

C++ uses dynamic allocation: one `new int[m]` per row.

## How it works

**In place, one thread.** Row `i` needs the *original* rows `i-1`, `i`, `i+1`, but row `i-1` has already been overwritten. So before overwriting row `i`, its original values are saved in a vector `current`; after the row is done, `current` becomes `above` for row `i+1` (the two vectors are swapped, nothing is copied). Row `i+1` is still original, so it is read directly from `f`.

**Parallel (variant 1f, atomic flags).** Thread `t` owns rows `[start, end)`. Its first and last rows need row `start-1` and row `end`, which belong to the neighbor threads and will be overwritten by them. Each thread:

1. Copies row `start-1` into `above` and row `end` into `afterBand` (at the matrix border, row 0 / row n-1).
2. Sets `flag[t] = true` ("my copies are done").
3. Waits until `flag[t-1]` and `flag[t+1]` are true, so the neighbors have copied *its* boundary rows.
4. Filters its rows in place, as in the sequential version, using `afterBand` as the row below its last row.

A thread only waits for its two neighbors, not for all threads as with a barrier (variant 1b). The flags are `atomic<bool>` in C++ and `AtomicBoolean` in Java.

## Instructions

`python run.py` runs all test cases, `python run.py <i>` only test case `i` (0-2). `times.csv` always holds every individual time: a run replaces only the rows of the sizes it ran and keeps the others. For each test case the script:
1. Writes a random `date.txt`: first line `n m`, then the 3x3 kernel (values -5..5), then the matrix (values 0..255).
2. Runs the sequential version (`p = 1`) 10 times, then the parallel version 10 times for every thread count.
3. For the 10x10 case, checks the sequential output against a plain out-of-place convolution in Python.
4. After every parallel run, checks that `output.txt` is identical to `output_seq.txt` and stops with an error if not.
5. Prints the averages and saves every individual time to `times.csv` (written at the end, so a failed run leaves the old file intact).

Manual use, from `Code/lab_2`:
```
javac filter.java
g++ -O2 -std=c++17 -pthread -static -o filter filter.cpp
java filter output.txt 4      # 4 threads
./filter output.txt 1         # sequential
```
`-static` is needed on this machine: without it the exe loads an older `libstdc++-6.dll` from Git's folder and crashes on start.

Only the computation is timed (thread creation and join included), not reading or writing files.

## Results

Machine: Intel Core Ultra 9 185H, 16 cores / 22 threads. Average of 10 runs, in ms. All parallel outputs were identical to the sequential output.

| Matrix size | Language | Threads | Avg time (ms) | Speedup |
|---|---|---|---|---|
| 10x10 | Java | sequential | 0.091 | 1.000 |
| 10x10 | Java | 2 | 61.485 | 0.001 |
| 10x10 | C++ | sequential | 0.002 | 1.000 |
| 10x10 | C++ | 2 | 1.374 | 0.001 |
| 1000x1000 | Java | sequential | 26.449 | 1.000 |
| 1000x1000 | Java | 2 | 81.111 | 0.326 |
| 1000x1000 | Java | 4 | 99.023 | 0.267 |
| 1000x1000 | Java | 8 | 123.751 | 0.214 |
| 1000x1000 | Java | 16 | 103.240 | 0.256 |
| 1000x1000 | C++ | sequential | 4.877 | 1.000 |
| 1000x1000 | C++ | 2 | 4.096 | 1.191 |
| 1000x1000 | C++ | 4 | 4.344 | 1.123 |
| 1000x1000 | C++ | 8 | 5.812 | 0.839 |
| 1000x1000 | C++ | 16 | 5.803 | 0.840 |
| 10000x10000 | Java | sequential | 534.987 | 1.000 |
| 10000x10000 | Java | 2 | 319.049 | 1.677 |
| 10000x10000 | Java | 4 | 240.165 | 2.228 |
| 10000x10000 | Java | 8 | 227.890 | 2.348 |
| 10000x10000 | Java | 16 | 185.336 | 2.887 |
| 10000x10000 | C++ | sequential | 461.342 | 1.000 |
| 10000x10000 | C++ | 2 | 181.900 | 2.536 |
| 10000x10000 | C++ | 4 | 119.752 | 3.852 |
| 10000x10000 | C++ | 8 | 73.837 | 6.248 |
| 10000x10000 | C++ | 16 | 67.035 | 6.882 |

Every individual time is in `Code/lab_2/times.csv`.

## Space complexity

| | Lab 1 | Lab 2 |
|---|---|---|
| Matrix | `f` + result `v`: 2·n·m | only `f`: n·m |
| Extra memory, sequential | n·m (the matrix `v`) | 2 vectors of m (`above`, `current`) = O(m) |
| Extra memory, p threads | n·m | 3 vectors of m per thread (`above`, `current`, `afterBand`) + p flags = O(p·m) |

For 10000x10000 that is about 400 MB saved (no second matrix of 10^8 ints), while the extra memory is 16 threads · 3 · 10000 ints ≈ 1.9 MB.

**Compared to the other variants:** 1b uses the same memory as 1f (only the synchronization differs). 2b/2f save the computed boundary rows instead of the copied neighbor rows, which is also O(p·m).

## Analysis

- **10x10:** the work takes microseconds, so starting threads costs far more than it saves. Sequential wins.
- **1000x1000:** the whole filter takes ~5 ms in C++, so thread start-up eats most of the gain: speedup stays around 1x (best 1.19x at 2 threads, below 1 at 8 and 16). Java is slower in parallel, because starting threads and the first lambda costs ~60-100 ms while the sequential work is only ~26 ms.
- **10000x10000:** here parallelism pays off. C++ reaches 6.9x with 16 threads, Java 2.9x. C++ scales well up to 8 threads (6.2x), then flattens: 16 threads only gain a little more, since the 16 cores include slower efficiency cores and memory bandwidth becomes the limit. The C++ sequential average (461 ms) is pushed up by three slow runs (546, 556, 1235 ms); the typical run is ~320 ms, which would give a speedup of ~4.8x at 16 threads.
- **Java vs C++:** C++ is about 5x faster sequentially for 1000x1000 (4.9 vs 26.4 ms). For 10000x10000 the gap shrinks (~320 vs ~490 ms for a typical run), because the JIT has warmed up and both are limited by memory. In parallel C++ is ~2.8x faster (67 vs 185 ms at 16 threads).
- **Compared to Lab 1:** the in-place version is faster sequentially (C++ 1000x1000: 4.9 ms vs 14.5 ms in Lab 1 dynamic). Only one matrix goes through the cache, and the 3 rows are read from small vectors instead of clamping both indices for each of the 9 kernel values.
