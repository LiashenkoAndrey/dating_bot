package module.util.telegramUtils;

import lombok.NoArgsConstructor;
import module.dao.UserDao;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import module.domain.persistentEntities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static module.util.HibernateUtils.unregisteredUserMap;
import static module.util.telegramUtils.TelegramUtils.*;


@DIBean
@NoArgsConstructor
public class UpdateReceiver {

    private static final Logger logger = LoggerFactory.getLogger(UpdateReceiver.class);

    @InjectBean
    public MethodExecutor executor;

    @InjectBean
    public UserDao userDao;

    public static void main(String[] args) {
        System.out.println("checkLocation?VILLAGE".contains("?"));
    }

    public void processUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            logger.info(printUpdate(update));

            String data = update.getCallbackQuery().getData();
            if (data.contains("?")) {
                String[] arr = data.split("\\?");
                String methodName = arr[0];
                System.out.println(arr[1]);
                logger.info(arr[1]);
                logger.info(Arrays.toString(arr[1].split("_")));
                System.out.println(Arrays.toString(arr[1].split("_")));

                executor.invokeCallBack(methodName,  arr[1].split("_"));
            } else {
                executor.invokeCallBack(update.getCallbackQuery().getData());
            }
        } else {

            Message msg = update.getMessage();
            String text = msg.getText();
            Long userId = msg.getFrom().getId();

            User user;
            if (userDao.isRegistered(userId)) {
                user = userDao.getByTelegramId(userId);
            } else if (unregisteredUserMap.get(userId) != null) {
                user = unregisteredUserMap.get(userId);
            } else user = null;


            if (user != null) {
                logger.info(printUpdate(update) + printUser(user));
                String currentCommand = user.getUserCash().getCurrentMethod();

                if (user.getChat_id() == null) {
                    user.setChat_id(msg.getChatId());
                }

                if (currentCommand != null) {
                    String[] args = null;
                    if (currentCommand.contains("?")) {
                        String[] arr = currentCommand.split("\\?");
                        currentCommand = arr[0];
                        args = arr[1].split("_");
                    }

                    if (currentCommand.startsWith("/")) executor.invokeCommand(currentCommand);
                    else {
                        if (args != null) executor.invokeCallBack(currentCommand, (Object[]) args);
                        executor.invokeCallBack(currentCommand);
                    }
                } else if (text != null) {
                    if (text.startsWith("/")) executor.invokeCommand(text);
                    else executor.invokeCallBack(text);
                }
            } else {
                if (text.startsWith("/")) executor.invokeCommand(text);
                else executor.invokeCallBack(text);
            }

        }
    }

}
