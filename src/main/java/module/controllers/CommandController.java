package module.controllers;

import lombok.NoArgsConstructor;
import module.Bot;
import module.dao.LikeDao;
import module.dao.UserDao;
import module.domain.UserCash;
import module.domain.persistentEntities.Like;
import module.domain.persistentEntities.User;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.Command;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static module.controllers.ControllerUtils.createOneRowBtn;
import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;
import static module.util.telegramUtils.TelegramUtils.getMessage;
import static module.util.telegramUtils.TelegramUtils.getUserIdFromUpdate;

@DIBean
@NoArgsConstructor
public class CommandController extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(CommandController.class);

    @InjectBean
    public Bot bot;

    @InjectBean
    public LikeDao likeDao;

    @InjectBean
    public UserDao userDao;


    @Command("/start")
    public void start() throws TelegramApiException {
        Update update = getUpdate();
        Long userId = getUserIdFromUpdate(update);
        Message message = getMessage(update);

        User user = new User(userId);
        user.setChat_id(message.getChatId());
        unregisteredUserMap.put(userId, user);

        Message message1 = printInlineKeyboard(
                message.getChatId(),
                "Привіт це чат бот знайомств bluadki\nГотовий(a) почати? Давай не соромся:) Буде круто!",
                List.of(createOneRowBtn("Нова анкета", "registration"))
        );
        user.getUserCash().setLastMessageId(message1.getMessageId());

    }

    @Command("/profile")
    public void profile() throws TelegramApiException {

        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);
        Long chatId = user.getChat_id();
        UserCash cash = user.getUserCash();

        List<InputMedia> inputMediaList = new ArrayList<>();
        user.getPhotos().forEach(photo -> inputMediaList.add(new InputMediaPhoto(photo.getPhoto_id())));

        String caption = user.getName() + "\n" + user.getAge() + "\n" + user.getAbout();
        inputMediaList.get(inputMediaList.size()-1).setCaption(caption);

        deleteLastMessages(user);

        List<Message> photosMsg = bot.execute(new SendMediaGroup(chatId.toString(), inputMediaList));
        Message message = bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Моя анкета")
                .replyMarkup(new InlineKeyboardMarkup(List.of(
                        List.of(
                                createRowBtn("Замінити фото\uD83D\uDDBC️", "newPhotos"),
                                createRowBtn("Меню", "menu")
                        ),
                        createOneRowBtn("Переробити анкету", "createNewProfile")
                )))
                .build());

        photosMsg.forEach((p) -> cash.getLastMessagesPhotoIds().add(p.getMessageId()));
        cash.setLastMessageId(message.getMessageId());
    }

    @Command("/settings")
    public void settings() throws TelegramApiException {
        Update update = getUpdate();

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

    @Command("/menu")
    public void menu() {
        try {
            Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
            User user = userDao.getByTelegramId(userId);
            Long chatId = user.getChat_id();
            UserCash cash = user.getUserCash();


            deleteLastProfilePhotos(user);

            List<Like> likes = likeDao.getLikesListOfLikedUser(user.getId());


            var myProfileBtn =  createRowBtn("Моя анкета", "profile");
            var searchBtn = createRowBtn("<< Пошук >>", "search");
            var likesBtn = createRowBtn("Лайки(" + likes.size() +")", "showLikes");
            var settingsBtn = createRowBtn("Налаштування", "settings");
            List<List<InlineKeyboardButton>> btnsList;

            if (likes.size() == 0) {
                btnsList = List.of(
                        List.of(
                                myProfileBtn,
                                searchBtn,
                                settingsBtn
                        )
                );
            } else {
                btnsList = List.of(
                        List.of(
                                myProfileBtn,
                                searchBtn
                        ),
                        List.of(
                                likesBtn,
                                settingsBtn
                        )
                );
            }
            String messageText = "Меню";

            if (cash.getLastMessageId() == null) {
                Message sentMessage = bot.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .replyMarkup(new InlineKeyboardMarkup(btnsList))
                        .build());
                cash.setLastMessageId(sentMessage.getMessageId());
            } else {
                Integer msgId = user.getUserCash().getLastMessageId();

                bot.execute(EditMessageText.builder()
                        .messageId(msgId)
                        .replyMarkup(new InlineKeyboardMarkup(btnsList))
                        .chatId(chatId)
                        .text("Меню")
                        .build());
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
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
