package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.MonthlyReport;

// Renders a MonthlyReport to .xlsx bytes - kept behind a port so the
// aggregation logic in MonthlyReportUseCase stays free of Apache POI.
public interface ReportWorkbookPort {
    byte[] render(MonthlyReport report);
}
