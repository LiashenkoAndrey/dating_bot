package module;

import lombok.NoArgsConstructor;
import module.services.UserService;
import org.open.cdi.BeanManager;
import module.domain.enums.RegSteps;
import module.domain.enums.Sex;
import module.domain.User;
import module.telegram_utils.ControllerService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDateTime;
import java.util.ArrayList;


@NoArgsConstructor
public class TelegramServer {

    public static void main(String[] args)  {


        System.out.println("______ _                 _ _    _ \n" +
                "| ___ \\ |               | | |  (_)\n" +
                "| |_/ / |_   _  __ _  __| | | ___ \n" +
                "| ___ \\ | | | |/ _` |/ _` | |/ / |\n" +
                "| |_/ / | |_| | (_| | (_| |   <| |\n" +
                "\\____/|_|\\__,_|\\__,_|\\__,_|_|\\_\\_|\n" +
                "                                  \n" +
                "Version: 0.1                         \n"+
                "                                    ");
        User user = User.builder()
                .sex(null)
                .last_activity(LocalDateTime.now())
                .regStep(RegSteps.NAME)
                .photos(new ArrayList<>())
                .build();

        BeanManager beanManager = new BeanManager();
        beanManager.loadWithName(user, "currentUser");
        beanManager.loadWithName(beanManager, "BeanManager");
        beanManager.loadAll(new UserService());
        beanManager.loadWithName(ControllerService.processCallBack(), "callbacksPool");
        beanManager.loadWithName(ControllerService.processCommands(), "commandsPool");
        beanManager.loadFromPackages(
                "module",
                "module.controllers" ,
                "module.telegram_utils"
        );
        beanManager.init();


        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot((LongPollingBot) beanManager.find("Bot"));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}

