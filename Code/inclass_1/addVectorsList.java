package Code.inclass_1;

import java.util.ArrayList;
import java.util.Collections;

public class addVectorsList {
    public static ArrayList<Double> generate(int n){
        ArrayList<Double> list = new ArrayList<>(n);
        for(int i = 0; i < n; i++){
            list.add(Math.random());
        }
        return list;
    }

    // c is pre-filled to size n so threads only call set() on their own indices;
    // concurrent add() would resize the backing array and is not thread-safe.
    public static ArrayList<Double> zeros(int n){
        return new ArrayList<>(Collections.nCopies(n, 0.0));
    }

    public static ArrayList<Double> vectorSumSerial(int n, ArrayList<Double> a, ArrayList<Double> b){
        ArrayList<Double> c = zeros(n);
        for (int i=0; i<n; i++){
            c.set(i, Math.sqrt(a.get(i)*a.get(i) + b.get(i)*b.get(i)));
        }
        return c;
    }

    public static ArrayList<Double> vectorSumParallel(int n, ArrayList<Double> a, ArrayList<Double> b, int p){
        int chunkSize = (int) Math.ceil((double)n/p);
        ArrayList<Double> c = zeros(n);
        Thread[] threads = new Thread[p];
        for (int i=0; i<p; i++){
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, n);
            threads[i] = new Thread(() -> {
                for (int j=start; j<end; j++){
                    c.set(j, Math.sqrt(a.get(j)*a.get(j) + b.get(j)*b.get(j)));
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

    public static ArrayList<Double> vectorSumParallelCycle(int n, ArrayList<Double> a, ArrayList<Double> b, int p){
        ArrayList<Double> c = zeros(n);
        Thread[] threads = new Thread[p];
        for (int j=0; j<p; j++){
            int index = j;
            threads[index] = new Thread(() -> {
                for (int i=index; i<n; i+=p){
                    c.set(i, Math.sqrt(a.get(i)*a.get(i) + b.get(i)*b.get(i)));
                }
            });
        }
        for (Thread thread : threads){
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

    public static boolean correct(ArrayList<Double> a, ArrayList<Double> b, ArrayList<Double> c){
        for (int i=0; i<a.size(); i++){
            if (Math.abs(Math.sqrt(a.get(i)*a.get(i) + b.get(i)*b.get(i)) - c.get(i)) > 1e-5){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int tp = Integer.parseInt(args[1]);
        int p = Integer.parseInt(args[2]);

        ArrayList<Double> a = generate(n);
        ArrayList<Double> b = generate(n);
        ArrayList<Double> c = null;
        double start = System.nanoTime();
        if (tp==0) c = vectorSumSerial(n, a, b);
        else if (tp==1) c = vectorSumParallel(n, a, b, p);
        else if (tp==2) c = vectorSumParallelCycle(n, a, b, p);

        double finish = System.nanoTime();
        System.out.println((finish-start) / 1e6);
        System.out.println(correct(a, b, c));
    }
}
