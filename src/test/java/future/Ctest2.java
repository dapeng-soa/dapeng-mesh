package future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author maple 2018.09.16 下午9:50
 */
public class Ctest2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ctest2.class);

    private ServerProcessor serverProcessor = new ServerProcessor();

    public void test(String msg) {
        LOGGER.info("客户端: {} 处理消息: {}", msg, msg);
        CompletableFuture<String> f = new CompletableFuture<>();

        serverProcessor.postAsync(f, msg);

        f.whenComplete((result, ex) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            LOGGER.info("{} CompletableFuture whenComplete ===> {}", msg, result);
        });
    }


}
