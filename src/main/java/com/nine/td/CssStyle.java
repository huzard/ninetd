package com.nine.td;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CssStyle {
    private final String name;
    private final File[] files;

    public CssStyle(String name, File[] files) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "invalid css style name");
        Preconditions.checkArgument(files != null && files.length >= 1, "invalid css style files");

        this.name = name;
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public File[] getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, String.join(", ", Stream.of(this.files).map(File::getName).collect(Collectors.toList())));
    }
}
