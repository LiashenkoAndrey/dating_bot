package module.telegram_utils;

import lombok.NoArgsConstructor;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import module.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Method;
import java.util.Map;

@DIBean
@NoArgsConstructor
public class UpdateReceiver {

    private static final Logger logger = LoggerFactory.getLogger(UpdateReceiver.class);

    @InjectBean("BeanManager")
    public BeanManager manager;

    @InjectBean
    public MethodExecutor executor;

    @InjectBean("commandsPool")
    public Map<String, Method> commandsPool;

    @InjectBean("callbacksPool")
    public Map<String, Method> callbacksPool;

    public void processUpdate(Update update) {
        try {
            manager.loadAll(update);

            Message msg = update.getMessage();
            String text = null;
            if (msg != null) text = msg.getText();

            User user = (User) manager.find("currentUser");
            String currentCommand = user.getCurrentCommand();

            logger.info("message text: " + text +", currentCommand: " + currentCommand);
            Method method = commandsPool.get(currentCommand == null ? text : currentCommand);

            if (method != null) executor.invokeCommand(method);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
