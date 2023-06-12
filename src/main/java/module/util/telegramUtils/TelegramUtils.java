package module.util.telegramUtils;

import module.Bot;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@DIBean
public class TelegramUtils {

    private static final Logger logger = LoggerFactory.getLogger(TelegramUtils.class);

    @InjectBean
    public Bot bot;

    @InjectBean("BeanManager")
    public BeanManager manager;

    public static Message getMessage(Update update) {
        return  update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
    }

    public static String printUpdate(Update update) {

        CallbackQuery query = null;
        Message message = getMessage(update);
        User user = message.getFrom();

        StringBuilder sb = new StringBuilder();
        sb.append("\n\nUpdate:\n\t");
        if (update.hasCallbackQuery()) {
            query = update.getCallbackQuery();
            sb.append(
                    "hasCallBack: true\n" +
                    "\t\tData: "+ query.getData() +
                    "\n\t\tFrom: " + message.getFrom().getId()
            );
        } else {
            sb.append(
                    "hasCallBack: false" +
                            "\n\tMessage:" +
                            "\n\t\tText: " + message.getText() +
                            "\n\t\tFrom: ID: " + user.getId() +", Name: " + user.getFirstName() + ", Username: " + user.getUserName()
            );
        }

        sb.append("\n");
        return sb.toString();
    }


    public static String printUser(module.domain.persistentEntities.User user) {
        StringBuilder sb = new StringBuilder();

        sb.append("\nUser:");
        if (user == null) {
            sb.append("\n\tUnregistered");
        } else {
            sb.append("\n\tTelegram_id: " + user.getTelegram_id())
                    .append("\n\tCurrentCommand :" + user.getUserCash().getCurrentMethod())
                    .append("\n\tRegStep :" + user.getUserCash().getRegStep())
                    .append("\n\tId :" + user.getId())
                    .append("\n\tName: " +user.getName())
                    .append("\n\tSex :" + user.getSex())
                    .append("\n\tAbout :" + user.getAbout())
                    .append("\n\tAge :" + user.getAge())
                    .append("\n\tLocation :" + user.getLocation())
                    .append("\n\tPhotos :" + user.getPhotos())
                    .append("\n\tRating :" + user.getRating());
        }
        sb.append("\n");
        return sb.toString();
    }

    public static boolean containsOnlyLetters(String str) {
        if (!str.equals("")) return str.replaceAll(" ", "").chars().allMatch(Character::isLetter);
        else return false;
    }

    public static Long getUserIdFromUpdate(Update update) {
        return update.hasCallbackQuery() ? update.getCallbackQuery().getFrom().getId() : getMessage(update).getFrom().getId();
    }
}
