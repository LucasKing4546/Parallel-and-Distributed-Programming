# Lab 0: vector addition, sequential vs parallel

Computes `c[i] = a[i] + b[i]` for two vectors read from `in.txt`, in Java and C++.

| File | What it does |
|---|---|
| `sequential.java`, `sequential.cpp` | One thread does the whole loop. |
| `parallel.java`, `parallel.cpp` | The index range is split into `p` equal chunks, one thread per chunk. Each thread writes only its own part of `c`, so no locks are needed. |
| `run.py` | Generates `in.txt`, compiles, runs everything and checks the results. |

## Instructions

The script:
1. Writes a random `in.txt` (`N = 100000`: the count `n`, then vector `a`, then vector `b`).
2. Compiles the four programs.
3. Runs each program 10 times (`RUNS`), with 4 threads for the parallel ones (`THREADS`).
4. After every execution, checks that the parallel output file is identical to the sequential one. It stops with an error if they differ.
5. Prints the average times and saves every individual time to `times.csv`.

Manual use: `java sequential in.txt out.txt`, `java parallel in.txt out.txt 4`, `./seq_cpp in.txt out.txt`, `./par_cpp in.txt out.txt 4`.
Each program prints its total execution time (reading, computing and writing) in ms.

## Results

Machine: Intel Core Ultra 9 185H, 16 cores / 22 threads. Input: `n = 100000`, 4 threads, 10 runs each. All 10 parallel outputs were identical to the sequential output in both languages.

| Language | Variant | Avg (ms) | Min | Max | Std dev |
|---|---|---|---|---|---|
| Java | sequential | 241.36 | 213.41 | 268.61 | 17.65 |
| Java | parallel | 288.81 | 237.81 | 318.09 | 21.79 |
| C++ | sequential | 32.50 | 29.65 | 36.06 | 2.02 |
| C++ | parallel | 33.93 | 32.14 | 39.77 | 2.24 |
