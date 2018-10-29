import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.*;

/**
 * @author maple 2018.09.21 下午4:03
 */
public class CyclicBarrierTest {

    private static CyclicBarrier barrier = new CyclicBarrier(3);

    private static class TThread implements Runnable {
        private String name;

        public TThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                System.out.println(name + " 第一步");
                int i = ThreadLocalRandom.current().nextInt(10);

                System.out.println(i);
                TimeUnit.SECONDS.sleep(i);
                barrier.await();
                System.out.println(name + " 第二步");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        TThread t1 = new TThread("t1");
        TThread t2 = new TThread("t2");
        TThread t3 = new TThread("t3");

        executorService.execute(t1);
        executorService.execute(t2);
        executorService.execute(t3);

        executorService.shutdown();

    }

}
