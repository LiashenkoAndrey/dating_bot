package module.controllers;

import lombok.NoArgsConstructor;
import module.dao.LikeDao;
import module.dao.UserDao;
import module.domain.UserCash;
import module.domain.persistentEntities.Like;
import module.domain.persistentEntities.User;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static module.controllers.ControllerUtils.createRowBtn;

@DIBean
@NoArgsConstructor
public class SearchCallBackController extends Controller {

    @InjectBean
    public LikeDao likeDao;

    private static final Logger logger = LoggerFactory.getLogger(SearchCallBackController.class);

    public static void main(String[] args) {
        LikeDao likeDao1 = new LikeDao();
        List<Like> likes = likeDao1.getLikesListOfLikedUser(73L);
        System.out.println(likes.size());
        Like like = likes.get(0);
        System.out.println(like);
        System.out.println(like.getId());
        System.out.println(likes.get(0));
    }


    @CallBackFun
    public void search() {
        try {
            Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
            User user = userDao.getByTelegramId(userId);
            User randomUser = userDao.searchRandom(user);

            UserCash cash = user.getUserCash();
            String caption =
                            randomUser.getName() + " " +
                            randomUser.getAge() + "\n" +
                            randomUser.getAbout();

            List<InputMedia> inputMediaList = new ArrayList<>();
            randomUser.getPhotos().forEach(p -> inputMediaList.add(new InputMediaPhoto(p.getPhoto_id())));
            inputMediaList.get(0).setCaption(caption);

            if (cash.getLastSearchMessagesPhotoIds().isEmpty()) {

                List<Message> messageList = bot.execute(new SendMediaGroup(user.getChat_id().toString(), inputMediaList));
                messageList.forEach(message -> cash.getLastSearchMessagesPhotoIds().add(message.getMessageId()));
                Message message = bot.execute(SendMessage.builder()
                        .chatId(user.getChat_id())
                        .text("Дія")
                        .replyMarkup(new InlineKeyboardMarkup(List.of(
                                List.of(
                                        createRowBtn("\uD83D\uDCAC", "message"),
                                        createRowBtn("❤", "like?" + randomUser.getTelegram_id()),
                                        createRowBtn("Далі⬇", "search"),
                                        createRowBtn("--->", "menu")
                                )
                        )))
                        .build());



                user.getUserCash().setLastMessageId(message.getMessageId());
            } else {
                bot.execute(EditMessageMedia.builder()
                        .messageId(cash.getLastSearchMessagesPhotoIds().get(0))
                        .chatId(user.getChat_id())
                        .media(inputMediaList.get(0))
                        .build());

                bot.execute(EditMessageMedia.builder()
                        .messageId(cash.getLastSearchMessagesPhotoIds().get(1))
                        .chatId(user.getChat_id())
                        .media(inputMediaList.get(1))
                        .build());
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @CallBackFun
    public void like(String likedUserId) throws TelegramApiException {
        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);
        User to = userDao.getByTelegramId(Long.parseLong(likedUserId));
        likeDao.save(new Like(user.getId() , to.getId()));
        search();
    }


    @CallBackFun
    public void showLikes() throws TelegramApiException {
        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);
        UserCash cash = user.getUserCash();

        deleteLastMessage(user);
        if (cash.getLastMessageId() == null) cash.setCurrentMethod("showLikes");

        List<Like> likesList = likeDao.getLikesListOfLikedUser(user.getId());
        User liker = userDao.getById(likesList.get(0).getLike_from_id());
        likeDao.deleteById(likesList.get(0));

        List<InputMedia> inputMediaList = new ArrayList<>();
        liker.getPhotos().forEach(p -> inputMediaList.add(new InputMediaPhoto(p.getPhoto_id())));
        inputMediaList.get(0).setCaption(liker.getName() + " " + liker.getAge() + "\n" + liker.getAbout());

        bot.execute(new SendMediaGroup(user.getChat_id().toString(), inputMediaList));

        if (likesList.size() > 1) {
            Message message = bot.execute(SendMessage.builder()
                    .chatId(user.getChat_id())
                    .text("Дія")
                    .replyMarkup(new InlineKeyboardMarkup(List.of(
                            List.of(
                                    createRowBtn("<-- Назад", "breakShowLikes"),
                                    createRowBtn("Далі⬇", "showLikes")
                            )
                    )))
                    .build());

            cash.setLastMessageId(message.getMessageId());
        } else {
            breakShowLikes();
        }
    }

    @CallBackFun
    public void breakShowLikes() {
        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);
        user.getUserCash().setCurrentMethod(null);
        methodExecutor.invokeCommand("/menu");
    }

    @CallBackFun
    public void likeWithMessage() {

    }
}
