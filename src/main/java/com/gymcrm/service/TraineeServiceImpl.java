package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.UsernamePasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/


/*
From: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Service.html
@Service:
Indicates that an annotated class is a "Service", originally defined by Domain-Driven Design (Evans, 2003)
as "an operation offered as an interface that stands alone in the model, with no encapsulated state."

It is a specialization (implementation) of @Component and allows TraineeServiceImpl to be autodetected through classpath scanning.

 */
@Service  // Could also be @Component
public class TraineeServiceImpl implements TraineeService {
    // Why final? Because we want a singleton for both
    private final TraineeDao traineeDao;
    private final UsernamePasswordGenerator generator;

    // Constructor-based injection
    public TraineeServiceImpl(TraineeDao traineeDao, UsernamePasswordGenerator generator)
    {
        this.traineeDao = traineeDao;
        this.generator = generator;
    }

    @Override
    public Trainee createProfile(Trainee trainee)
    {
        String username = generator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = generator.generatePassword();


        trainee.setUsername(username);
        trainee.setPassword(password);

        trainee.setActive(true);

        return traineeDao.save(trainee);
    }

    @Override
    public Trainee updateProfile(Trainee trainee)
    {
        return traineeDao.save(trainee);
    }

    @Override
    public Optional<Trainee> selectProfile(Long id)
    {
        return traineeDao.findById(id);
    }

    @Override
    public void deleteProfile(Long id)
    {
        traineeDao.delete(id);
    }


}
