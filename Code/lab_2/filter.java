import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class filter {
    static int n, m, p;
    static int[][] c = new int[3][3]; 
    static int[][] f;                
    static AtomicBoolean[] flag;   

    static int next(StreamTokenizer in) throws IOException {
        in.nextToken();
        return (int) in.nval;
    }

    static void filterRow(int[] out, int[] above, int[] current, int[] below) {
        for (int j = 0; j < m; j++) {
            int sum = 0;
            for (int b = -1; b <= 1; b++) {
                int col = Math.min(Math.max(j + b, 0), m - 1);
                sum += above[col] * c[0][b + 1] + current[col] * c[1][b + 1] + below[col] * c[2][b + 1];
            }
            out[j] = sum;
        }
    }

    static void work(int t) {
        int start = n * t / p;
        int end = n * (t + 1) / p;

        int[] above = f[Math.max(start - 1, 0)].clone();
        int[] afterBand = f[Math.min(end, n - 1)].clone();

        flag[t].set(true);

        if (t > 0)
            while (!flag[t - 1].get()) Thread.yield();
        if (t < p - 1)
            while (!flag[t + 1].get()) Thread.yield();

        int[] current = new int[m];
        for (int i = start; i < end; i++) {
            System.arraycopy(f[i], 0, current, 0, m);
            int[] below = (i + 1 < end) ? f[i + 1] : afterBand;
            filterRow(f[i], above, current, below);
            int[] tmp = above; 
            above = current; 
            current = tmp;
        }
    }

    public static void main(String[] args) throws Exception {
        StreamTokenizer fin = new StreamTokenizer(new BufferedReader(new FileReader("date.txt")));
        n = next(fin);
        m = next(fin);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) c[i][j] = next(fin);
        f = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) f[i][j] = next(fin);
        p = Integer.parseInt(args[1]);
        flag = new AtomicBoolean[p];
        for (int t = 0; t < p; t++) flag[t] = new AtomicBoolean(false);

        long t0 = System.nanoTime();
        if (p == 1) {
            work(0); // sequential: everything on the main thread
        } else {
            Thread[] threads = new Thread[p];
            for (int t = 0; t < p; t++) {
                int id = t;
                threads[t] = new Thread(() -> work(id));
                threads[t].start();
            }
            for (Thread th : threads) th.join();
        }
        long t1 = System.nanoTime();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))) {
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++) out.print(f[i][j] + (j + 1 < m ? " " : "\n"));
        }
        System.out.println((t1 - t0) / 1e6);
    }
}
