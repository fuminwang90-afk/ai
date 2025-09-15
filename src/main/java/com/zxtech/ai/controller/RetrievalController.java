package com.zxtech.ai.controller;

import com.zxtech.ai.service.RetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/retrieval")
public class RetrievalController {
    @Autowired
    private RetrievalService retrievalService;

    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String question) {
        try {
            String answer = retrievalService.answerQuestion(question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("出错：" + e.getMessage());
        }
    }
}
