package ar.uba.fi.recursos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.uba.fi.recursos.exceptions.InvalidHourDetailHoursException;
import ar.uba.fi.recursos.model.TimeRegister;
import ar.uba.fi.recursos.model.TimeRegisterTypeOfActivity;
import ar.uba.fi.recursos.repository.TimeRegisterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class TimeRegisterService {

    @Autowired
    private TimeRegisterRepository timeRegisterRepository;
    // @Autowired private ConceptRepository conceptRepository;

    public boolean verifyActivity(Long activityId, TimeRegisterTypeOfActivity typeOfActivity) {
        if(typeOfActivity == TimeRegisterTypeOfActivity.TASK){
            String url = "https://squad2-2022-2c.herokuapp.com/api/v1/tasks/" + activityId;
            RestTemplate restTemplate = new RestTemplate();
            TaskData task_raw = restTemplate.getForObject(url, TaskData.class);
            if (task_raw == null) {
                return false;
            }
            return true;

        } else if(typeOfActivity == TimeRegisterTypeOfActivity.CONCEPT){
            if(conceptRepository.findById(activityId).isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public ResponseEntity<Object> verifyTimeRegister(TimeRegister timeRegister) {
        if(timeRegister.getHours()<=0 && timeRegister.getHours()>24){
            return ResponseEntity.badRequest().body("Las horas del parte no pueden ser negativas o mayores a 24: " + timeRegister.getHours());
        }
        if (!verifyActivity(timeRegister.getActivityId(), timeRegister.getTypeOfActivity())) {
            return ResponseEntity.badRequest().body("Activity is not valid for this hour detail");
        }
        
        if (timeRegisterRepository.existsTimeRegisterByDateAndActivityIdAndTypeOfActivity(timeRegister.getDate(), timeRegister.getActivityId(), timeRegister.getTypeOfActivity())) {
            return ResponseEntity.badRequest().body("Time Register with given date and activity already exists");
        }
        return ResponseEntity.ok().build();

    }

    public List<TimeRegister> findAll() {
        return timeRegisterRepository.findAll();
    }

    public boolean existsTimeRegisterByDateAndActivityIdAndTypeOfActivity(LocalDate date, Long activityId,
            TimeRegisterTypeOfActivity typeOfActivity) {
        return timeRegisterRepository.existsTimeRegisterByDateAndActivityIdAndTypeOfActivity(date, activityId,
                typeOfActivity);
    }

    public Optional<TimeRegister> findById(Long timeRegisterId) {
        return timeRegisterRepository.findById(timeRegisterId);
    }

    public void deleteById(Long timeRegisterId) {
        timeRegisterRepository.deleteById(timeRegisterId);
    }

    public List<TimeRegister> findTimeRegistersByDateBetweenAndHourDetail_WorkerIdOrderByDateAsc(LocalDate minDate,
            LocalDate maxDate, Long workerId) {
        return timeRegisterRepository.findTimeRegistersByDateBetweenAndHourDetail_WorkerIdOrderByDateAsc(minDate,
                maxDate, workerId);
    }
}