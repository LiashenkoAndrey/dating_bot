package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.persistentEntities.User;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;
import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;


@DIBean
@NoArgsConstructor
public class MainCallBackController extends CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(MainCallBackController.class);

    @CallBackFun
    public void menu() {
        logger.info("CallBack function: " + "\"menu\" executed");
        try {
            Update update = (Update) manager.find("Update");
            Long userId = TelegramUtils.getUserIdFromUpdate(update);

            User user;
            if (userDao.isRegistered(userId)) {
                user = userDao.getByTelegramId(userId);
            } else {
                user = unregisteredUserMap.get(userId);
            }

            Long chatId = user.getChat_id();

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(List.of(
                    createRowBtn("Моя анкета", "profile"),
                    createRowBtn("Налаштування", "settings"),
                    createRowBtn("<< Пошук >>", "search")
            )));

            String messageText = "Меню";

            if (user.getUserCash().getLastMessageId() == null) {
                Message sentMessage = bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .replyMarkup(markup)
                        .build());
                user.getUserCash().setLastMessageId(sentMessage.getMessageId());
            } else {
                Integer msgId = user.getUserCash().getLastMessageId();

                bot.execute(EditMessageText.builder()
                                .messageId(msgId)
                                .replyMarkup(markup)
                                .chatId(chatId)
                                .text("Меню")
                        .build());
            }


        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
