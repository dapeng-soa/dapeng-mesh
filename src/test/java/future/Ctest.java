package future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author maple 2018.09.16 下午9:50
 */
public class Ctest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ctest.class);

    public void test(String msg) {
        CompletableFuture<String> f = new CompletableFuture<>();

        f.whenComplete((result, ex) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("1 " + result);
            System.out.println("1==>  " + Thread.currentThread().getName());

        });

        f.whenComplete((result, ex) -> {

            System.out.println("2 " + result);
            System.out.println("2==>  " + Thread.currentThread().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("2==> 睡眠结束");
        });


        CompletableFuture<String> f2 = new CompletableFuture<>();
        f2.whenComplete((result, ex) -> {
            System.out.println("3 " + result);
            System.out.println("3==>  " + Thread.currentThread().getName());
        });

        new Thread(() -> {
            LOGGER.info("开始处理消息");
            f.complete("hello1");

            LOGGER.info("结束处理消息");
        }).start();

//        f.complete("hello1");
        f2.complete("hello1");
        System.out.println(Thread.currentThread().getName());
    }
}
