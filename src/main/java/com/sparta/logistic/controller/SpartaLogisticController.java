package com.sparta.logistic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistic")
public class SpartaLogisticController {

    @GetMapping("/info")
    public ResponseEntity<String> getLogisticInfo() {
        return ResponseEntity.ok("Sparta Logistic Information");
    }
}
