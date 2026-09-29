package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.RecordReadingBatchUseCase;
import com.tmsbackend.infrastructure.web.dto.ReadingBatchRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// One call every 60s from the frontend, covering every currently-visible
// device (see RecordReadingBatchUseCase for the upsert-and-insert logic).
@RestController
@RequestMapping("/tms/api/readings")
public class ReadingIngestController {
    private final RecordReadingBatchUseCase recordReadingBatchUseCase;

    public ReadingIngestController(RecordReadingBatchUseCase recordReadingBatchUseCase) {
        this.recordReadingBatchUseCase = recordReadingBatchUseCase;
    }

    @PostMapping("/batch")
    public void batch(@RequestBody ReadingBatchRequestDto request) {
        recordReadingBatchUseCase.execute(request.toDomain());
    }
}
