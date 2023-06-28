package ru.nightmirror.wlbytime.common.covertors.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Builder
public class TimeUnitsConvertorSettings {
    List<String> year;
    List<String> month;
    List<String> week;
    List<String> day;
    List<String> hour;
    List<String> minute;
    List<String> second;
    String forever;
}
