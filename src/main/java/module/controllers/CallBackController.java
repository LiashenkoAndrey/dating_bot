package module.controllers;

import lombok.NoArgsConstructor;
import module.Bot;
import module.dao.UserDao;

import module.domain.persistentEntities.User;
import module.util.telegramUtils.MethodExecutor;

import module.util.telegramUtils.TelegramUtils;
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
public class CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(CallBackController.class);



    @InjectBean
    public Bot bot;

    @InjectBean
    public TelegramUtils utils;

    @InjectBean
    public UserDao userDao;
    @InjectBean
    public MethodExecutor methodExecutor;

    @InjectBean("BeanManager")
    public BeanManager manager;


    public Update getUpdate() {
        return  (Update) manager.find("Update");
    }

    protected void deleteLastMessages(User user) throws TelegramApiException {
        long chatId = user.getChat_id();
        bot.execute(DeleteMessage.builder()
                .messageId(user.getUserCash().getLastMessageId())
                .chatId(chatId)
                .build());

        for (Integer id : user.getUserCash().getLastMessagesId()) {
            bot.execute(DeleteMessage.builder()
                    .messageId(id)
                    .chatId(chatId)
                    .build());

        }
        user.getUserCash().setLastMessageId(null);
        user.getUserCash().getLastMessagesId().clear();
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
