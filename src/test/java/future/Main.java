package future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author maple 2018.09.16 下午10:25
 */
public class Main {
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Ctest2 test = new Ctest2();


        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                test.test("消息：" + atomicInteger.incrementAndGet());
            });
        }

    }

}
