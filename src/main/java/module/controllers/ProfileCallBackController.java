package module.controllers;

import lombok.NoArgsConstructor;
import module.domain.Address;
import module.domain.Location;
import module.domain.LocationFinder;
import module.domain.UserCash;
import module.domain.persistentEntities.User;
import module.domain.persistentEntities.UserFilter;
import module.domain.persistentEntities.UserPhoto;
import module.domain.enums.FindBy;
import module.domain.enums.RegSteps;
import module.domain.enums.Sex;
import module.util.exeptions.CallBackControllerException;
import module.util.telegramUtils.TelegramUtils;
import module.util.telegramUtils.annotations.CallBackFun;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.SIZE;
import static java.lang.Integer.parseInt;
import static module.controllers.ControllerUtils.*;
import static module.util.HibernateUtils.unregisteredUserMap;
import static module.util.telegramUtils.TelegramUtils.*;


@DIBean
@NoArgsConstructor
public class ProfileCallBackController extends CallBackController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileCallBackController.class);


    @CallBackFun
    public void registration() {
        try {
            Update update = (Update) manager.find("Update");
            Message message = getMessage(update);
            Long userId = getUserIdFromUpdate(update);

            User user = unregisteredUserMap.get(userId);
            String chatId;

            if (user.getUserCash().getRegStep() == null) {
                user.setChat_id(message.getChatId());
                user.getUserCash().setRegStep(RegSteps.NAME);
                user.getUserCash().setCurrentMethod("registration");

                unregisteredUserMap.put(userId, user);
                chatId = message.getChatId().toString();
            } else chatId = user.getChat_id().toString();

            switch (user.getUserCash().getRegStep().toString()) {
                case "NAME" -> {
                    bot.execute(new SendMessage(chatId, "Твоє ім'я: "));
                    user.getUserCash().nextStep();
                }

                case "SEX" -> {
                    user.setName(message.getText());

                    bot.execute(SendMessage.builder()
                            .text("Ти дівчина/хлопець?")
                            .replyMarkup(new InlineKeyboardMarkup(List.of(
                                    ControllerUtils.createOneRowBtn("Дівчина", "sex"),
                                    ControllerUtils.createOneRowBtn("Хлопець", "sex")
                            )))
                            .chatId(chatId)
                            .build());
                }
                case "AGE" -> {
                    user.setSex(Sex.female);
                    user.getUserCash().nextStep();
                    bot.execute(new SendMessage(chatId, "Скільки тобі років?"));
                }
                case "ABOUT" -> {
                    user.setAge(parseInt(message.getText()));
                    user.getUserCash().nextStep();
                    bot.execute(new SendMessage(chatId, "Напиши щось круте про себе :)"));
                }
                case "PHOTOS" -> {
                    user.setAbout(message.getText());
                    user.getUserCash().setCurrentMethod("loadPhotos");
                    loadPhotos();
                }

                default -> logger.error("default step");
            }

        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
            throw new CallBackControllerException(e);
        }
    }



    @CallBackFun
    public void loadPhotos() {
        Update update = (Update) manager.find("Update");

        Message message = getMessage(update);
        User user = userDao.getByTelegramId(message.getFrom().getId());


        try {
            if (message.getPhoto() == null) {
                bot.execute(new SendMessage(user.getChat_id().toString(), "Тепер по черзі відправ свої фото (2-3)"));
            } else {

                List<PhotoSize> photos = message.getPhoto();
                String fieldId = photos.get(photos.size() -1).getFileId();

                SendMessage sendMessage = SendMessage.builder()
                        .chatId(user.getChat_id().toString())
                        .text("0_0")
                        .build();
                logger.info("SIZE OF PHOTO IS : " + user.getPhotos().size());
                switch (user.getPhotos().size()) {

                    case 0 -> {
                        user.getPhotos().add(new UserPhoto(fieldId));
                        sendMessage.setText("1 фото з 3 завантажено");
                        bot.execute(sendMessage);
                    }
                    case 1 -> {
                        user.getPhotos().add(new UserPhoto(fieldId));
                        sendMessage.setText("2 фото з 3 завантажено");
                        sendMessage.setReplyMarkup(new InlineKeyboardMarkup(List.of(createOneRowBtn("Це все", "saveUser"))));
                        bot.execute(sendMessage);
                    }
                    case 2 -> {
                        user.getPhotos().add(new UserPhoto(fieldId));
                        userDao.save(user);
                        profile();
                    }

                    default -> logger.error("default step");
                }
            }
        }  catch (TelegramApiException ex) {
            logger.error(ex.getMessage());
            throw new CallBackControllerException(ex);
        }
    }


    @CallBackFun
    public void saveUser() {
        Update update = (Update) manager.find("Update");
        Long userId = getUserIdFromUpdate(update);
        User user = unregisteredUserMap.get(userId);
        userDao.save(user);
        unregisteredUserMap.remove(userId);
        profile();
    }

    @CallBackFun
    public void profile() {
        methodExecutor.invokeCommand("/profile");
    }

    @CallBackFun
    public void settings() {
        methodExecutor.invokeCommand("/settings");
    }


    @CallBackFun
    public void sex() {
        Update update = (Update) manager.find("Update");
        User user = unregisteredUserMap.get(update.getCallbackQuery().getFrom().getId());

        String text = update.getCallbackQuery().getMessage().getText();
        Sex sex = text.equals("Дівчина") ? Sex.female : Sex.male;
        user.setSex(sex);
        user.getUserCash().nextStep();
        registration();
    }




    @CallBackFun
    public void getPreferredLocation() {
        try {
            Update update = (Update) manager.find("Update");
            User user = unregisteredUserMap.get(update.getCallbackQuery().getFrom().getId());
            KeyboardButton button1 = new KeyboardButton("Надати мою локацію");
            button1.setRequestLocation(true);

            bot.execute(SendMessage.builder()
                    .replyMarkup( new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button1)))))
                    .text("Надати локацію")
//                    .chatId(user.getChatId())
                    .build());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }



    @CallBackFun
    public void filters() {
        try {
            Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
            User user = userDao.getByTelegramId(userId);
            Long chatId = user.getChat_id();

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                    List.of(
                            createRowBtn("Вік","updateAgeFilter"),
                            createRowBtn("Місце","updateLocationFilter")
                    ),
                    createOneRowBtn("<< Назад","settings")
            ));

            String messageText = "Фільтри пошуку";

            if (user.getUserCash().getLastMessageId() == null) {
                Message message = bot.execute(SendMessage.builder()
                        .text(messageText)
                        .chatId(chatId)
                        .replyMarkup(markup)
                        .build());

                user.getUserCash().setLastMessageId(message.getMessageId());
            } else {
                bot.execute(EditMessageText.builder()
                        .text(messageText)
                        .messageId(user.getUserCash().getLastMessageId())
                        .chatId(chatId)
                        .replyMarkup(markup)
                        .build());
            }

        } catch (TelegramApiException e) {
            logger.error(e.toString());
            throw new CallBackControllerException(e);
        }
    }

    @CallBackFun
    public void updateAgeFilter() {
        try {
            Update update = getUpdate();
            Long userId = TelegramUtils.getUserIdFromUpdate(update);
            User user = userDao.getByTelegramId(userId);

            bot.execute(EditMessageText.builder()
                    .messageId(user.getUserCash().getLastMessageId())
                    .chatId(user.getChat_id())
                    .text("Поточний вік: "+ user.getUserFilter().getAge_from() + "-" + user.getUserFilter().getAge_to() +
                            "\nОбрати інший вік: ")
                    .replyMarkup(new InlineKeyboardMarkup(List.of(
                            List.of(
                                    createRowBtn("16-18", "updateAgeBtn?16-18"),
                                    createRowBtn("18-21", "updateAgeBtn?18-21"),
                                    createRowBtn("21-27", "updateAgeBtn?21-27"),
                                    createRowBtn("27-35", "updateAgeBtn?27-35"),
                                    createRowBtn("35-40", "updateAgeBtn?35-40"),
                                    createRowBtn("40-50", "updateAgeBtn?40-50")
                            ),
                            List.of(
                                    createRowBtn("<< Назад", "filters"),
                                    createRowBtn("Вказати", "ageFilter$setCustomAge")
                            )
                    )))
                    .build());

        } catch (TelegramApiException e) {
            logger.error(e.toString());
            throw new CallBackControllerException(e);
        }
    }


    @CallBackFun
    public void updateAgeBtn(String age) {
        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);

        String[] arr = age.split("-");
        user.getUserFilter().setAge_from(parseInt(arr[0]));
        user.getUserFilter().setAge_to(parseInt(arr[1]));

        userDao.update(user);
        filters();
    }

    @CallBackFun
    public void ageFilter$setCustomAge() throws TelegramApiException {
        Update update = getUpdate();
        Long userId = TelegramUtils.getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);
        Message message = getMessage(update);
        Long chatId = user.getChat_id();


        if (user.getUserCash().getCurrentMethod() == null) {
            bot.execute(EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(user.getUserCash().getLastMessageId())
                    .text("Введи вік в форматі -> від-до.\nНаприклад: 18-25, 40-45")
                    .replyMarkup(null)
                    .build());

            user.getUserCash().setCurrentMethod("ageFilter$setCustomAge");

        } else {
            String userInput = message.getText();
            if (ageIsValid(userInput)) {
                String[] arr = userInput.split("-");
                user.getUserFilter().setAge_from(parseInt(arr[0]));
                user.getUserFilter().setAge_to(parseInt(arr[1]));

                userDao.update(user);

                user.getUserCash().setCurrentMethod(null);
                deleteLastMessages(user);
                filters();

            } else {
                Message message1 = bot.execute(SendMessage.builder()
                        .text("Невірний формат. Спробуй ще раз")
                        .chatId(chatId)
                        .build());

                user.getUserCash().getLastMessagesId().add(message1.getMessageId());
            }
        }
    }



    @CallBackFun
    public void updateLocationFilter() {
        try {
            Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
            User user = userDao.getByTelegramId(userId);

            bot.execute(EditMessageText.builder()
                    .messageId(user.getUserCash().getLastMessageId())
                    .chatId(user.getChat_id())
                    .text("Поточний пошук по: " + FindBy.translate(user.getUserFilter()) +"\nОбрати інше місце: ")
                    .replyMarkup(new InlineKeyboardMarkup(List.of(
                            List.of(
                                    createRowBtn("Область", "updateFilterState"),
                                    createRowBtn("Район", "checkLocation?DISTINCT"),
                                    createRowBtn("Місто", "checkLocation?CITY"),
                                    createRowBtn("СМТ", "checkLocation?TOWN"),
                                    createRowBtn("Село", "checkLocation?VILLAGE")
                            ),
                            List.of(
                                    createRowBtn("<< Назад", "filters"),
                                    createRowBtn("Шукати по дистанції", "updateFilterDistance")
                            )
                    )))
                    .build());
        } catch (TelegramApiException e) {
            logger.error(e.toString());
            throw new CallBackControllerException(e);
        }
    }

    @CallBackFun
    public void checkLocation(String findByLocation) throws TelegramApiException {
        Update update = getUpdate();
        Long userId = TelegramUtils.getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);
        UserCash cash = user.getUserCash();
        FindBy findBy = FindBy.valueOf(findByLocation);

        if (cash.getCurrentMethod() == null) {
            String text;
            switch (findBy) {
                case DISTINCT -> text = "Вкажи назву району в форматі --> <назва> район.\nНаприклад: бериславський район";
                case VILLAGE -> text = "Вкажи назву села";
                case TOWN -> text = "Вкажи назву містечка/СМТ";
                case CITY -> text = "Вкажи назву міста";
                default -> {
                    logger.error("Default step, module.controllers.ProfileCallBackController.updateFilterLocation method");
                    throw new CallBackControllerException("Default step");
                }
            }
            Message message = bot.execute(SendMessage.builder()
                    .chatId(user.getChat_id())
                    .text(text)
                    .build());
            cash.setCurrentMethod("checkLocation?" + findByLocation);

            cash.getLastMessagesId().add(message.getMessageId());
        } else {
            String userInput = getMessage(update).getText();

            if (containsOnlyLetters(userInput)) {
                LocationFinder locationFinder = new LocationFinder();
                Address[] addresses = locationFinder.filterLocations(userInput, findBy)
                        .toArray(new Address[]{});

                user.getUserCash().setAddresses(addresses); // load addresses to cash

                List<List<InlineKeyboardButton>> listOfBtns = new ArrayList<>();

                // adds buttons to list
                for (int i = 0; i < addresses.length; i++) {
                    listOfBtns.add(
                            createOneRowBtn(
                                    addresses[i].toFormattedString(), // text of button is reduced initial name
                                    "updateFilterLocation?" + findByLocation+"_" + i  // Callback data is method for execution with parameter.
                                                      // Parameter is index if array witch contains location
                            ));
                }

                cash.setCurrentMethod(null);

                Message message = printInlineKeyboard(user.getChat_id(), "Обери", listOfBtns);
                cash.getLastMessagesId().add(message.getMessageId());
            }
        }
    }

    @CallBackFun
    public void updateFilterLocation(String findBy, String data) throws TelegramApiException {

        Update update = getUpdate();
        Long userId = TelegramUtils.getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);

        UserFilter filter = user.getUserFilter();
        FindBy find = FindBy.valueOf(findBy);
        filter.setFind_by(find);

        String locName = user.getUserCash().getAddresses()[Integer.parseInt(data)]
                .toFormattedString();

        user.getUserCash().setAddresses(null);


        switch (find) {
            case CITY -> filter.setCity(locName);
            case TOWN -> filter.setTown(locName);
            case VILLAGE -> filter.setVillage(locName);
            case DISTINCT -> filter.setDistrict(locName);
        }
        userDao.update(user);

        deleteLastMessages(user);
        filters();
    }

    @CallBackFun
    public void updateFilterDistance() throws TelegramApiException {
        Update update = getUpdate();
        Message message = getMessage(update);
        Long userId = TelegramUtils.getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);
        Long chatId = user.getChat_id();

        UserCash cash = user.getUserCash();

        if (cash.getCurrentMethod() == null) {
            Message message1 = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Вкажи дистанцію (в км)")
                    .build());
            cash.setCurrentMethod("updateFilterDistance");

            cash.getLastMessagesId().add(message1.getMessageId());

        } else {
            deleteLastMessages(user);

            user.getUserFilter().setFind_by(FindBy.DISTANCE);
            user.getUserFilter().setDistance(Double.parseDouble(message.getText()));
            userDao.update(user);

            cash.setCurrentMethod(null);
            filters();
        }
    }



    @CallBackFun
    public void updateFilterState() throws TelegramApiException {
        Update update = getUpdate();
        Long userId = TelegramUtils.getUserIdFromUpdate(update);
        User user = userDao.getByTelegramId(userId);

        if (user.getUserCash().getCurrentMethod() == null) {

            String district = user.getUserFilter().getDistrict();
            String text = "Вибери свою область";
            if (district != null) {
                text += ". Поточна: " + district;
            }

            bot.execute(EditMessageText.builder()
                    .chatId(user.getChat_id())
                    .messageId(user.getUserCash().getLastMessageId())
                    .text(text)
                    .replyMarkup(new InlineKeyboardMarkup(List.of(
                            List.of(
                                    createRowBtn("Чернівецька", "updRegionBtn?Чернівецька"),
                                    createRowBtn("Черкаська", "updRegionBtn?Черкаська")
                            ),
                            List.of(
                                    createRowBtn("Хмельницька", "updRegionBtn?Хмельницька"),
                                    createRowBtn("Херсонська", "updRegionBtn?Херсонська")
                            ),
                            List.of(
                                    createRowBtn("Харківська", "updRegionBtn?Харківська"),
                                    createRowBtn("Тернопільська", "updRegionBtn?Тернопільська")
                            ),
                            List.of(
                                    createRowBtn("Сумська", "updRegionBtn?Сумська"),
                                    createRowBtn("Рівненська", "updRegionBtn?Рівненська")
                            ),
                            List.of(
                                    createRowBtn("Київська", "updRegionBtn?Київська"),
                                    createRowBtn("Львівська", "updRegionBtn?Львівська")
                            ),
                            List.of(
                                    createRowBtn("Одеська", "updRegionBtn?Одеська"),
                                    createRowBtn("Полтавська", "updRegionBtn?Полтавська")
                            ),

                            List.of(
                                    createRowBtn("Чернігівська", "updRegionBtn?Чернігівська"),
                                    createRowBtn("Луганська", "updRegionBtn?Луганська")
                            ),
                            List.of(
                                    createRowBtn("Кіровоградська", "updRegionBtn?Кіровоградська"),
                                    createRowBtn("Івано-Франківська", "updRegionBtn?Івано-Франківська")
                            ),
                            List.of(
                                    createRowBtn("Запорізька", "updRegionBtn?Запорізька"),
                                    createRowBtn("Закарпатська", "updRegionBtn?Закарпатська")
                            ),
                            List.of(
                                    createRowBtn("Житомирська", "updRegionBtn?Житомирська"),
                                    createRowBtn("Донецька", "updRegionBtn?Донецька")
                            ),
                            List.of(
                                    createRowBtn("Дніпропетровська", "updRegionBtn?Дніпропетровська"),
                                    createRowBtn("Волинська", "updRegionBtn?Волинська")
                            ),
                            List.of(
                                    createRowBtn("Вінницька", "updRegionBtn?Вінницька"),
                                    createRowBtn("АР Крим", "updRegionBtn?АР Крим")
                            ),
                            List.of(
                                    createRowBtn("<< Назад", "updRegionBtn?updateLocationFilter"),
                                    createRowBtn("Миколаївська", "updRegionBtn?Миколаївська")
                            )

                    )))
                    .build());
        } else {
            user.getUserFilter().setDistrict(update.getCallbackQuery().getData());
            userDao.update(user);
            user.getUserCash().setCurrentMethod(null);

            filters();
        }
    }

    @CallBackFun
    public void updRegionBtn(String district) {
        Long userId = TelegramUtils.getUserIdFromUpdate(getUpdate());
        User user = userDao.getByTelegramId(userId);

        user.getUserFilter().setDistrict(district);
        user.getUserFilter().setFind_by(FindBy.DISTINCT);

        userDao.update(user);
        filters();
    }


}