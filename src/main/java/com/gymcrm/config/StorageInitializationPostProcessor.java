package com.gymcrm.config;

import com.gymcrm.storage.Storage;
import com.gymcrm.util.DataLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationPostProcessor implements BeanPostProcessor
{
    // Inject the path from property file.
    // Spring reads application.properties, finds the key, and sets the String value (dataPath) here,
    // to be equal to storage.initial-data-file
    @Value("${storage.initial-data-file}")
    private String dataPath;

    @Autowired
    private DataLoader dataLoader;

    /**
     * Intercepts the bean after instantiation (but before init methods like InitializingBean.afterPropertiesSet()).
     * We call loadInitialData on the Storage bean here.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Identify the target bean (Storage)
        if (bean instanceof Storage) {
            Storage storage = (Storage) bean;

            // Call the required method with the injected property path
            System.out.println("Bean-PostProc LOG: Intercepted storage bean for initialization.");
            dataLoader.loadInitialData(storage, dataPath);
        }

        return bean;
    }


    // Return the bean unmodified
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
