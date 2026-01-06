package com.gymcrm.config;

import com.gymcrm.util.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationPostProcessor implements BeanPostProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializationPostProcessor.class);

    // Inject the path from property file.
    // Spring reads application.properties, finds the key, and sets the String value (dataPath) here,
    // to be equal to storage.initial-data-file
    @Value("${storage.initial-data-file}")
    private String dataPath;

    @Autowired
    private final DataLoader dataLoader;

    @Autowired
    public StorageInitializationPostProcessor(@Lazy DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Intercepts the bean after instantiation (but before init methods like InitializingBean.afterPropertiesSet()).
     * We call loadInitialData on the Storage bean here.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Intercept the EntityManagerFactory to trigger DB seeding (after the EntityManager is ready)
        if (bean instanceof EntityManagerFactory) {
            logger.info("Bean-PostProc LOG: EntityManagerFactory initialized. Triggering global data load.");
            dataLoader.loadInitialData(dataPath);
        }

        return bean;
    }

    // Return the bean unmodified
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
