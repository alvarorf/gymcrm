package com.gymcrm.core.config;

import com.gymcrm.core.util.DataLoader;
import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.core.util.Nomenclature.Action;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationPostProcessor implements BeanPostProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializationPostProcessor.class);

    private final DataLoader dataLoader;
    private final Environment env;

    public StorageInitializationPostProcessor(@Lazy DataLoader dataLoader, Environment env) {
        this.dataLoader = dataLoader;
        this.env = env;
    }

    /**
     * Intercepts the bean after instantiation (but before init methods like InitializingBean.afterPropertiesSet()).
     * We call loadInitialData on the Storage bean here.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EntityManagerFactory) {
            // Fetch property from Environment at runtime instead of @Value at injection time
            String dataPath = env.getProperty("storage.initial-data-file");

            if (dataPath != null) {
                Nomenclature.info(logger, Action.SEED);
                dataLoader.loadInitialData(dataPath);
                Nomenclature.success(logger, Action.SEED, "Global Load: " + dataPath);
            } else {
                logger.warn("Property 'storage.initial-data-file' not found in current profile.");
            }
        }
        return bean;
    }

    // Return the bean unmodified
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
