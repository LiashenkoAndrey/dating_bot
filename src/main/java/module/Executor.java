package module;

import lombok.NoArgsConstructor;
import module.util.telegramUtils.UpdateReceiver;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.*;

@DIBean
@NoArgsConstructor
public class Executor {

    public ExecutorService executorService = Executors.newFixedThreadPool(5);

    @InjectBean("UpdateReceiver")
    private UpdateReceiver updateReceiver;

    public void processUpdate(Update update) {
        executorService.execute(() -> updateReceiver.processUpdate(update));
    }

}
