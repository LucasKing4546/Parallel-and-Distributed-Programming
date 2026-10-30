#include <algorithm>
#include <chrono>
#include <fstream>
#include <iostream>
#include <string>
#include <thread>
#include <vector>
using namespace std;

int **f, **v, c[5][5];
int n, m, k;

// computes v for rows [r0, r1) and columns [c0, c1)
void convolve(int r0, int r1, int c0, int c1) {
    for (int i = r0; i < r1; i++)
        for (int j = c0; j < c1; j++) {
            int s = 0;
            for (int a = 0; a < k; a++)
                for (int b = 0; b < k; b++)
                    s += f[clamp(i + a - k / 2, 0, n - 1)][clamp(j + b - k / 2, 0, m - 1)] * c[a][b];
            v[i][j] = s;
        }
}

int main(int, char** argv) {
    ifstream fin("MatrixF.txt"), cfin("ConvolutionM.txt");
    fin >> n >> m;
    cfin >> k;
    f = new int*[n];
    v = new int*[n];
    for (int i = 0; i < n; i++) {
        f[i] = new int[m];
        v[i] = new int[m];
    }
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) fin >> f[i][j];
    for (int i = 0; i < k; i++)
        for (int j = 0; j < k; j++) cfin >> c[i][j];
    int p = atoi(argv[2]);
    bool vertical = string(argv[3]) == "v";

    auto t0 = chrono::high_resolution_clock::now();
    vector<thread> ts;
    int len = vertical ? m : n;
    for (int t = 0; t < p; t++) {
        int from = (long long)len * t / p;
        int to = (long long)len * (t + 1) / p;
        if (vertical) ts.emplace_back(convolve, 0, n, from, to);
        else ts.emplace_back(convolve, from, to, 0, m);
    }
    for (auto& t : ts) t.join();
    auto t1 = chrono::high_resolution_clock::now();

    ofstream out(argv[1]);
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) out << v[i][j] << (j + 1 < m ? ' ' : '\n');
    cout << chrono::duration<double, milli>(t1 - t0).count() << endl;
}
