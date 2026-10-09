package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.MonthlyReportUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.ReportRecipientDto;
import com.tmsbackend.infrastructure.web.dto.ReportSettingsDto;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Settings > Mail Configuration > Monthly Report - gated on its own
// "Monthly Report" permission menu (V5__monthly_report_permission.sql),
// granted to Super Admin only by default. `month` params are "YYYY-MM".
@RestController
@RequestMapping("/tms/api/reports/monthly")
public class MonthlyReportController {
    private static final String MENU = "Monthly Report";

    private final MonthlyReportUseCase monthlyReportUseCase;
    private final CurrentUserResolver currentUserResolver;
    private final PermissionGuard permissionGuard;

    public MonthlyReportController(
            MonthlyReportUseCase monthlyReportUseCase, CurrentUserResolver currentUserResolver, PermissionGuard permissionGuard) {
        this.monthlyReportUseCase = monthlyReportUseCase;
        this.currentUserResolver = currentUserResolver;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping("/settings")
    public ReportSettingsDto getSettings() {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        return ReportSettingsDto.from(monthlyReportUseCase.getSettings());
    }

    @PutMapping("/settings")
    public ReportSettingsDto saveSettings(@Valid @RequestBody ReportSettingsDto request) {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(userId, MENU);
        monthlyReportUseCase.saveSettings(userId, request.toDomain());
        return ReportSettingsDto.from(monthlyReportUseCase.getSettings());
    }

    @GetMapping("/recipients")
    public List<ReportRecipientDto> listRecipients() {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        return monthlyReportUseCase.listRecipients().stream().map(ReportRecipientDto::from).toList();
    }

    @PostMapping("/recipients")
    public ReportRecipientDto saveRecipient(@Valid @RequestBody ReportRecipientDto request) {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(userId, MENU);
        return ReportRecipientDto.from(monthlyReportUseCase.saveRecipient(userId, request.toDomain()));
    }

    @DeleteMapping("/recipients/{id}")
    public void deleteRecipient(@PathVariable String id) {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(userId, MENU);
        monthlyReportUseCase.deleteRecipient(userId, id);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam YearMonth month) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        byte[] workbook = monthlyReportUseCase.renderReport(month);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", MonthlyReportUseCase.fileName(month));
        return ResponseEntity.ok().headers(headers).body(workbook);
    }

    @PostMapping("/send")
    public Map<String, Object> sendNow(@RequestParam YearMonth month) {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(userId, MENU);
        int recipientCount = monthlyReportUseCase.sendNow(userId, month);
        return Map.of("month", month.toString(), "recipientCount", recipientCount);
    }
}
