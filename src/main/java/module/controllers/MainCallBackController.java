package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.UserCash;
import module.domain.persistentEntities.User;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;
import static module.controllers.ControllerUtils.createRowBtn;
import static module.util.HibernateUtils.unregisteredUserMap;


@DIBean
@NoArgsConstructor
public class MainCallBackController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(MainCallBackController.class);

    @CallBackFun
    public void menu() {
        methodExecutor.invokeCommand("/menu");
    }

    @CallBackFun
    public void profile() {
        methodExecutor.invokeCommand("/profile");
    }

    @CallBackFun
    public void settings() {
        methodExecutor.invokeCommand("/settings");
    }

}
