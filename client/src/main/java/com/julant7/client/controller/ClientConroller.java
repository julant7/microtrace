package com.julant7.client.controller;

import com.julant7.client.dto.InsertLogRequestDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class ClientConroller {
    @PostMapping("/")
    ResponseEntity<?> insertLog(@RequestBody InsertLogRequestDto insertLogRequestDto) {
        log.warn("HELLOOO");
        // clientService.insertLog(insertLogRequestDto);
        return ResponseEntity.ok().build();
    }
}
