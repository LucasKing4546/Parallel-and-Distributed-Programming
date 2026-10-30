import csv
import filecmp
import random
import subprocess
import sys

RUNS = 10
TESTS = [ 
    (10, 10, 3, [4]),
    (1000, 1000, 3, [2, 4, 8, 16]),
    (10, 10000, 5, [2, 4, 8, 16]),
    (10000, 10, 5, [2, 4, 8, 16]),
    (10000, 10000, 5, [2, 4, 8, 16]),
]
PROGRAMS = [  
    ("Java", ["java", "sequential"], ["java", "parallel"]),
    ("C++ static", ["./seq_static"], ["./par_static"]),
    ("C++ dynamic", ["./seq_dynamic"], ["./par_dynamic"]),
]
MODES = {"h": "horizontal", "v": "vertical"}


def random_matrix(path, rows, cols, header, lo, hi):
    with open(path, "w") as f:
        f.write(header + "\n")
        for _ in range(rows):
            f.write(" ".join(map(str, random.choices(range(lo, hi + 1), k=cols))) + "\n")


def run(cmd):
    result = subprocess.run(cmd, capture_output=True, text=True, check=True)
    return float(result.stdout)


def test(n, m, k, threads, csv_writer):
    random_matrix("MatrixF.txt", n, m, f"{n} {m}", 0, 255)
    random_matrix("ConvolutionM.txt", k, k, f"{k}", -5, 5)
    size = f"{n}x{m}, k={k}"

    for name, seq_cmd, par_cmd in PROGRAMS:
        seq_times = [run(seq_cmd + ["output_seq.txt"]) for _ in range(RUNS)]
        seq_avg = sum(seq_times) / RUNS
        for i, t in enumerate(seq_times):
            csv_writer.writerow([size, name, "sequential", 1, i + 1, t])
        print(f"{size} | {name} | sequential: {seq_avg:.4f} ms", flush=True)

        for mode, mode_name in MODES.items():
            for p in threads:
                par_times = []
                for i in range(RUNS):
                    t = run(par_cmd + ["output.txt", str(p), mode])
                    if not filecmp.cmp("output_seq.txt", "output.txt", shallow=False):
                        raise Exception(f"{size} {name} {mode_name} p={p}: run {i + 1} gave a different output!")
                    par_times.append(t)
                    csv_writer.writerow([size, name, mode_name, p, i + 1, t])
                par_avg = sum(par_times) / RUNS
                print(f"{size} | {name} | {mode_name} p={p}: {par_avg:.4f} ms, speedup {seq_avg / par_avg:.4f}", flush=True)


subprocess.run(["javac", "--release", "8", "sequential.java", "parallel.java"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "seq_static", "sequential_static.cpp"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "par_static", "parallel_static.cpp"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "seq_dynamic", "sequential_dynamic.cpp"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "par_dynamic", "parallel_dynamic.cpp"], check=True)

tests = [TESTS[int(sys.argv[1])]] if len(sys.argv) > 1 else TESTS
with open("times.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["size", "program", "variant", "threads", "run", "time_ms"])
    for n, m, k, threads in tests:
        test(n, m, k, threads, writer)
