package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class KeepAliveController {

    @GetMapping("/myapp/keepalive")
    public ResponseEntity<String> keepAlive() {
        return ResponseEntity.ok("KEEPALIVE_OK");
    }
}

