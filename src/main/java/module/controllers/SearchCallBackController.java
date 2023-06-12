package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.persistentEntities.User;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;

@DIBean
@NoArgsConstructor
public class SearchCallBackController extends CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(SearchCallBackController.class);

    @CallBackFun
    public void search() {
        try {
            Update update = (Update) manager.find("Update");
            User user = unregisteredUserMap.get(update.getCallbackQuery().getFrom().getId());

            Message message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
            String chatId = message.getChatId().toString();
            User randomUser = new User();

            String caption =
                            randomUser.getName() + " " +
                            randomUser.getAge() + "\n" +
                            randomUser.getAbout();

            List<InputMedia> inputMediaList = new ArrayList<>();
//            randomUser.getPhotos().forEach(p -> inputMediaList.add(new InputMediaPhoto(p.getTelegramPhotoId())));
            inputMediaList.get(0).setCaption(caption);

            bot.execute(new SendMediaGroup(chatId, inputMediaList));

            bot.execute(SendMessage.builder()
                            .text("Дія")
                            .replyMarkup(new InlineKeyboardMarkup(List.of(
                                    List.of(
                                            createRowBtn("Далі⬇", "next"),
                                            createRowBtn("✉", "likeWithMessage"),
                                            createRowBtn("❤", "like")
                                    )
                            )))
                    .build());

            methodExecutor.invokeCallBack("menu");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @CallBackFun
    public void like() {

    }

    @CallBackFun
    public void likeWithMessage() {

    }

    @CallBackFun
    public void next() {

    }

}
