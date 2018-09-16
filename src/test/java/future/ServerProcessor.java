package future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author maple 2018.09.16 下午9:34
 */
public class ServerProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessor.class);

    private ExecutorService service = Executors.newFixedThreadPool(8, new MyThreadFactory());


    public void postAsync(CompletableFuture<String> future, String msg) {
        service.execute(() -> {
            LOGGER.info("服务端开始处理消息 {} ", msg);
            future.complete(msg + "服务端" + msg);
        });
    }
}
