package module.controllers;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import module.Bot;
import module.dao.UserDao;
import module.domain.persistentEntities.User;
import module.util.exeptions.CallBackControllerException;
import module.util.telegramUtils.MethodExecutor;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.Command;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static module.controllers.ControllerUtils.createOneRowBtn;
import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;
import static module.util.telegramUtils.TelegramUtils.getUserIdFromUpdate;

@DIBean
@NoArgsConstructor
public class CommandController {
    private static final Logger logger = LoggerFactory.getLogger(CommandController.class);

    @InjectBean
    public Bot bot;

    @InjectBean
    public TelegramUtils utils;

    @InjectBean
    public UserDao userDao;

    @InjectBean
    public MethodExecutor methodExecutor;

    @InjectBean("BeanManager")
    public BeanManager beanManager;


    @Command("/start")
    public void start() {
        Update update = (Update) beanManager.find("Update");
        Long userId = update.getMessage().getFrom().getId();

        unregisteredUserMap.put(userId, new User(userId));
        try {
            bot.execute(SendMessage.builder()
                            .text("Привіт це чат бот знайомств bluadki\nГотовий(a) почати? Давай не соромся:) Буде круто!")
                            .chatId(update.getMessage().getChatId())
                            .replyMarkup(new InlineKeyboardMarkup(List.of(createOneRowBtn("Нова анкета", "registration"))))
                    .build());
        } catch (TelegramApiException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Command("/profile")
    public void profile() {

        try {
            Update update = (Update) beanManager.find("Update");
            Long userId = getUserIdFromUpdate(update);
            User user;
            if (userDao.isRegistered(userId)) {
                user = userDao.getByTelegramId(userId);
            } else {
                user = unregisteredUserMap.get(userId);
            }
            Long chatId = user.getChat_id();


            List<InputMedia> inputMediaList = new ArrayList<>();
            user.getPhotos().forEach(photo -> inputMediaList.add(new InputMediaPhoto(photo.getPhoto_id())));

            String caption = user.getName() + "\n" + user.getAge() + "\n" + user.getAbout();
            inputMediaList.get(inputMediaList.size()-1).setCaption(caption);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                    List.of(
                            createRowBtn("Замінити фото\uD83D\uDDBC️", "newPhotos"),
                            createRowBtn("Меню", "menu")
                    ),
                    createOneRowBtn("Переробити анкету", "createNewProfile")
            ));

            String messageText = "Моя анкета";

            if (user.getUserCash().getLastMessageId() == null) {
                bot.execute(new SendMediaGroup(chatId.toString(), inputMediaList));
                Message message = bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .replyMarkup(markup)
                        .build());

                user.getUserCash().setLastMessageId(message.getMessageId());

            } else {
                bot.execute(EditMessageText.builder()
                                .messageId(user.getUserCash().getLastMessageId())
                                .chatId(chatId)
                                .text(messageText)
                        .build());

                bot.execute(EditMessageReplyMarkup.builder()
                        .messageId(user.getUserCash().getLastMessageId())
                        .chatId(chatId)
                        .replyMarkup(markup)
                        .build());
            }


        } catch (TelegramApiException ex) {
            logger.error(ex.toString());
            throw new CallBackControllerException(ex);
        }
    }

    @Command("/settings")
    public void settings() throws TelegramApiException {
        Update update = (Update) beanManager.find("Update");

        Long userId = getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);
        Long chatId = user.getChat_id();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                List.of(
                        createRowBtn("Фільтри", "filters")
                ),
                List.of(
                        createRowBtn(" << Назад", "menu"),
                        createRowBtn("Змінити режим", "mode")
                )
        ));
        String messageText = "Налаштування";

        if (user.getUserCash().getLastMessageId() == null) {
            Message message = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(messageText)
                    .replyMarkup(markup)
                    .build());
            user.getUserCash().setLastMessageId(message.getMessageId());

        } else {
            Integer msgId = user.getUserCash().getLastMessageId();
            bot.execute(EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(msgId)
                    .replyMarkup(markup)
                    .text(messageText)
                    .build());
        }
    }


    @SneakyThrows
    @Command("/show_location")
    public void show_location() {
        Update update = (Update) beanManager.find("Update");

        KeyboardButton button1 = new KeyboardButton("Надати мою локацію");
        button1.setRequestLocation(true);
        Message message = bot.execute(SendMessage.builder()
                .replyMarkup( new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button1)))))
                .text("Надати локацію")
                .chatId(update.getMessage().getChatId())
                .build());
    }





    @Command("/help")
    public void help() {
        logger.info("command: " +"/help");
        System.out.println("help!!");
    }

    @Command("/info")
    public void info() {
        logger.info("command: " +"/info");
    }


}
