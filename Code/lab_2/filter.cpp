#include <algorithm>
#include <atomic>
#include <chrono>
#include <fstream>
#include <iostream>
#include <thread>
#include <vector>
using namespace std;

int n, m, p;
int c[3][3];         
int** f;              
atomic<bool>* flag;   

void filterRow(int* out, const int* above, const int* current, const int* below) {
    for (int j = 0; j < m; j++) {
        int sum = 0;
        for (int b = -1; b <= 1; b++) {
            int col = clamp(j + b, 0, m - 1);
            sum += above[col] * c[0][b + 1] + current[col] * c[1][b + 1] + below[col] * c[2][b + 1];
        }
        out[j] = sum;
    }
}

void work(int t) {
    int start = n * t / p;
    int end = n * (t + 1) / p;

    vector<int> above(f[max(start - 1, 0)], f[max(start - 1, 0)] + m);
    vector<int> afterBand(f[min(end, n - 1)], f[min(end, n - 1)] + m);

    flag[t] = true;

    if (t > 0)
        while (!flag[t - 1]) this_thread::yield();
    if (t < p - 1)
        while (!flag[t + 1]) this_thread::yield();

    vector<int> current(m);
    for (int i = start; i < end; i++) {
        copy(f[i], f[i] + m, current.begin());
        int* below = (i + 1 < end) ? f[i + 1] : afterBand.data();
        filterRow(f[i], above.data(), current.data(), below);
        swap(above, current);
    }
}

int main(int, char** argv) {
    ifstream fin("date.txt");
    fin >> n >> m;
    for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++) fin >> c[i][j];
    f = new int*[n];
    for (int i = 0; i < n; i++) {
        f[i] = new int[m];
        for (int j = 0; j < m; j++) fin >> f[i][j];
    }
    p = atoi(argv[2]);
    flag = new atomic<bool>[p];
    for (int t = 0; t < p; t++) flag[t] = false;

    auto t0 = chrono::high_resolution_clock::now();
    if (p == 1) {
        work(0);  
    } else {
        vector<thread> threads;
        for (int t = 0; t < p; t++) threads.emplace_back(work, t);
        for (auto& th : threads) th.join();
    }
    auto t1 = chrono::high_resolution_clock::now();

    ofstream out(argv[1]);
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) out << f[i][j] << (j + 1 < m ? ' ' : '\n');
    cout << chrono::duration<double, milli>(t1 - t0).count() << endl;
}
