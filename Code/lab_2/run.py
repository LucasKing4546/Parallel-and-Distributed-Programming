import csv
import filecmp
import random
import subprocess
import sys

RUNS = 10
TESTS = [
    (10, 10, [2]),
    (1000, 1000, [2, 4, 8, 16]),
    (10000, 10000, [2, 4, 8, 16]),
]
PROGRAMS = [
    ("Java", ["java", "filter"]),
    ("C++", ["./filter"]),
]


def write_input(n, m):
    with open("date.txt", "w") as f:
        f.write(f"{n} {m}\n")
        for _ in range(3):
            f.write(" ".join(str(random.randint(-5, 5)) for _ in range(3)) + "\n")
        for _ in range(n):
            f.write(" ".join(map(str, random.choices(range(256), k=m))) + "\n")


def check_naive():
    # straightforward out-of-place convolution, to validate the in-place sequential output
    with open("date.txt") as f:
        n, m, *rest = map(int, f.read().split())
    c = [rest[i * 3:i * 3 + 3] for i in range(3)]
    a = [rest[9 + i * m:9 + (i + 1) * m] for i in range(n)]
    cl = lambda x, hi: min(max(x, 0), hi - 1)
    want = [[sum(a[cl(i + di, n)][cl(j + dj, m)] * c[di + 1][dj + 1] for di in (-1, 0, 1) for dj in (-1, 0, 1))
             for j in range(m)] for i in range(n)]
    with open("output_seq.txt") as f:
        got = [list(map(int, line.split())) for line in f]
    assert got == want, "sequential output differs from the naive convolution!"


def run(cmd):
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        raise Exception(f"{' '.join(cmd)} failed:\n{result.stderr}")
    return float(result.stdout)


def test(n, m, threads, rows):
    write_input(n, m)
    size = f"{n}x{m}"
    for name, cmd in PROGRAMS:
        seq_times = [run(cmd + ["output_seq.txt", "1"]) for _ in range(RUNS)]
        if n * m <= 10000:
            check_naive()
        seq_avg = sum(seq_times) / RUNS
        for i, t in enumerate(seq_times):
            rows.append([size, name, 1, i + 1, t])
        print(f"{size} | {name} | sequential: {seq_avg:.4f} ms", flush=True)

        for p in threads:
            par_times = []
            for i in range(RUNS):
                t = run(cmd + ["output.txt", str(p)])
                if not filecmp.cmp("output_seq.txt", "output.txt", shallow=False):
                    raise Exception(f"{size} {name} p={p}: run {i + 1} gave a different output!")
                par_times.append(t)
                rows.append([size, name, p, i + 1, t])
            par_avg = sum(par_times) / RUNS
            print(f"{size} | {name} | p={p}: {par_avg:.4f} ms, speedup {seq_avg / par_avg:.4f}", flush=True)


subprocess.run(["javac", "--release", "8", "filter.java"], check=True)
subprocess.run(["g++", "-O2", "-std=c++17", "-pthread", "-static", "-o", "filter", "filter.cpp"], check=True)

tests = [TESTS[int(sys.argv[1])]] if len(sys.argv) > 1 else TESTS
# keep the rows of the sizes not run now, so times.csv always holds every test case
sizes = [f"{n}x{m}" for n, m, _ in TESTS]
try:
    with open("times.csv") as f:
        rows = [r for r in list(csv.reader(f))[1:] if r[0] not in {f"{n}x{m}" for n, m, _ in tests}]
except FileNotFoundError:
    rows = []
for n, m, threads in tests:
    test(n, m, threads, rows)
rows.sort(key=lambda r: sizes.index(r[0]))  # stable: keeps program/thread/run order within a size
with open("times.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["size", "program", "threads", "run", "time_ms"])
    writer.writerows(rows)
