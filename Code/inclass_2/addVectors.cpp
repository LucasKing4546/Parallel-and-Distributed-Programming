#include <iostream>
#include <cstdlib>
#include <chrono>
#include <thread>
#include <vector>

const int SIZE = 10000000;
double a[SIZE], b[SIZE], c[SIZE], d[SIZE];

void generateRandomDouble(int n, double* v) {
    for (int i = 0; i < n; i++) {
        v[i] = rand() % 100;
    }
}

void sumSerial(int n, double* a, double* b, double* c) {
    for (int i = 0; i < n; i++) {
        c[i] = a[i] + b[i];
    }
}

void sumParallel(int n, int p, double* a, double* b, double* c) {
    std::vector<std::thread> threads;
    int chunkSize = n / p;
    for (int i = 0; i < p; i++) {
        int start = i * chunkSize;
        int end = (i == p - 1) ? n : start + chunkSize;
        threads.emplace_back([start, end, a, b, c]() {
            for (int j = start; j < end; j++) {
                c[j] = a[j] + b[j];
            }
        });
    }
    for (auto& t : threads) {
        t.join();
    }
}

void sumCyclic(int n, int p, double* a, double* b, double* c) {
    std::vector<std::thread> threads;
    for (int i = 0; i < p; i++) {
        int index = i;
        threads.emplace_back([n, p, a, b, c, index]() {
            for (int j = index; j < n; j += p) {
                c[j] = a[j] + b[j];
            }
        });
    }
    for (auto& t : threads) {
        t.join();
    }
}

bool verifyResult(int n, double* a, double* b) {
    for (int i = 0; i < n; i++) {
        if(std::abs(c[i] - (a[i] + b[i])) > 1e-6) {
            return false;
        }
    }
    return true;
}

int main(int argc, char* argv[]) {
    int n = std::atoi(argv[1]);
    int p = std::atoi(argv[2]);

    generateRandomDouble(n, a);
    generateRandomDouble(n, b);

    auto startSerial = std::chrono::high_resolution_clock::now();
    sumSerial(n, a, b, c);
    auto endSerial= std::chrono::high_resolution_clock::now();
    std::chrono::duration<double, std::milli> duration = endSerial - startSerial;
    std::cout << duration.count() << std::endl;
    for (int i = 0; i < n; i++) {
            std::cout << c[i] << " ";
    }
    std::cout << std::endl;
    auto startParallel = std::chrono::high_resolution_clock::now();
    sumParallel(n, p, a, b, d);
    auto endParallel= std::chrono::high_resolution_clock::now();
    std::chrono::duration<double, std::milli> durationParallel = endParallel - startParallel;
    std::cout << durationParallel.count() << std::endl;
    for (int i = 0; i < n; i++) {
        std::cout << d[i] << " ";
    }
    std::cout << std::endl;
    std::cout << (verifyResult(n, c, d) ? "Success" : "Failure") << std::endl;

    auto startCyclic = std::chrono::high_resolution_clock::now();
    sumCyclic(n, p, a, b, d);
    auto endCyclic= std::chrono::high_resolution_clock::now();
    std::chrono::duration<double, std::milli> durationCyclic = endCyclic - startCyclic;
    std::cout << durationCyclic.count() << std::endl;
    for (int i = 0; i < n; i++) {
        std::cout << d[i] << " ";
    }
    std::cout << std::endl;
    std::cout << (verifyResult(n, c, d) ? "Success" : "Failure") << std::endl;
    return 0;
}