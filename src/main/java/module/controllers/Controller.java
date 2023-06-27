package module.controllers;

import lombok.NoArgsConstructor;
import module.Bot;
import module.controllers.exceptions.ControllerException;
import module.dao.UserDao;

import module.domain.persistentEntities.User;
import module.util.telegramUtils.MethodExecutor;

import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@NoArgsConstructor
@DIBean
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @InjectBean
    public Bot bot;

    @InjectBean
    public UserDao userDao;
    @InjectBean
    public MethodExecutor methodExecutor;

    @InjectBean("BeanManager")
    public BeanManager manager;

    public Update getUpdate() {
        return (Update) manager.find("Update");
    }

    protected void deleteLastMessages(User user) {
        try {
            long chatId = user.getChat_id();

            if (user.getUserCash().getLastMessageId() != null){
                bot.execute(DeleteMessage.builder()
                        .messageId(user.getUserCash().getLastMessageId())
                        .chatId(chatId)
                        .build());
            }

            for (Integer id : user.getUserCash().getLastMessagesId()) {
                bot.execute(DeleteMessage.builder()
                        .messageId(id)
                        .chatId(chatId)
                        .build());

            }
            user.getUserCash().setLastMessageId(null);
            user.getUserCash().getLastMessagesId().clear();

        } catch (TelegramApiException e) {
            logger.error(e.toString());
            throw new ControllerException(e);
        }
    }

    protected void deleteLastMessage(User user) {
        try {
            long chatId = user.getChat_id();
            if (user.getUserCash().getLastMessageId() != null){
                bot.execute(DeleteMessage.builder()
                        .messageId(user.getUserCash().getLastMessageId())
                        .chatId(chatId)
                        .build());
            }
            user.getUserCash().setLastMessageId(null);

        } catch (TelegramApiException e) {
            logger.error(e.toString());
            throw new ControllerException(e);
        }
    }

    protected void deleteLastProfilePhotos(User user) throws TelegramApiException {
        for (Integer id : user.getUserCash().getLastMessagesPhotoIds()) {
            bot.execute(DeleteMessage.builder()
                    .messageId(id)
                    .chatId(user.getChat_id())
                    .build());

        }
        user.getUserCash().getLastMessagesPhotoIds().clear();
    }

    protected void printMessageIsIllegal(Long chatId) throws TelegramApiException {
        bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Рядок має містити лише літери, спробуй ще раз")
                .build());
    }

    protected Message printInlineKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> buttons)
            throws TelegramApiException {
        return bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(new InlineKeyboardMarkup(buttons))
                .build());
    }

}
