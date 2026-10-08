package com.tmsbackend.domain.model;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

// A month of readings bucketed into fixed 30-minute slots (00:00, 00:30, ...
// in the report's own time zone), one section per device. Every slot of the
// month is present - a slot with no readings has sampleCount 0 and null
// values, so gaps in data collection are visible in the report rather than
// silently skipped.
public record MonthlyReport(YearMonth month, ZoneId zone, List<DeviceSection> devices) {

    public record DeviceSection(String transformerName, Device device, List<Slot> slots) {
        public long slotsWithData() {
            return slots.stream().filter(s -> s.sampleCount() > 0).count();
        }
    }

    // avg/max are over every reading in [start, start + 30min). Setpoints and
    // tap position are discrete values, so those carry the slot's last
    // reading rather than an average.
    public record Slot(
            LocalDateTime start,
            int sampleCount,
            Double otiAvg,
            Double otiMax,
            Double wtiAvg,
            Double wtiMax,
            Double tapPosition,
            Double ptVoltageAvg,
            Double setVoltage,
            Double otiAlarmSetpoint,
            Double otiTripSetpoint,
            Double wtiAlarmSetpoint,
            Double wtiTripSetpoint) {
    }
}
