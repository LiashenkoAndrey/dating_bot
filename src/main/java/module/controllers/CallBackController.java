package module.controllers;

import lombok.NoArgsConstructor;
import module.Bot;
import module.domain.User;
import module.services.UserService;
import module.telegram_utils.MethodExecutor;
import org.open.cdi.BeanManager;
import org.open.cdi.annotations.DIBean;
import org.open.cdi.annotations.InjectBean;

import java.lang.reflect.Method;
import java.util.Map;

@NoArgsConstructor
@DIBean
public class CallBackController {

    @InjectBean
    public Bot bot;

    @InjectBean("currentUser")
    public User user;

    @InjectBean
    public UserService userService;

    @InjectBean
    public MethodExecutor methodExecutor;

    @InjectBean("commandsPool")
    public Map<String, Method> commandsPool;

    @InjectBean
    public ServiceCallBacks serviceCallBacks;

    @InjectBean("BeanManager")
    public BeanManager manager;

}
