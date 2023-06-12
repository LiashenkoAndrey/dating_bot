package module;

import lombok.NoArgsConstructor;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@DIBean
@NoArgsConstructor
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @InjectBean
    private Executor executor;

    @InjectBean
    private BeanManager manager;

    @Override
    public String getBotToken() {
        return "5740194564:AAH5YErg0L3eBtF8O5neRCULhO6_RPMpRWs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        manager.loadWithName(update, "Update");
        executor.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return "Bluadki";
    }
}
