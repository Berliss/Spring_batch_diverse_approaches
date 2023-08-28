package com.blueskies.springbatch.controller;

import com.blueskies.springbatch.request.JobParamRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/jobs")
@AllArgsConstructor
public class JobController {

    private JobService jobService;

    @GetMapping("/start")
    public String startJob(
            @RequestParam(required = false, defaultValue = "firstJob") String jobName,
            @RequestBody(required = false) List<JobParamRequest> params
    ) throws Exception {
        System.out.println(params);
        jobService.startJob(jobName, params);
        return "Job started...";
    }

    @GetMapping("stop/{executionId}")
    public String stopJob(@PathVariable long executionId) {
        jobService.stopJob(executionId);
        return "job with execution ID -> " + executionId +" has been stopped";
    }




}
