package com.gymcrm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymcrm.model.*;
import com.gymcrm.repositories.*;
import lombok.Data;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/*
    Req3: Implement the ability to initialize storage with some prepared data from the file
     during the application start (use spring bean post-processing features).
     Path to the concrete file should be set using property placeholder and external property file.
*/

@Component
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private final ObjectMapper objectMapper;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final PasswordEncoder passwordEncoder; // TODO: Why do we get this warning? Private field 'passwordEncoder' is never assigned. Do we need to address it? We are using spring security

    @Autowired
    public DataLoader(TraineeRepository traineeRepository,
                      TrainerRepository trainerRepository,
                      TrainingRepository trainingRepository,
                      TrainingTypeRepository trainingTypeRepository, PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Nomenclature.info(logger, Nomenclature.Action.INITIALIZE);    }

    @Transactional
    public void loadInitialData(String dataPath) {
        try {
            Nomenclature.info(logger, Nomenclature.Action.SEED, dataPath);
            ClassPathResource resource = new ClassPathResource(dataPath);

            try (InputStream inputStream = resource.getInputStream()) {
                DataWrapper data = objectMapper.readValue(inputStream, DataWrapper.class);


                // Save training types first (constants)
                if (data.getTrainingTypes() != null) {
                    trainingTypeRepository.saveAll(data.getTrainingTypes());
                    Nomenclature.success(logger, Nomenclature.Action.SEED, "Training Types");
                }

                // Save trainers (they now have valid specialization IDs to point to)
                if (data.getTrainers() != null) {
                    data.getTrainers().forEach(t -> t.setPassword(passwordEncoder.encode(t.getPassword())));
                    trainerRepository.saveAll(data.getTrainers());
                    Nomenclature.success(logger, Nomenclature.Action.SEED, "Trainers");
                }


                // TODO: The following section within the try block, needs more coverage
                // 3. Save trainees
                if (data.getTrainees() != null) {
                    data.getTrainees().forEach(t -> {
                        // ENCODE the plain text password from JSON before saving to MySQL
                        t.setPassword(passwordEncoder.encode(t.getPassword()));
                    });
                    traineeRepository.saveAll(data.getTrainees());
                    Nomenclature.success(logger, Nomenclature.Action.SEED, "Trainees");
                }

                // 4. Save Trainings (must happen last because they refer to Trainees/Trainers)
                if (data.getTrainings() != null) {
                    trainingRepository.saveAll(data.getTrainings());
                    Nomenclature.success(logger, Nomenclature.Action.SEED, data.getTrainings().size() + " Trainings");                }

            }
        }
        catch (Exception e)
        {
            Nomenclature.warn(logger, Nomenclature.Action.SEED, dataPath);
        }
    }

    @Data
    private static class DataWrapper {
        private List<TrainingType> trainingTypes;
        private List<Trainer> trainers;
        private List<Trainee> trainees;
        private List<Training> trainings;
    }
}