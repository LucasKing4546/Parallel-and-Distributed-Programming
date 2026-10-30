import java.io.*;

public class parallel {
    static int n, m, k;
    static int[][] f, v, c;

    static int next(StreamTokenizer in) throws IOException {
        in.nextToken();
        return (int) in.nval;
    }

    // computes v for rows [r0, r1) and columns [c0, c1)
    static void convolve(int r0, int r1, int c0, int c1) {
        for (int i = r0; i < r1; i++)
            for (int j = c0; j < c1; j++) {
                int s = 0;
                for (int a = 0; a < k; a++)
                    for (int b = 0; b < k; b++)
                        s += f[Math.min(Math.max(i + a - k / 2, 0), n - 1)][Math.min(Math.max(j + b - k / 2, 0), m - 1)] * c[a][b];
                v[i][j] = s;
            }
    }

    public static void main(String[] args) throws Exception {
        StreamTokenizer fin = new StreamTokenizer(new BufferedReader(new FileReader("MatrixF.txt")));
        StreamTokenizer cfin = new StreamTokenizer(new BufferedReader(new FileReader("ConvolutionM.txt")));
        n = next(fin);
        m = next(fin);
        k = next(cfin);
        f = new int[n][m];
        v = new int[n][m];
        c = new int[k][k];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) f[i][j] = next(fin);
        for (int i = 0; i < k; i++)
            for (int j = 0; j < k; j++) c[i][j] = next(cfin);
        int p = Integer.parseInt(args[1]);
        boolean vertical = args[2].equals("v");

        long t0 = System.nanoTime();
        Thread[] ts = new Thread[p];
        int len = vertical ? m : n;
        for (int t = 0; t < p; t++) {
            int from = (int) ((long) len * t / p);
            int to = (int) ((long) len * (t + 1) / p);
            ts[t] = vertical
                    ? new Thread(() -> convolve(0, n, from, to))
                    : new Thread(() -> convolve(from, to, 0, m));
            ts[t].start();
        }
        for (Thread t : ts) t.join();
        long t1 = System.nanoTime();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))) {
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++) out.print(v[i][j] + (j + 1 < m ? " " : "\n"));
        }
        System.out.println((t1 - t0) / 1e6);
    }
}
