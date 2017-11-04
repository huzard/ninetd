package com.nine.td;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GamePaths {
    String FILE_SEP = System.getProperty("file.separator");
    String CSS_DIR  = "css";

    Path ROOT       = Paths.get("").toAbsolutePath();
    Path SRC        = ROOT.resolve("src").resolve("main");
    Path RESOURCES  = SRC.resolve("resources");
    Path MAPS       = RESOURCES.resolve("maps");
    Path SPRITES    = RESOURCES.resolve("sprites");
    Path ENEMIES    = SPRITES.resolve("enemies");
    Path STYLES     = SRC.resolve("java").resolve(TowerDefense.class.getPackage().getName().replace(".", FILE_SEP)).resolve(CSS_DIR);
}
