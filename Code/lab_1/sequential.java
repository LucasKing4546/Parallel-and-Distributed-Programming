import java.io.*;

public class sequential {

    static int next(StreamTokenizer in) throws IOException {
        in.nextToken();
        return (int) in.nval;
    }

    public static void main(String[] args) throws Exception {
        StreamTokenizer fin = new StreamTokenizer(new BufferedReader(new FileReader("MatrixF.txt")));
        StreamTokenizer cfin = new StreamTokenizer(new BufferedReader(new FileReader("ConvolutionM.txt")));
        int n = next(fin), m = next(fin), k = next(cfin);
        int[][] f = new int[n][m], v = new int[n][m], c = new int[k][k];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) f[i][j] = next(fin);
        for (int i = 0; i < k; i++)
            for (int j = 0; j < k; j++) c[i][j] = next(cfin);

        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                int s = 0;
                for (int a = 0; a < k; a++)
                    for (int b = 0; b < k; b++)
                        s += f[Math.min(Math.max(i + a - k / 2, 0), n - 1)][Math.min(Math.max(j + b - k / 2, 0), m - 1)] * c[a][b];
                v[i][j] = s;
            }
        long t1 = System.nanoTime();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[0])))) {
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++) out.print(v[i][j] + (j + 1 < m ? " " : "\n"));
        }
        System.out.println((t1 - t0) / 1e6);
    }
}
