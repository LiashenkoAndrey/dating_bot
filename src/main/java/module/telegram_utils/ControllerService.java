package module.telegram_utils;

import module.controllers.CallBackController;
import module.controllers.MainController;
import module.controllers.ProfileCallBacks;
import module.controllers.ServiceCallBacks;
import module.telegram_utils.annotations.CallBackFun;
import module.telegram_utils.annotations.Command;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ControllerService {

    public static Map<String, Method> processCommands() {
        MainController mainController = new MainController();
        Class<?> c = mainController.getClass();

        return Stream.of(c.getMethods())
                .filter((method -> method.getAnnotation(Command.class) != null))
                .collect(Collectors.toMap(method -> method.getAnnotation(Command.class).value(), Function.identity()));
    }

    public static Map<String, Method> processCallBack() {
        Method[] methods = ArrayUtils.addAll(ProfileCallBacks.class.getMethods(), ServiceCallBacks.class.getMethods());

        return Stream.of(methods)
                .filter((method -> method.getAnnotation(CallBackFun.class) != null))
                .collect(Collectors.toMap(method -> {
                    String val = method.getAnnotation(CallBackFun.class).value();
                    return val.equals("") ? method.getName() : val;
                }, Function.identity() ));
    }
}