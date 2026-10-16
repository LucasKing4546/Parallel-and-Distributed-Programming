#include <chrono>
#include <fstream>
#include <iostream>
#include <vector>
using namespace std;


int main(int, char** argv) {
    auto t0 = chrono::high_resolution_clock::now();
    ifstream in(argv[1]);
    int n;
    in >> n;
    vector<long long> a(n);
    vector<long long> b(n);
    vector<long long> c(n);
    for (auto& x : a) in >> x;
    for (auto& x : b) in >> x;

    for (int i = 0; i < n; i++) {
        c[i] = a[i] + b[i];
    }
    ofstream out(argv[2]);
    for (long long v : c) out << v << "\n";
    auto t1 = chrono::high_resolution_clock::now();
    cout << chrono::duration<double, milli>(t1 - t0).count() << endl;
}
