package module.telegram_utils;

import lombok.NoArgsConstructor;
import module.controllers.MainController;
import org.open.cdi.BeanManager;
import org.open.cdi.DIContainer;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@DIBean
@NoArgsConstructor
public class MethodExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MethodExecutor.class);

    @InjectBean
    public MainController mainController;

    @InjectBean
    public BeanManager beanManager;

    public void invokeCommand(Method method) {
        try {
            if (method != null) {
                logger.info("Received a command: " + method.getName() +" executed" );
                method.invoke(mainController);
            } else {
                logger.error("Method id not present");
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Can't invoke a method with name: " + method.getName() +", " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void invokeCallBack(Method method, BotApiObject query)  {
        try {
            if (method != null) {

                logger.info("Received a callBack:" + method.getName() +" executed");
                String className = method.getDeclaringClass().getName();

                Object o = beanManager.find(DIContainer.parseClassNameFromClassToString(className));
                method.invoke(o, query);
            } else {
                logger.error("CallBack method id not present");
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Can't invoke a callBack method with name: " + method.getName() +", " + e.getMessage());
        }
    }

}
