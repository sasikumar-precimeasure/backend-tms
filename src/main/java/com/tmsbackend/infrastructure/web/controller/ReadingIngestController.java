package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.RecordReadingBatchUseCase;
import com.tmsbackend.infrastructure.web.dto.ReadingBatchRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// One call per push interval (60s by default) from the Modbus gateway
// service - authenticated with its ingest key (IngestKeyAuthenticationFilter)
// - covering every configured device (see RecordReadingBatchUseCase for the
// upsert-and-insert logic). A logged-in user's JWT is still accepted too.
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
