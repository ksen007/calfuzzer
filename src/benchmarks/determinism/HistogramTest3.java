package javato.benchmarks.determinism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static edu.berkeley.cs.detcheck.Determinism.openDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.closeDeterministicBlock;
import static edu.berkeley.cs.detcheck.Determinism.requireDeterministic;
import static edu.berkeley.cs.detcheck.Determinism.assertDeterministic;

public class HistogramTest3 {

    private static final int kNumSamples = 1000;
    private static final int kUnitSize = 100;
    private static final int kMaxValue = 10;
    private static final int kNumThreads = 2;

    private static class Worker implements Runnable {
        private final Queue<Collection<Integer>> wq;
        private final Map<Integer,Integer> res;

        public static int unitsProcessed = 0;

        public Worker(Queue<Collection<Integer>> workQueue,
                      Map<Integer,Integer> resultMap) {
            this.wq = workQueue;
            this.res = resultMap;
        }

        public void run() {
            Map<Integer,Integer> tmp = new TreeMap<Integer,Integer>();

            Collection<Integer> unit = null;
            while ((unit = wq.poll()) != null) {
                for (int i : unit) {
                    if (!tmp.containsKey(i)) {
                        tmp.put(i, 1);
                    } else {
                        tmp.put(i, 1 + tmp.get(i));
                    }
                }
                // Benign (?) data race.
                unitsProcessed += 1;
            }

            for (Map.Entry<Integer,Integer> e : tmp.entrySet()) {
                // Harmful data race.
                if (!res.containsKey(e.getKey())) {
                    res.put(e.getKey(), e.getValue());
                } else {
                    res.put(e.getKey(), e.getValue() + res.get(e.getKey()));
                }
            }
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage: HistogramTest1 <random-seed>\n");
            return;
        }

        // Initialize workqueue;
        Queue<Collection<Integer>> wq =
            new ConcurrentLinkedQueue<Collection<Integer>>();
        Random rnd = new Random(Long.parseLong(args[0]));
        for (int i = 0; i < kNumSamples / kUnitSize ; i++) {
            ArrayList<Integer> unit = new ArrayList<Integer>(100);
            for (int j = 0; j < kUnitSize; j++) {
                unit.add(rnd.nextInt(kMaxValue));
            }
            wq.add(unit);
        }

        // Compute a histogram in parallel.
        SortedMap<Integer,Integer> counts = new TreeMap<Integer,Integer>();
        try {
            openDeterministicBlock();
            requireDeterministic(new ArrayList<Collection<Integer>>(wq));
            Thread workers[] = new Thread[kNumThreads];
            for (int i = 0; i < kNumThreads; i++) {
                workers[i] = new Thread(new Worker(wq, counts));
                workers[i].start();
            }
            for (int i = 0; i < kNumThreads; i++) {
                // Really block until worker i is done.
                while (true) {
                    try {
                        workers[i].join();
                        break;
                    } catch (InterruptedException e) {
                    }
                }
            }
        } finally {
            assertDeterministic(counts);
            closeDeterministicBlock();
        }
        // Print the histogram.
        int sum = 0;
        for (Map.Entry<Integer,Integer> e : counts.entrySet()) {
            sum += e.getValue();
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        System.out.println("sum: " + sum);
    }

}
