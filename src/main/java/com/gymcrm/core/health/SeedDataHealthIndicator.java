package com.gymcrm.core.health;

import com.gymcrm.core.util.Nomenclature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SeedDataHealthIndicator implements HealthIndicator {

    @Value("${storage.initial-data-file}")
    private String dataPath;

    @Override
    public Health health() {
        ClassPathResource resource = new ClassPathResource(dataPath);

        if (resource.exists()) {
            return Health.up()
                    .withDetail("file", dataPath)
                    .withDetail(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.MSG.HEALTH_UP)
                    .build();
        }

        return Health.down()
                .withDetail("file", dataPath)
                .withDetail(Nomenclature.ERR.KEY_ERR_TYPE, Nomenclature.MSG.HEALTH_DOWN)
                .build();
    }
}