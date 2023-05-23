package module;

import lombok.NoArgsConstructor;
import module.telegram_utils.MethodExecutor;
import module.telegram_utils.UpdateReceiver;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Method;
import java.util.Map;

@DIBean
@NoArgsConstructor
public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @InjectBean
    public UpdateReceiver updateReceiver;

    @InjectBean("callbacksPool")
    public Map<String, Method> callbacksPool;

    @InjectBean("BeanManager")
    public BeanManager manager;

    @InjectBean
    public MethodExecutor executor;

    @Override
    public String getBotToken() {
        return "5740194564:AAH5YErg0L3eBtF8O5neRCULhO6_RPMpRWs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        manager.loadAll(update);
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Method method = callbacksPool.get(callbackQuery.getData());
            if (method != null) executor.invokeCallBack(method, callbackQuery);
        }

        updateReceiver.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return "Bluadki";
    }
}
