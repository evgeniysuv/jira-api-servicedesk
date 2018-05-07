package config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by esuv on 4/29/18
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    public ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext ac) throws BeansException {
        context = ac;
    }
}
