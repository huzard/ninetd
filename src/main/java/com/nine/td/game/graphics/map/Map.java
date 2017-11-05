package com.nine.td.game.graphics.map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.nine.td.GamePaths;
import com.nine.td.game.HasRendering;
import com.nine.td.game.path.Wave;
import com.nine.td.game.graphics.Components;
import com.nine.td.game.graphics.GraphicComponent;
import com.nine.td.game.path.Direction;
import com.nine.td.game.path.Position;
import com.nine.td.game.path.WayPoint;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.playable.Target;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.nine.td.GameConstants.*;

public final class Map implements HasRendering, Engine {
    private Node root;

    private char[][] grid;
    private List<Wave> waves;

    private final String name;
    private final Scale scale;
    private final Properties metaProperties;

    private Canvas targetsCanvas;

    private AnimationTimer animationTimer;

    private Map(String name, Scale scale) {
        this.scale = scale;
        this.name = name;
        this.metaProperties = new Properties();
    }

    @Override
    public void start() {
        this.getCurrentWave().ifPresent(wave -> {
            wave.start();
            animationTimer.start();
        });
    }

    @Override
    public void stop() {
        this.waves.forEach(Engine::stop);
        if(this.animationTimer != null) {
            this.animationTimer.stop();
        }
    }

    @Override
    public void pause() {
        this.waves.forEach(Engine::pause);
        this.animationTimer.stop();
    }

    @Override
    public Node render() {
        if (this.root == null) {
            int rows = this.grid.length;
            int columns = this.grid[0].length;

            double canvasWidth = REQUIRED_SIZE * this.scale.getX() * columns;
            double canvasHeight = REQUIRED_SIZE * this.scale.getY() * rows;

            Canvas canvas = new Canvas(canvasWidth, canvasHeight);
            this.targetsCanvas = new Canvas(canvasWidth, canvasHeight);

            IntStream
                    .range(0, rows)
                    .forEach(i -> IntStream.range(0, columns).forEach(j -> {
                        GraphicComponent component = Components.get(
                                this.grid[i][j],
                                new Position(j * (canvas.getWidth() / columns), i * (canvas.getHeight() / rows))
                        );

                        canvas
                                .getGraphicsContext2D()
                                .drawImage(
                                        component.draw(this.scale),
                                        component.getPosition().getX(),
                                        component.getPosition().getY()
                                );
                    }));

            this.root = new Group(canvas, this.targetsCanvas);
        }

        return this.root;
    }

    public Map reload() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(this.name), "bad filename");
        Preconditions.checkArgument(scale != null, "null scale");

        Path pathMap = GamePaths.MAPS.resolve(this.name);

        try {
            //general compute
            List<String> matrix = Files
                    .lines(Objects.requireNonNull(pathMap.resolve(MAP_DEFINITION), "null map definition path"))
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            Preconditions.checkState(matrix.size() > 1, "bad map definition");

            java.util.Map<Boolean, List<String>> partition = matrix.stream().collect(Collectors.partitioningBy(line -> line.startsWith(PATH_PREFIX)));

            List<String> mapDefinition = partition.get(false);

            List<String> pathsDefinition = partition.get(true)
                    .stream()
                    .map(path -> path.substring(PATH_PREFIX.length()))
                    .map(line -> line.replace(" ", ""))
                    .collect(Collectors.toList());

            List<String> wavesDefinition = Files
                    .lines(Objects.requireNonNull(pathMap.resolve(WAVES_DEFINITION), "null waves definition path"))
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.replace("\\s+", ""))
                    .collect(Collectors.toList());

            //check map data validity
            Preconditions.checkState(mapDefinition.size() > 1, "bad map matrix");
            Preconditions.checkState(pathsDefinition.size() >= 1, "bad paths definition");
            Preconditions.checkState(wavesDefinition.size() >= 1, "bad waves definition");
            Preconditions.checkState(mapDefinition.stream().allMatch(row -> row.length() == matrix.get(0).length()), "bad map definition");
            Preconditions.checkState(pathsDefinition.stream().allMatch(PATH_LIST_PATTERN.asPredicate()), "bad paths definition [format = x:y:direction (N, E, S, W), ...");
            Preconditions.checkState(wavesDefinition.stream().allMatch(WAVE_LIST_PATTERN.asPredicate()), "bad waves definition [format = how much:img_name:life:shield:speed, ...]");

            //Map definition
            this.grid = mapDefinition.stream().map(String::toCharArray).toArray(char[][]::new);

            //waves compute
            this.waves = parseWaves(wavesDefinition, parsePaths(pathsDefinition, this.scale, this.grid));

            this.animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    targetsCanvas.getGraphicsContext2D().clearRect(0, 0, targetsCanvas.getWidth(), targetsCanvas.getHeight());

                    Optional<Wave> maybeWave = getCurrentWave();

                    maybeWave
                            .ifPresent(wave -> {
                                wave
                                        .get()
                                        .stream()
                                        .filter(target -> target.getPosition() != null)
                                        .forEach(target -> targetsCanvas
                                                .getGraphicsContext2D()
                                                .drawImage(
                                                        target.draw(scale),
                                                        target.getPosition().getX(),
                                                        target.getPosition().getY()
                                                )
                                        );

                                if (wave.isEnded()) {
                                    Map.this.stop();
                                }
                            });
                }
            };

            return this.loadMeta(pathMap);
        } catch(IOException e) {
            throw new RuntimeException("Error loading map " + this.name + " : " + e.getMessage());
        }
    }

    public static Map load(String fileName, Scale scale) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "bad filename");
        Preconditions.checkArgument(scale != null, "null scale");

        return new Map(fileName, scale).reload();
    }

    public static Map load(String fileName) throws IOException {
        return load(fileName, new Scale(1.0, 1.0));
    }

    private Map loadMeta(Path pathMap) {
        File propertiesFile = pathMap.resolve(PROPERTIES_DEFINITION).toFile();

        if(propertiesFile.exists()) {
            try {
                this.metaProperties.load(new FileReader(propertiesFile));
            } catch (IOException e) {
                System.err.println("error loading meta file " + propertiesFile.getName());
            }
        }

        return this;
    }

    private Optional<Wave> getCurrentWave() {
        return this.waves.isEmpty() ? Optional.empty() : Optional.of(this.waves.get(0));
    }

    private static List<Wave> parseWaves(List<String> wavesDefinition, List<com.nine.td.game.path.Path> paths) {
        Function<String, List<Target>> toTarget = string -> {
            //format = count:imgName:life:shield:speed
            String[] data = string.split(DATA_SEPARATOR);

            int count       = Integer.valueOf(data[0]);
            String imgName  = data[1];
            int life        = Integer.valueOf(data[2]);
            int shield      = Integer.valueOf(data[3]);
            int speed       = Integer.valueOf(data[4]);

            Preconditions.checkState(count > 0,     "require at least 1 target (found " + count + ")");
            Preconditions.checkState(life > 0,      "require life > 0 (found " + life + ")");
            Preconditions.checkState(shield > 0,    "require shield > 0 (found " + shield + ")");
            Preconditions.checkState(speed > 0,     "require speed > 0 (found " + speed + ")");

            return Stream
                    .generate(() -> new Target(() -> GamePaths.ENEMIES.resolve(imgName), life, shield, speed))
                    .limit(count)
                    .collect(Collectors.toList());
        };

        return wavesDefinition.stream().map(wave -> {
            //format = (count:imgName:life:shield:speed, ...)
            List<Target> waveTargets = Stream
                    .of(wave.split(LIST_SEPARATOR))
                    .map(toTarget)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            Collections.shuffle(waveTargets);

            return new Wave(waveTargets, paths);
        }).collect(Collectors.toList());
    }

    private static List<com.nine.td.game.path.Path> parsePaths(List<String> pathsDefinition, Scale scale, char[][] grid) {
        double scaleX = scale.getX();
        double scaleY = scale.getY();

        int rows = grid.length;
        int columns = grid[0].length;

        double canvasWidth  = REQUIRED_SIZE * scaleX * columns;
        double canvasHeight = REQUIRED_SIZE * scaleY * rows;

        Function<String, WayPoint> toWaypoint = wp -> {
            //format = x:y:[N/E/S/W]
            String[] wayPointDefinition = wp.split(DATA_SEPARATOR);

            int x = Integer.valueOf(wayPointDefinition[0]);
            int y = Integer.valueOf(wayPointDefinition[1]);

            Preconditions.checkState(x >= 0, "require positive x position for waypoint (found " + x + ")");
            Preconditions.checkState(y >= 0, "require positive y position for waypoint (found " + y + ")");

            Position position = new Position(y * (canvasWidth / columns),x * (canvasHeight / rows));
            Direction direction = Direction.get(wayPointDefinition[2].charAt(0));

            return new WayPoint(direction, position);
        };

        return pathsDefinition.stream().map(path -> {
            //format = path:(x:y:[N/E/S/W], x:y:[N/E/S/W], ...)
            return new com.nine.td.game.path.Path(
                    Stream.of(path.split(LIST_SEPARATOR)).map(toWaypoint).collect(Collectors.toList())
            );
        }).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }
}
