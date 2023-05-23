package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.User;
import module.domain.enums.Sex;
import module.services.UserService;
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
import java.util.Random;
import java.util.UUID;

@DIBean
@NoArgsConstructor
public class ServiceCallBacks extends CallBackController {

    private static final InlineKeyboardButton searchBtn = InlineKeyboardButton.builder()
            .text("<< Пошук >>")
            .callbackData("search")
            .build();

    private static final InlineKeyboardButton myProfileBtn = InlineKeyboardButton.builder()
            .text("Моя анкета")
            .callbackData("showProfile")
            .build();

    private static final InlineKeyboardButton settingsBtn = InlineKeyboardButton.builder()
            .text("Моя анкета")
            .callbackData("showProfile")
            .build();
    private static final Logger logger = LoggerFactory.getLogger(ServiceCallBacks.class);


    public static void main(String[] args) {
        UserService service = new UserService();
        service.registerInPool(User.builder()
                        .age(15)
                        .name("Maria")
                        .about("Sport")
                        .last_activity(LocalDateTime.now())
                        .photos(List.of("AgACAgIAAxkBAAIHkmRn2AwRwVCgc5xHQ9UbMuV_m95HAAIYyTEb2C0RS4NSH1DEZijsAQ…"))
                        .telegram_id(234235345L)
                        .isActive(true)
                        .sex(Sex.FEMALE)
                .build());

        service.registerInPool(User.builder()
                .age(22)
                .name("Ann")
                .about("Design")
                .last_activity(LocalDateTime.now())
                .photos(List.of("AgACAgIAAxkBAAIHkmRn2AwRwVCgc5xHQ9UbMuV_m95HAAIYyTEb2C0RS4NSH1DEZijsAQ…"))
                .telegram_id(23455375L)
                .isActive(true)
                .sex(Sex.FEMALE)
                .build());

        service.registerInPool(User.builder()
                .age(18)
                .name("Katia")
                .about("A love reading!")
                .last_activity(LocalDateTime.now())
                .photos(List.of("AgACAgIAAxkBAAIHkmRn2AwRwVCgc5xHQ9UbMuV_m95HAAIYyTEb2C0RS4NSH1DEZijsAQ…"))
                .telegram_id(234235647L)
                .isActive(true)
                .sex(Sex.FEMALE)
                .build());

        service.registerInPool(User.builder()
                .age(15)
                .name("Masha")
                .about("It")
                .last_activity(LocalDateTime.now())
                .photos(List.of("AgACAgIAAxkBAAIHkmRn2AwRwVCgc5xHQ9UbMuV_m95HAAIYyTEb2C0RS4NSH1DEZijsAQ…"))
                .telegram_id(234235369L)
                .isActive(true)
                .sex(Sex.FEMALE)
                .build());
    }


    @CallBackFun
    public void search(CallbackQuery query) {
        logger.info("CallBack function: " + "\"search\" executed");
        try {

            if (!user.isActive()) {
                if (user.getTelegram_id() == null) user.setTelegram_id(query.getFrom().getId());
                user.setActive(true);
                userService.registerInPool(user);
            }

            Message message = query.getMessage();
            String chatId = message.getChatId().toString();

            User randomUser = userService.getRandomUserFromPool(user.getSex());

            List<InputMedia> inputMediaList = new ArrayList<>();
            randomUser.getPhotos().forEach(p -> inputMediaList.add(new InputMediaPhoto(p)));

            String caption =
                    randomUser.getName() + " " +
                            randomUser.getAge() + "\n" +
                            randomUser.getAbout();

            inputMediaList.get(0).setCaption(caption);

            bot.execute(new DeleteMessage(chatId, user.getLastMessageId()));
            bot.execute(new SendMediaGroup(chatId, inputMediaList));

            CallbackQuery query1 = new CallbackQuery();
            query1.setMessage(message);
            menu(query1);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }


    @CallBackFun
    public void menu(CallbackQuery query) {
        logger.info("CallBack function: " + "\"menu\" executed");
        try {
            Message message = query.getMessage();

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Меню")
                    .replyMarkup(new InlineKeyboardMarkup(List.of(List.of(searchBtn, myProfileBtn, settingsBtn))))
                    .build();

            Message sentMessage = bot.execute(sendMessage);
            user.setLastMessageId(sentMessage.getMessageId());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
