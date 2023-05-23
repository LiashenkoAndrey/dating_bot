package module.controllers;

import lombok.NoArgsConstructor;
import module.Bot;
import module.services.UserService;
import module.domain.enums.RegSteps;
import module.domain.enums.Sex;
import module.domain.User;
import module.telegram_utils.MethodExecutor;
import module.telegram_utils.annotations.Command;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.*;

@DIBean
@NoArgsConstructor
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @InjectBean
    public Bot bot;

    @InjectBean("BeanManager")
    public BeanManager beanManager;

    @InjectBean
    public UserService userService;

    @InjectBean
    public MethodExecutor methodExecutor;

    @InjectBean("callbacksPool")
    public Map<String, Method> callbacksPool;

    @InjectBean("commandsPool")
    public Map<String, Method> commandsPool;

    @Command("/start")
    public void start() {
        Update update = (Update) beanManager.find("Update");
        SendMessage message = new SendMessage();
        message.setText("Привіт це чат бот знайомств bluadki\nГотовий(a) почати? Давай не соромся:) Буде круто!");
        message.setChatId(update.getMessage().getChatId());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Нова анкета");
        button.setCallbackData("new_user");

        rowInline.add(button);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        try {
            bot.execute(message);
        } catch (TelegramApiException ex) {
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

    @Command("/reg")
    public void reg() {
        try {
            Update update = (Update) beanManager.find("Update");
            User user = (User) beanManager.find("currentUser");
            SendMessage sendMessage = new SendMessage();
            Message message;

            if (update.hasCallbackQuery()) message = update.getCallbackQuery().getMessage();
            else message = update.getMessage();

            if (user.getTelegram_id() == null && !message.getFrom().getIsBot()) {
                Long id = message.getFrom().getId();
                user.setTelegram_id(id);
            }

            sendMessage.setChatId(message.getChatId());

            switch (user.getRegStep().toString()) {
                case "NAME" -> sendMessage.setText("Твоє Ім'я:");

                case "SEX" -> {
                    sendMessage.setText("Ти дівчина/хлопець?");
                    String string = update.getMessage().getText();
                    user.setName(string);

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                    InlineKeyboardButton button2 = new InlineKeyboardButton();
                    button2.setText("Хлопець");
                    button2.setCallbackData("sex");

                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText("Дівчина");
                    button.setCallbackData("sex");
                    rowInline.add(button);
                    rowInline.add(button2);
                    rowsInline.add(rowInline);

                    markupInline.setKeyboard(rowsInline);
                    sendMessage.setReplyMarkup(markupInline);
                }
                case "AGE" -> {
                    sendMessage.setText("Скільки тобі років?");
                    user.setSex(Sex.FEMALE);
                }
                case "ABOUT" -> {
                    sendMessage.setText("Напиши щось круте про себе :)");
                    user.setAge(Integer.parseInt(message.getText()));
                }
                case "PHOTOS" -> {
                    user.setAbout(message.getText());
                    user.setCurrentCommand("/loadPhotos");
                    methodExecutor.invokeCommand(commandsPool.get("/loadPhotos"));
                }

                default -> logger.error("default step");
            }

            if (user.getRegStep() != RegSteps.PHOTOS) {
                user.nextStep();
                bot.execute(sendMessage);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Command("/loadPhotos")
    public void loadPhotos() {
        logger.info("command: " +"/loadPhotos");
        Update update = (Update) beanManager.find("Update");
        User user = (User) beanManager.find("currentUser");
        Message message = update.getMessage();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());

        InlineKeyboardMarkup saveUserBtn = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Це все");
        button.setCallbackData("saveUser");
        rowInline.add(button);
        rowsInline.add(rowInline);

        saveUserBtn.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(saveUserBtn);


        try {
            if (message.getPhoto() == null) {
                sendMessage.setText("Тепер по черзі відправ декілька своїх 2-3 фото");
                bot.execute(sendMessage);
            } else {

                if (user.getPhotos().size() > 0) sendMessage.setReplyMarkup(saveUserBtn);

                List<PhotoSize> photos = message.getPhoto();
                int size = photos.size() -1;
                String fieldId = photos.get(size).getFileId();

                switch (user.getPhotos().size()) {

                    case 0 -> {
                        sendMessage.setText("1 фото з 3 завантажено");
                        user.getPhotos().add(fieldId);
                    }
                    case 1 -> {
                        sendMessage.setText("2 фото з 3 завантажено");
                        user.getPhotos().add(fieldId);
                    }
                    case 2 -> user.getPhotos().add(fieldId);

                    default -> logger.error("default step");
                }

                if (user.getPhotos().size() != 3) {
                    bot.execute(sendMessage);
                } else {
                    methodExecutor.invokeCallBack(callbacksPool.get("saveUser"), update);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
