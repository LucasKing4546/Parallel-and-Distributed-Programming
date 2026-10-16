import java.io.*;
import java.util.Scanner;

public class parallel {

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();
        Scanner in = new Scanner(new File(args[0]));
        int n = in.nextInt();
        long[] a = new long[n], b = new long[n], c = new long[n];
        for (int i = 0; i < n; i++) a[i] = in.nextLong();
        for (int i = 0; i < n; i++) b[i] = in.nextLong();
        int p = Integer.parseInt(args[2]);

        Thread[] ts = new Thread[p];
        for (int k = 0; k < p; k++) {
            int from = (int) ((long) n * k / p);
            int to = (int) ((long) n * (k + 1) / p);
            ts[k] = new Thread(() -> { 
                for (int i = from; i < to; i++) 
                    c[i] = a[i] + b[i]; 
            });
            ts[k].start();
        }
        for (Thread t : ts) t.join();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[1])))) {
            for (long v : c) out.println(v);
        }
        long t1 = System.nanoTime();
        System.out.println((t1 - t0) / 1e6);
    }
}
