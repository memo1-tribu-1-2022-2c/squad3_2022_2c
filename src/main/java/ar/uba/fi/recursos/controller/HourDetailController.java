package ar.uba.fi.recursos.controller;

import ar.uba.fi.recursos.model.HourDetail;
import ar.uba.fi.recursos.model.TimeRegister;
import ar.uba.fi.recursos.repository.HourDetailRepository;
import ar.uba.fi.recursos.service.HourDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping(path = "/hourDetail")
@EnableSwagger2
public class HourDetailController {

    @Autowired
    private HourDetailService hourDetailService;

    @Autowired
    private HourDetailRepository hourDetailRepository;
    
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)	
    public List<HourDetail> getAllHourDetails(@RequestParam(required = false) Long workerId) {
        if (workerId != null) {
            return this.hourDetailRepository.findByWorkerId(workerId);
        }
        return this.hourDetailRepository.findAll();
    }

    @PostMapping(path = "")
    public HourDetail createHourDetail(@RequestBody HourDetail hourDetail) throws Throwable{
        return hourDetailService.createHourDetail(hourDetail);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<HourDetail> getHourDetail(@PathVariable Long id) {
        return ResponseEntity.of(hourDetailRepository.findById(id));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Object> modifyHourDetailData(@RequestBody HourDetail hourDetail, @PathVariable Long id){
        Optional<HourDetail> hourDetailOptional = hourDetailRepository.findById(id);

        if (!hourDetailOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        //chequeos

        if (!hourDetailService.verifyDates(hourDetail)) {
            return ResponseEntity.badRequest().body("Dates are not valid or overlaps with other hour details");
        }
        
        hourDetail.setId(id);
        return ResponseEntity.ok(hourDetailService.save(hourDetail));
    }

    // Put mapping to add a new TimeRegister to an existing HourDetail
    // @PutMapping(path = "/{id}/timeRegister")
    // public ResponseEntity<Object> addTimeRegister(@RequestBody TimeRegister timeRegister, @PathVariable Long id){
    //     Optional<HourDetail> hourDetailOptional = hourDetailRepository.findById(id);
        
    //     if (!hourDetailOptional.isPresent()) {
    //         System.out.println("HourDetail not found");
    //         return ResponseEntity.notFound().build();
    //     }
    //     HourDetail hourDetail = hourDetailOptional.get();
    //     hourDetail.addTimeRegister(timeRegister);
    //     hourDetailService.save(hourDetail);
    //     return ResponseEntity.ok().build();
    // }

    @DeleteMapping(path = "/{id}")
    public void deleteHourDetail(@PathVariable Long id) {
        hourDetailRepository.deleteById(id);
    }

    // @DeleteMapping(path = "/{id}/timeRegister/{timeRegisterId}")
    // public ResponseEntity<Object> deleteTimeRegister(@PathVariable Long id, @PathVariable Long timeRegisterId) {
    //     Optional<HourDetail> hourDetailOptional = hourDetailRepository.findById(id);
    //     if (!hourDetailOptional.isPresent()) {
    //         return ResponseEntity.notFound().build();
    //     }
    //     HourDetail hourDetail = hourDetailOptional.get();
    //     hourDetail.removeTimeRegister(timeRegisterId);
    //     hourDetailService.save(hourDetail);
    //     return ResponseEntity.ok().build();
    // }

    // /totalProjectHours/<projectId>
    @GetMapping(path = "/totalProjectHours/{projectId}")
    public ResponseEntity<Integer> getTotalProjectHours(@PathVariable Long projectId) {
        return ResponseEntity.ok(hourDetailService.getTotalProjectHours(projectId));
    }
    
}





// hourDetail
// {
//     "workerId": 1,
//     "startTime":"2017-01-19", 
//     "endTime":"2018-01-19",
//     "status":"BORRADOR",
// }
