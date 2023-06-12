package module;

import org.open.cdi.BeanManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.Properties;

public class Runner {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(Runner.class.getClassLoader().getResourceAsStream("application.properties"));
        System.out.println(properties.getProperty("logo"));

        BeanManager beanManager = new BeanManager();

        beanManager.loadAll(properties, beanManager);
        beanManager.loadFromPackages(
                "module",
                "module.controllers" ,
                "module.util.telegramUtils",
                "module.domain",
                "module.dao"
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

