import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * desc: PathMaterTest
 *
 * @author hz.lei
 * @since 2018年08月23日 下午1:45
 */
public class PathMaterTest {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
                String format = now.format(formatter);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread:" + Thread.currentThread().getName() + ", format:" + format);
            });
        }


    }
}
