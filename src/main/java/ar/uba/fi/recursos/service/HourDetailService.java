package ar.uba.fi.recursos.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ar.uba.fi.recursos.dtos.TaskData;
import ar.uba.fi.recursos.exceptions.InvalidDatesException;
import ar.uba.fi.recursos.exceptions.InvalidTypeException;
import ar.uba.fi.recursos.model.HourDetail;
import ar.uba.fi.recursos.model.HourDetailStatus;
import ar.uba.fi.recursos.model.TimeRegister;
import ar.uba.fi.recursos.model.TimeRegisterTypeOfActivity;
import ar.uba.fi.recursos.repository.HourDetailRepository;

@Service
public class HourDetailService {

    @Autowired
    private HourDetailRepository hourDetailRepository;
    @Autowired
    private ResourceService resourceService;

    public HourDetail createHourDetail(HourDetail hourDetail) throws Throwable {

        // fix: manejar errores sin tirar excepciones

        if (!hasValidPeriod(hourDetail)) {
            throw new InvalidDatesException("Las fechas del parte no son validas o se superponen con unas existentes");
        }

        if (!resourceService.findById(hourDetail.getWorkerId()).isPresent()) {
            throw new InvalidTypeException("El recurso no existe");
        }

        hourDetail.setStatus(HourDetailStatus.DRAFT);
        hourDetail.setTimeRegisters(new ArrayList<TimeRegister>());
        return hourDetailRepository.save(hourDetail);
    }

    public boolean hasValidPeriod(HourDetail hourDetail) {
        Date startDate = Date.from(hourDetail.getStartTime().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        switch (hourDetail.getType()) {
            case SEMANAL: {
                if (!((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY))) {
                    return false;
                    // throw new InvalidDatesException("La fecha especificada no es un lunes: " +
                    // startDate);
                }
                calendar.add(Calendar.DAY_OF_MONTH, 6);
                hourDetail.setEndTime(calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                break;
            }
            case QUINCENAL: {
                if (!((calendar.get(Calendar.DAY_OF_MONTH) == 1)) && !((calendar.get(Calendar.DAY_OF_MONTH) == 16))) {
                    return false;
                    // throw new InvalidDatesException("La fecha especificada no es un 1 o 16 del
                    // mes: " + startDate);
                }

                if ((calendar.get(Calendar.DAY_OF_MONTH) == 1)) {
                    calendar.add(Calendar.DAY_OF_MONTH, 14);
                    hourDetail.setEndTime(calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    break;
                }
                int max_day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, max_day);
                hourDetail.setEndTime(calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                break;
            }
            case MENSUAL: {

                if (!((calendar.get(Calendar.DAY_OF_MONTH) == 1))) {
                    return false;
                    // throw new InvalidDatesException("La fecha especificada no es el primer día
                    // del mes: " + startDate);
                }
                int max_day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, max_day);
                hourDetail.setEndTime(calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                break;
            }
            default: {
                return false;
                // throw new InvalidTypeException("El tipo especificado es inválido: " +
                // hourDetail.getType());
            }
        }

        // this.hourDetailRepository.findByWorkerId(hourDetail.getWorkerId()).stream().forEach(hd
        // -> {
        // if(hd.getStartTime().isBefore(hourDetail.getEndTime()) &&
        // hd.getEndTime().isAfter(hourDetail.getStartTime())){ // si se solapan
        // throw new OverlappingDatesException("Las horas se solapan con el parte: " +
        // hd.getId());
        // }
        // });
        return true;
    }

    public HourDetail save(HourDetail hourDetail) {
        return hourDetailRepository.save(hourDetail);
    }

    public Double getTotalProjectHours(Long projectId) {
        String url = "https://squad2-2022-2c.herokuapp.com/api/v1/projects/" + projectId + "/tasks";
        RestTemplate restTemplate = new RestTemplate();

        TaskData[] tasks_raw = restTemplate.getForObject(url, TaskData[].class);
        if (tasks_raw == null) {
            return 0D;
        }

        List<Long> tasks = Arrays.asList(tasks_raw).stream().map(task -> task.getId()).toList();

        Double totalHours = 0D;
        for (HourDetail hd : this.hourDetailRepository.findAll()) {
            for (TimeRegister tr : hd.getTimeRegisters()) {
                if (tr.getTypeOfActivity() == TimeRegisterTypeOfActivity.TASK) {
                    if (tasks.contains(tr.getActivityId())) {
                        totalHours += tr.getHours();
                    }
                }
            }
        }

        return totalHours;
    }

    public Optional<HourDetail> findById(Long id) {
        return hourDetailRepository.findById(id);
    }

    public List<HourDetail> findByWorkerId(Long workerId) {
        return hourDetailRepository.findByWorkerId(workerId);
    }

    public List<HourDetail> findAll() {
        return hourDetailRepository.findAll();
    }

    public void deleteById(Long id) {
        hourDetailRepository.deleteById(id);
    }
}
