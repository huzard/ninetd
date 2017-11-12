package com.nine.td;

import java.util.regex.Pattern;

public interface GameConstants {
    double ANIMATION_TARGET_SPEED   = 40d;
    double ANIMATION_WAYPOINT_SPEED = 250d;
    double START_WAVE_BASIC_SPEED   = 500d;
    int REQUIRED_SIZE               = 24;

    String DEFAULT_STYLE            = "Par défaut";
    String MAP_DEFINITION           = "setup.txt";
    String PROPERTIES_DEFINITION    = "properties.properties";
    String DATA_SEPARATOR           = ":";
    String LIST_SEPARATOR           = ",";
    String PATH_PREFIX              = String.format("path%s", DATA_SEPARATOR);
    String WAVE_PREFIX              = String.format("wave%s", DATA_SEPARATOR);
    String PATH_PATTERN             = String.format("\\d+%s\\d+%s[NESW]", DATA_SEPARATOR, DATA_SEPARATOR);
    String WAVE_PATTERN             = String.format("\\d+%s(?i)(.+)%s\\d+%s\\d+%s\\d+", DATA_SEPARATOR, DATA_SEPARATOR, DATA_SEPARATOR, DATA_SEPARATOR);
    Pattern PATH_LIST_PATTERN       = Pattern.compile(String.format("%s%s(%s%s)+%s", PATH_PATTERN, LIST_SEPARATOR, PATH_PATTERN, LIST_SEPARATOR, PATH_PATTERN));
    Pattern WAVE_LIST_PATTERN       = Pattern.compile(String.format("%s(%s%s)*", WAVE_PATTERN, LIST_SEPARATOR, WAVE_PATTERN));

    char WALL_TILE                  = '#';
    char PATH_TILE                  = '.';

    char NORTH_DIR                  = 'N';
    char EAST_DIR                   = 'E';
    char SOUTH_DIR                  = 'S';
    char WEST_DIR                   = 'W';
}
