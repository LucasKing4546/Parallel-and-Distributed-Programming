#include <algorithm>
#include <chrono>
#include <fstream>
#include <iostream>
using namespace std;

const int MAX = 10000;
int f[MAX][MAX], v[MAX][MAX], c[5][5];

int main(int, char** argv) {
    ifstream fin("MatrixF.txt"), cfin("ConvolutionM.txt");
    int n, m, k;
    fin >> n >> m;
    cfin >> k;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) fin >> f[i][j];
    for (int i = 0; i < k; i++)
        for (int j = 0; j < k; j++) cfin >> c[i][j];

    auto t0 = chrono::high_resolution_clock::now();
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) {
            int s = 0;
            for (int a = 0; a < k; a++)
                for (int b = 0; b < k; b++)
                    s += f[clamp(i + a - k / 2, 0, n - 1)][clamp(j + b - k / 2, 0, m - 1)] * c[a][b];
            v[i][j] = s;
        }
    auto t1 = chrono::high_resolution_clock::now();

    ofstream out(argv[1]);
    for (int i = 0; i < n; i++)
        for (int j = 0; j < m; j++) out << v[i][j] << (j + 1 < m ? ' ' : '\n');
    cout << chrono::duration<double, milli>(t1 - t0).count() << endl;
}
