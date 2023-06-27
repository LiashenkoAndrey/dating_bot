package module.controllers;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class ControllerUtils {

    public static List<InlineKeyboardButton> createOneRowBtn(String text, String callbackData) {
        return List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build());
    }

    public static InlineKeyboardButton createRowBtn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public static boolean ageIsValid(String str) {
        Pattern pattern = Pattern.compile("^[0-9]{2}-[0-9]{2}$"); // dd-dd pattern
        if (pattern.matcher(str).find()) {
            String[] arr = str.split("-");
            int from = parseInt(arr[0]);
            int to = parseInt(arr[1]);

            // from > 13 and to < 60 and from > to
            return parseInt(arr[0]) >= 13 && parseInt(arr[1]) <= 60 && from < to;
        }
        else return false;
    }
}
