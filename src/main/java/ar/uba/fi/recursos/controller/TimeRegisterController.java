package ar.uba.fi.recursos.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.uba.fi.recursos.model.HourDetail;
import ar.uba.fi.recursos.model.TimeRegister;
import ar.uba.fi.recursos.service.HourDetailService;
import ar.uba.fi.recursos.service.TimeRegisterService;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Validated
@RestController
@RequestMapping(path = "/timeRegisters")
@EnableSwagger2
public class TimeRegisterController {

    @Autowired
    private TimeRegisterService timeRegisterService;
    @Autowired
    private HourDetailService hourDetailService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TimeRegister> getAllTimeRegisters() {
        return timeRegisterService.findAll();
    }

    @PostMapping(path = "")
    public ResponseEntity<Object> createTimeRegister(@RequestBody TimeRegister timeRegister) {

        Long hourDetailId = timeRegister.getHourDetailId();
        Optional<HourDetail> hourDetail = hourDetailService.findById(hourDetailId);
        if (hourDetail.isEmpty()) {
            return ResponseEntity.badRequest().body("HourDetail with id " + hourDetailId + " does not exist");
        }

        // check if activity is valid
        if (!timeRegisterService.isValidActivity(timeRegister.getActivityId(), timeRegister.getTypeOfActivity())) {
            return ResponseEntity.badRequest().body("Activity is not valid for this hour detail");
        }

        HourDetail existingHourDetail = hourDetail.get();

        if (timeRegisterService.existsTimeRegisterByDateAndActivityIdAndTypeOfActivity(timeRegister.getDate(),
                timeRegister.getActivityId(), timeRegister.getTypeOfActivity())) {
            return ResponseEntity.badRequest().body("Time Register with given date and activity already exists");
        }

        existingHourDetail.addTimeRegister(timeRegister);
        hourDetailService.save(existingHourDetail);

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/{timeRegisterId}")
    public ResponseEntity<Object> modifyTimeRegister(@RequestBody TimeRegister timeRegister,
            @PathVariable Long timeRegisterId) {
        // TODO checkeos
        Optional<TimeRegister> timeRegisterOptional = timeRegisterService.findById(timeRegisterId);

        if (timeRegisterOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Time Register with id " + timeRegisterId + " does not exist");
        }

        timeRegister.setId(timeRegisterId);

        // hay que chequear q sean validos los otros atributos del TimeRegister

        return ResponseEntity.ok(timeRegisterService.save(timeRegister));
    }

    @DeleteMapping(path = "/{timeRegisterId}")
    public ResponseEntity<Object> deleteTimeRegister(@PathVariable Long timeRegisterId) {
        Optional<TimeRegister> timeRegisterOptional = timeRegisterService.findById(timeRegisterId);

        if (timeRegisterOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Time Register with id " + timeRegisterId + " does not exist");
        }

        TimeRegister existingTimeRegister = timeRegisterOptional.get();

        // hour detail deletes the time register
        Long hourDetailId = existingTimeRegister.getHourDetailId();
        Optional<HourDetail> hourDetailOptional = hourDetailService.findById(hourDetailId);

        if (hourDetailOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Hour detail with id " + hourDetailId + "does not exist");
        }

        HourDetail hourDetail = hourDetailOptional.get();
        hourDetail.removeTimeRegister(existingTimeRegister);
        hourDetailService.save(hourDetail);
        timeRegisterService.deleteById(timeRegisterId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TimeRegister> getTimeRegisterReport(@RequestParam Long workerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate minDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate maxDate) {
        return timeRegisterService.findTimeRegistersByDateBetweenAndHourDetail_WorkerIdOrderByDateAsc(minDate,
                maxDate, workerId);
    }

}

// hourDetail
// {
// "workerId": 1,
// "startTime":"2017-01-19",
// "endTime":"2018-01-19",
// "status":"BORRADOR",
// "hours": "24"
// }

// timeRegister
// {
// "typeOfActivity":"TASK",
// "activityId":"121",
// "hours":"8",
// "dates": ["2020-01-01", "2020-01-02"]
// }
