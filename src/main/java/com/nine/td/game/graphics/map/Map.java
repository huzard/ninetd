package com.nine.td.game.graphics.map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.nine.td.GameConstants;
import com.nine.td.GamePaths;
import com.nine.td.game.graphics.Components;
import com.nine.td.game.graphics.GraphicComponent;
import com.nine.td.game.path.Direction;
import com.nine.td.game.path.Position;
import com.nine.td.game.path.Wave;
import com.nine.td.game.path.WayPoint;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.playable.HasVariableSpeed;
import com.nine.td.game.playable.Target;
import com.nine.td.game.playable.unit.BasicUnit;
import com.nine.td.game.playable.unit.Unit;
import com.nine.td.game.ui.HasRendering;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.nine.td.GameConstants.*;

public final class Map implements HasRendering<Node>, Engine, HasVariableSpeed {
    private Group root;

    private char[][] grid;
    private Deque<Wave> waves;

    private final String name;
    private final Scale scale;
    private final Properties metaProperties;

    private final List<GraphicComponent> waypoints = new LinkedList<>();

    private final Group mapRendering = new Group();
    private final Group enemies = new Group();
    private final Group waypointsGroup = new Group();
    private final Group unitsGroup = new Group();

    private final List<Unit> units = new LinkedList<>();

    private Map(String name, Scale scale) {
        this.scale = scale;
        this.name = name;
        this.metaProperties = new Properties();
    }

    @Override
    public Node render() {
        if (this.root == null) {
            int rows = this.grid.length;
            int columns = this.grid[0].length;

            IntStream
                    .range(0, rows)
                    .forEach(i -> IntStream.range(0, columns).forEach(j -> {
                        Position position = calibrate(scale, grid, j, i);

                        GraphicComponent component = Components.get(
                                this.grid[i][j],
                                this.scale,
                                position
                        );

                        ImageView render = component.render();

                        if(this.grid[i][j] == GameConstants.WALL_TILE) {
                            //no need to check for unit presence, mouse click is intercepted on the new created unit frame once it's set
                            render.setOnMouseClicked(event -> addUnit(new BasicUnit(position)));
                        }

                        this.mapRendering.getChildren().add(render);
                    }));

            this.root = new Group(this.mapRendering, this.enemies, this.waypointsGroup, this.unitsGroup);
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

            java.util.Map<Boolean, List<String>> partition = matrix
                    .stream()
                    .collect(Collectors.partitioningBy(line -> Stream.of(PATH_PREFIX, WAVE_PREFIX).noneMatch(line::startsWith)));

            List<String> mapDefinition = partition.get(true);
            Preconditions.checkState(mapDefinition.size() > 1, "bad map matrix");
            Preconditions.checkState(mapDefinition.stream().allMatch(row -> row.length() == matrix.get(0).length()), "bad map definition");

            List<String> pathsDefinition = filterBy(PATH_PREFIX, partition.get(false));
            Preconditions.checkState(pathsDefinition.size() >= 1, "bad paths definition");
            Preconditions.checkState(pathsDefinition.stream().allMatch(PATH_LIST_PATTERN.asPredicate()), "bad paths definition [format = x:y:direction (N, E, S, W), ...");

            List<String> wavesDefinition = filterBy(WAVE_PREFIX, partition.get(false));
            Preconditions.checkState(wavesDefinition.size() >= 1, "bad waves definition");
            Preconditions.checkState(wavesDefinition.stream().allMatch(WAVE_LIST_PATTERN.asPredicate()), "bad waves definition [format = how much:img_name:life:shield:speed, ...]");

            //Map definition
            this.grid = mapDefinition.stream().map(String::toCharArray).toArray(char[][]::new);

            //waves compute
            this.waves = parseWaves(wavesDefinition, parsePaths(pathsDefinition, this.scale, this.grid), this.scale);

            return this.loadMeta(pathMap);
        } catch(IOException e) {
            throw new RuntimeException("Error loading map " + this.name + " : " + e.getMessage());
        }
    }

    private void loadWaypoints(List<com.nine.td.game.path.Path> paths) {
        this.waypoints.clear();
        this.waypointsGroup.getChildren().clear();
        paths.forEach(path -> Stream.concat(Stream.of(path.getStart(), path.getEnd()), path.getWayPoints().stream()).forEach(wayPoint -> {
            GraphicComponent image = Components.get(wayPoint.getDirection().charCode(), this.scale, wayPoint.getPosition());
            this.waypoints.add(image);
            this.waypointsGroup.getChildren().add(image.render());
        }));
    }

    public static Map load(String fileName, Scale scale) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "bad filename");
        Preconditions.checkArgument(scale != null, "null scale");

        return new Map(fileName, scale).reload();
    }

    public static Map load(String fileName) {
        return load(fileName, new Scale(1.0, 1.0));
    }

    private static List<String> filterBy(String prefix, List<String> values) {
        return values
                .stream()
                .filter(value -> value.startsWith(prefix))
                .map(value -> value.substring(PATH_PREFIX.length()))
                .map(value -> value.replace("\\s+", ""))
                .collect(Collectors.toList());
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

    public Optional<Wave> getCurrentWave() {
        return Optional.ofNullable(this.waves.peek());
    }

    private static Deque<Wave> parseWaves(List<String> wavesDefinition, List<com.nine.td.game.path.Path> paths, Scale scale) {
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
                    .generate(() -> new Target(GamePaths.ENEMIES.resolve(imgName), scale, life, shield, speed))
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
        }).collect(LinkedList::new, Deque::push, Deque::addAll);
    }

    private static List<com.nine.td.game.path.Path> parsePaths(List<String> pathsDefinition, Scale scale, char[][] grid) {
        Function<String, WayPoint> toWaypoint = wp -> {
            //format = x:y:[N/E/S/W]
            String[] wayPointDefinition = wp.split(DATA_SEPARATOR);

            int x = Integer.valueOf(wayPointDefinition[0]);
            int y = Integer.valueOf(wayPointDefinition[1]);

            Preconditions.checkState(x >= 0, "require positive x position for waypoint (found " + x + ")");
            Preconditions.checkState(y >= 0, "require positive y position for waypoint (found " + y + ")");

            Position position   = calibrate(scale, grid, x, y);
            Direction direction = Direction.get(wayPointDefinition[2].charAt(0));

            return new WayPoint(direction, position);
        };

        return pathsDefinition.stream().map(path -> {
            //format = path:(x:y:[N/E/S/W], x:y:[N/E/S/W], ...)
            return new com.nine.td.game.path.Path(Stream.of(path.split(LIST_SEPARATOR)).map(toWaypoint).collect(Collectors.toList()));
        }).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public Collection<Wave> getWaves() {
        return Collections.unmodifiableCollection(this.waves);
    }

    public void loadNextWave() {
        this.waves.removeFirst();
    }

    @Override
    public void start() {
        this.getCurrentWave().ifPresent(wave -> {
            if(this.enemies.getChildren().isEmpty()) {
                this.enemies.getChildren().setAll(wave.get().stream().map(Target::render).collect(Collectors.toList()));
                this.units.forEach(unit -> wave.get().forEach(target -> target.add(unit)));
            }

            if(this.waypointsGroup.getChildren().isEmpty()) {
                this.loadWaypoints(wave.getPaths());
            }

            wave.start();

            this.waypoints.forEach(Engine::start);
            this.units.forEach(Engine::start);
        });
    }

    @Override
    public void stop() {
        this.getCurrentWave().ifPresent(wave -> {
            Stream.of(this.enemies, this.waypointsGroup).forEach(group -> group.getChildren().clear());
            wave.stop();
            this.waypoints.forEach(Engine::stop);
            this.units.forEach(Engine::stop);
        });
    }

    @Override
    public void pause() {
        this.getCurrentWave().ifPresent(wave -> {
            wave.pause();
            this.waypoints.forEach(Engine::pause);
            this.units.forEach(Engine::pause);
        });
    }

    @Override
    public void changeSpeed(double coeff) {
        this.getCurrentWave().ifPresent(wave -> {
            wave.changeSpeed(coeff);
            this.units.forEach(unit -> unit.changeSpeed(coeff));
        });
    }

    public boolean isOver() {
        return !this.getCurrentWave().isPresent();
    }

    private void addUnit(Unit unit) {
        this.units.add(unit);
        ImageView imageView = Components.get('u', this.scale, unit.getPosition()).render();
        this.unitsGroup.getChildren().add(imageView);
    }

    private static Position calibrate(Scale scale, char[][] grid, int x, int y) {
        double scaleX = scale.getX();
        double scaleY = scale.getY();

        int rows = grid.length;
        int columns = grid[0].length;

        double canvasWidth  = REQUIRED_SIZE * scaleX * columns;
        double canvasHeight = REQUIRED_SIZE * scaleY * rows;

        return new Position(x * (canvasWidth / columns),y * (canvasHeight / rows));
    }
}
