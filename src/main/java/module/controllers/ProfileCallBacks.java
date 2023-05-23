package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.User;
import module.domain.enums.RegSteps;
import module.domain.enums.Sex;
import module.telegram_utils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DIBean
@NoArgsConstructor
public class ProfileCallBacks extends CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileCallBacks.class);

    @CallBackFun
    public void new_user(CallbackQuery query) {
        try {

            logger.info("CallBack function: " + "\"new_user\" executed");
            user.setCurrentCommand("/reg");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }


    @CallBackFun
    public void sex(CallbackQuery query) {
        logger.info("CallBack function: " + "\"sex\" executed");
        String text = query.getMessage().getText();
        Sex sex = text.equals("Дівчина") ? Sex.FEMALE : Sex.MALE;
        user.setSex(sex);
    }


    @CallBackFun
    public void saveUser(CallbackQuery query) {
        logger.info("CallBack function: " + "\"saveUser\" executed");
        userService.save(user);
        user.setCurrentCommand(null);

        showProfile(query);
    }


    @CallBackFun
    private void newPhotos() {
        user.getPhotos().clear();
        user.setCurrentCommand("/loadPhotos");
        methodExecutor.invokeCommand(commandsPool.get("/loadPhotos"));
    }

    @CallBackFun
    public void showProfile(CallbackQuery query) {
        logger.info("CallBack function: " + "\"showProfile\" executed");

        try {
            Message message = query.getMessage();
            String chatId = message.getChatId().toString();
            User user = userService.getProfileByTelegramId(query.getFrom().getId());

            List<InputMedia> inputMediaList = new ArrayList<>();
            user.getPhotos().forEach(p -> inputMediaList.add(new InputMediaPhoto(p)));

            String caption = user.getName() + "\n" + user.getAge() + "\n" + user.getAbout();
            inputMediaList.get(inputMediaList.size()-1).setCaption(caption);
            bot.execute(new SendMediaGroup(chatId, inputMediaList));

            List<List<InlineKeyboardButton>> rowsInline = List.of(
                    createRowBtn("Мені подобається) Далі➡", "menu"),
                    createRowBtn("Замінити фото\uD83D\uDDBC️", "newPhotos"),
                    createRowBtn("Переробити анкету", "createNewProfile")
            );

            Message sentMessage = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Дія")
                    .replyMarkup(new InlineKeyboardMarkup(rowsInline))
                    .build());

            user.setLastMessageId(sentMessage.getMessageId());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @CallBackFun
    public void createNewProfile(CallbackQuery query) {
        try {
            String chatId = query.getMessage().getChatId().toString();
            bot.execute(new DeleteMessage(chatId, user.getLastMessageId()));

            user.setAbout(null);
            user.setAge(0);
            user.setName(null);
            user.setPhotos(new ArrayList<>());
            user.setSex(null);
            user.setRegStep(RegSteps.NAME);
            user.setCurrentCommand("/reg");

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }


    private List<InlineKeyboardButton> createRowBtn(String text, String callbackData) {
        List<InlineKeyboardButton> btnWrapper = new ArrayList<>();
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        btnWrapper.add(btn);
        return btnWrapper;
    }
}
