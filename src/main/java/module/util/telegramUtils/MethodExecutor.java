package module.util.telegramUtils;

import module.controllers.*;
import module.util.telegramUtils.annotations.CallBackFun;
import module.util.telegramUtils.annotations.Command;
import module.util.telegramUtils.exceptions.CallBackMethodExecutionException;
import module.util.telegramUtils.exceptions.CommandMethodExecutionException;
import org.open.cdi.BeanManager;
import org.open.cdi.DIContainer;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DIBean
public class MethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodExecutor.class);

    @InjectBean
    public CommandController mainController;

    @InjectBean
    public BeanManager beanManager;

    public static final Map<String, Method> commands = parseCommands();

    public static Map<String, Method> callBacks = parseCallBack();

    public void invokeCommand(String commandName) {
        try {
            Method method = commands.get(commandName);
            if (method != null) {
                logger.info("Received a command: " + method.getName() +" executed" );
                method.invoke(mainController);
            } else {
                logger.error("Method id not present");
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Can't invoke a method with name: " + commandName +", " + e.getMessage());
            throw new CommandMethodExecutionException(e);
        }
    }

    public void invokeCallBack(String callbackName) {
        try {
            Method method = callBacks.get(callbackName);
            if (method != null) {
                logger.info("Received a callBack:" + callbackName + " executed");
                Object o = beanManager.find(DIContainer.parseClassNameFromClassToString(method.getDeclaringClass().getName()));
                method.invoke(o);
            }

        }  catch (Exception e) {
        logger.error("Can't invoke a callBack method with: " + callbackName +", " + e.getMessage());
            throw new CallBackMethodExecutionException(e);
        }
    }

    public void invokeCallBack(CallbackQuery callbackQuery)  {
        try {
            String callBackName = callbackQuery.getData();
            Method method = callBacks.get(callBackName);
            if (method != null) {
                logger.info("Received a callBack:" + callBackName + " executed");
                method.invoke(method.getDeclaringClass().getConstructor().newInstance(), callbackQuery);
            }

        } catch (Exception e) {
            logger.error(e.toString());
            throw new CallBackMethodExecutionException(e);
        }
    }



    public void invokeCallBack(String callbackName, Object... args) {
        try {
            Method method = callBacks.get(callbackName);
            if (method != null) {
                logger.info("Received a callBack:" + callbackName + " executed");
                Object o = beanManager.find(DIContainer.parseClassNameFromClassToString(method.getDeclaringClass().getName()));
                method.invoke(o, args);
            }

        }  catch (Exception e) {
            logger.error(e.toString());
            throw new CallBackMethodExecutionException(e);
        }
    }

    public static Map<String, Method> parseCommands() {
        Class<?> c = CommandController.class;

        return Stream.of(c.getMethods())
                .filter((method -> method.getAnnotation(Command.class) != null))
                .collect(Collectors.toMap(method -> method.getAnnotation(Command.class).value(), Function.identity()));
    }

    public static Map<String, Method> parseCallBack() {
        List<Method> list = new ArrayList<>();
        Stream.of(ProfileCallBackController.class.getMethods()).forEach(list::add);
        Stream.of(MainCallBackController.class.getMethods()).forEach(list::add);
        Stream.of(SettingsCallBackController.class.getMethods()).forEach(list::add);
        Stream.of(MeetingCallBackController.class.getMethods()).forEach(list::add);
        Stream.of(SearchCallBackController.class.getMethods()).forEach(list::add);

        return list.stream()
                .filter((method ->  method.getAnnotation(CallBackFun.class) != null))
                .collect(Collectors.toMap(method -> {
                    String val = method.getAnnotation(CallBackFun.class).value();
                    return val.equals("") ? method.getName() : val;
                }, Function.identity()));

    }



}
