package com.nine.td;

import java.util.regex.Pattern;

public interface GameConstants {
    int REQUIRED_SIZE               = 24;
    String MAP_DEFINITION           = "definition.txt";
    String WAVES_DEFINITION         = "waves.txt";
    String PROPERTIES_DEFINITION    = "properties.txt";
    String DATA_SEPARATOR           = ":";
    String LIST_SEPARATOR           = ",";
    String PATH_PREFIX              = String.format("path%s", DATA_SEPARATOR);
    String PATH_PATTERN             = String.format("\\d+%s\\d+%s[NESW]", DATA_SEPARATOR, DATA_SEPARATOR);
    String WAVE_PATTERN             = String.format("\\d+%s(?i)(.+)%s\\d+%s\\d+%s\\d+", DATA_SEPARATOR, DATA_SEPARATOR, DATA_SEPARATOR, DATA_SEPARATOR);
    Pattern PATH_LIST_PATTERN       = Pattern.compile(String.format("%s%s%s(%s%s)*", PATH_PATTERN, LIST_SEPARATOR, PATH_PATTERN, LIST_SEPARATOR, PATH_PATTERN));
    Pattern WAVE_LIST_PATTERN       = Pattern.compile(String.format("%s(%s%s)*", WAVE_PATTERN, LIST_SEPARATOR, WAVE_PATTERN));
}
