package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.persistentEntities.User;
import module.util.exeptions.CallBackControllerException;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;

@DIBean
@NoArgsConstructor
public class SettingsCallBackController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(SettingsCallBackController.class);

    @CallBackFun
    public void mode() {
        try {
            Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());

            User user;
            if (userDao.isRegistered(userId)) {
                user = userDao.getByTelegramId(userId);
            } else {
                user = unregisteredUserMap.get(userId);
            }

            Long chatId = user.getChat_id();

            String messageText ="Ти маєш на вибір два режими:\n" +
                    "• Звичайний\n" +
                    "• Зустріч прямо зараз \n\n" +
                    "<b>Звичайний</b> - це типовий режим де ти просто лайкаєш анкету та почнеш спілкуватися якщо симпатія взаємна.\n\n" +
                    "<b>Зустріч прямо зараз</b> - ти маєш лайкати анкети і як тільки отримуєш перший взаємний лайк я надішлю тобі контакт партнера та випадково оберу час зустрічі.\n" +
                    "Вам необхідно зустрітись, інакше той что не прийшов отримує тимчасовий бан! :) \n" +
                    "а тому хто залишився один буде тимчасово підвищено рейтинг (щою не сумував(ла))\n" +
                    "Про систему рейтингу читай в /about. ";
           bot.execute(EditMessageText.builder()
                            .chatId(chatId)
                            .text(messageText)
                            .messageId(user.getUserCash().getLastMessageId())
                            .parseMode("html")
                            .replyMarkup(new InlineKeyboardMarkup(List.of(
                                    List.of(
                                            createRowBtn("Звичайний", "setDefaultMode"),
                                            createRowBtn("Зустріч прямо зараз", "setMeetingRightNowMode")
                                    ),
                                    List.of(
                                            createRowBtn("<< Назад", "settings")
                                    )
                            )))
                    .build());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new CallBackControllerException(ex);
        }
    }
}
