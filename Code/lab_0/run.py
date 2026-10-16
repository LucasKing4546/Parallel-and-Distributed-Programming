import csv
import os
import random
import subprocess

RUNS = 10
THREADS = 4
N = 100000

with open("in.txt", "w") as f:
    f.write(f"{N}\n")
    for _ in range(2):
        f.write(" ".join(str(random.randint(0, 1000)) for _ in range(N)) + "\n")


def run(cmd):
    result = subprocess.run(cmd, capture_output=True, text=True, check=True)
    return float(result.stdout)


def same_content(file1, file2):
    with open(file1) as f1, open(file2) as f2:
        return f1.read() == f2.read()


def test(name, seq_cmd, par_cmd, csv_writer):
    seq_times = []
    par_times = []
    for i in range(RUNS):
        seq_time = run(seq_cmd + ["in.txt", "out_seq.txt"])
        par_time = run(par_cmd + ["in.txt", "out_par.txt", str(THREADS)])

        if not same_content("out_seq.txt", "out_par.txt"):
            raise Exception(f"{name}: run {i + 1} gave a different output!")

        seq_times.append(seq_time)
        par_times.append(par_time)
        csv_writer.writerow([name, "sequential", i + 1, seq_time])
        csv_writer.writerow([name, "parallel", i + 1, par_time])

    print(f"{name}: seq={sum(seq_times) / RUNS:.2f} ms  par={sum(par_times) / RUNS:.2f} ms  (all {RUNS} outputs identical)")


subprocess.run(["javac", "--release", "8", "sequential.java", "parallel.java"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "seq_cpp", "sequential.cpp"], check=True)
subprocess.run(["g++", "-O2", "-pthread", "-o", "par_cpp", "parallel.cpp"], check=True)

with open("times.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["language", "variant", "run", "time_ms"])
    test("Java", ["java", "sequential"], ["java", "parallel"], writer)
    test("C++", ["./seq_cpp"], ["./par_cpp"], writer)
