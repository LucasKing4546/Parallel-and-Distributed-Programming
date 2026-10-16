import java.io.*;
import java.util.Scanner;

public class sequential {

    public static void main(String[] args) throws IOException {
        long t0 = System.nanoTime();
        Scanner in = new Scanner(new File(args[0]));
        int n = in.nextInt();
        long[] a = new long[n], b = new long[n], c = new long[n];
        for (int i = 0; i < n; i++)
            a[i] = in.nextLong();
        for (int i = 0; i < n; i++)
            b[i] = in.nextLong();

        for (int i = 0; i < n; i++)
            c[i] = a[i] + b[i];

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[1])))) {
            for (long v : c) out.println(v);
        }
        long t1 = System.nanoTime();
        System.out.println((t1 - t0) / 1e6);
    }
}
