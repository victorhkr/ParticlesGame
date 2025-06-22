package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ParticlesGame extends Application {
    private static final int SCREEN_SIZE_X = 800;
    private static final int SCREEN_SIZE_Y = 600;
    private static final int PARTICLE_SIZE = 2;
    private static double GRAVITY = 10.00; // Valor dentro do intervalo do slider
    private static final double REPEL_CONSTANT = 100;
    private static final double DAMPING = 0.95;
    private static final double MIN_DISTANCE = 0.01;
    private static final double MAX_FORCE = 1000.0;
    private static final double FIXED_STEP_SECONDS = 0.016; // 60 FPS
    
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle>[][] spatialGrid;
    private final int gridCols, gridRows;
    private final double gridCellSize = 200;
    private long frameCount = 0;
    private long startTime = System.nanoTime();
    private Label fpsLabel;
    private Label particleLabel;

    @SuppressWarnings("unchecked")
    public ParticlesGame() {
        gridCols = (int) Math.ceil(SCREEN_SIZE_X / gridCellSize);
        gridRows = (int) Math.ceil(SCREEN_SIZE_Y / gridCellSize);
        spatialGrid = new ArrayList[gridCols][gridRows];
        
        // Inicializar grid com listas vazias
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                spatialGrid[i][j] = new ArrayList<>();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Pane simulationPane = new Pane();
        simulationPane.setPrefSize(SCREEN_SIZE_X, SCREEN_SIZE_Y);
        
        // Painel de controle
        HBox controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);
        
        Slider gravitySlider = new Slider(0, 50, GRAVITY);
        gravitySlider.setShowTickLabels(true);
        gravitySlider.setShowTickMarks(true);
        gravitySlider.setMajorTickUnit(0.001);
        gravitySlider.setPrefWidth(200);
        gravitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            GRAVITY = newVal.doubleValue();
        });
        
        fpsLabel = new Label("FPS: --");
        fpsLabel.setFont(Font.font("Arial", 14));
        
        particleLabel = new Label("Partículas: 0");
        particleLabel.setFont(Font.font("Arial", 14));
        
        controlPanel.getChildren().addAll(
                new Label("Gravidade:"), gravitySlider, 
                fpsLabel, particleLabel);

        BorderPane root = new BorderPane();
        root.setCenter(simulationPane);
        root.setBottom(controlPanel);

        Scene scene = new Scene(root, SCREEN_SIZE_X, SCREEN_SIZE_Y + 50);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulação de Partículas Avançada");
        primaryStage.show();

        instantiateParticles(simulationPane, PARTICLE_SIZE, SCREEN_SIZE_X - 200, SCREEN_SIZE_Y - 200);

        simulationPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                addNewParticle(simulationPane, e.getX(), e.getY(), 10, 10000, Color.RED);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                removeParticleAt(simulationPane, e.getX(), e.getY());
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                // Cálculo de FPS
                frameCount++;
                if (now - startTime >= 1_000_000_000) {
                    fpsLabel.setText(String.format("FPS: %d", frameCount));
                    frameCount = 0;
                    startTime = now;
                }
                particleLabel.setText(String.format("Partículas: %d", particles.size()));
                
                // Atualização de física com passo fixo
                if (now - lastUpdate >= 16_000_000) {
                    updatePhysics();
                    lastUpdate = now;
                }
                
                // Atualizar visualização
                for (Particle p : particles) {
                    p.updateView();
                }
            }
        };
        timer.start();
    }

    private void updatePhysics() {
        // Resetar grid
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                spatialGrid[i][j].clear();
            }
        }

        // Preencher grid com partículas considerando seu raio
        for (Particle p : particles) {
            double radius = p.getRadius();
            int startX = Math.max(0, (int) ((p.getPosX() - radius) / gridCellSize));
            int endX = Math.min(gridCols - 1, (int) ((p.getPosX() + radius) / gridCellSize));
            int startY = Math.max(0, (int) ((p.getPosY() - radius) / gridCellSize));
            int endY = Math.min(gridRows - 1, (int) ((p.getPosY() + radius) / gridCellSize));
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    spatialGrid[x][y].add(p);
                }
            }
        }
        
        // Resetar forças
        for (Particle p : particles) {
            p.resetForces();
        }
        
        // Calcular interações usando grid espacial
        for (int i = 0; i < gridCols; i++) {
            for (int j = 0; j < gridRows; j++) {
                List<Particle> cellParticles = spatialGrid[i][j];
                
                for (int k = 0; k < cellParticles.size(); k++) {
                    Particle p1 = cellParticles.get(k);
                    
                    // Verificar partículas na mesma célula
                    for (int l = k + 1; l < cellParticles.size(); l++) {
                        Particle p2 = cellParticles.get(l);
                        if (p1.getId() != p2.getId()) {
                            calculateForces(p1, p2);
                        }
                    }
                    
                    // Verificar células vizinhas (3x3 grid)
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue; // Já processamos a célula atual
                            
                            int neighborX = i + dx;
                            int neighborY = j + dy;
                            
                            if (neighborX >= 0 && neighborX < gridCols && 
                                neighborY >= 0 && neighborY < gridRows) {
                                
                                for (Particle p2 : spatialGrid[neighborX][neighborY]) {
                                    if (p1.getId() < p2.getId()) { // Evitar duplicatas
                                        calculateForces(p1, p2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Atualizar física
        for (Particle p : particles) {
            p.updatePhysics(FIXED_STEP_SECONDS);
            handleBoundaryCollision(p);
        }
    }

    public void calculateForces(Particle p1, Particle p2) {
        double dx = p2.getPosX() - p1.getPosX();
        double dy = p2.getPosY() - p1.getPosY();
        double distanceSq = dx*dx + dy*dy;
        double minDistance = p1.getRadius() + p2.getRadius();
        double minDistanceSq = minDistance * minDistance;
        
        if (distanceSq < MIN_DISTANCE * MIN_DISTANCE) return;
        
        // Atração gravitacional com limite
        if (distanceSq > minDistanceSq) {
            double force = GRAVITY * p1.getMass() * p2.getMass() / distanceSq;
            force = Math.min(force, MAX_FORCE);
            
            double distance = Math.sqrt(distanceSq);
            p1.applyForce(force * dx/distance, force * dy/distance);
            p2.applyForce(-force * dx/distance, -force * dy/distance);
        } 
        // Repulsão com limite
        else {
            double distance = Math.sqrt(distanceSq);
            double overlap = minDistance - distance;
            double repelForce = REPEL_CONSTANT * overlap;
            repelForce = Math.min(repelForce, MAX_FORCE);
            
            p1.applyForce(-repelForce * dx/distance, -repelForce * dy/distance);
            p2.applyForce(repelForce * dx/distance, repelForce * dy/distance);
        }
    }

    private void handleBoundaryCollision(Particle p) {
        double radius = p.getRadius();
        double x = p.getPosX();
        double y = p.getPosY();
        double vx = p.getVelocityX();
        double vy = p.getVelocityY();
        
        if (x < radius) {
            p.setPos(radius, y);
            p.setVelocity(-vx * DAMPING, vy);
        } else if (x > SCREEN_SIZE_X - radius) {
            p.setPos(SCREEN_SIZE_X - radius, y);
            p.setVelocity(-vx * DAMPING, vy);
        }
        
        if (y < radius) {
            p.setPos(x, radius);
            p.setVelocity(vx, -vy * DAMPING);
        } else if (y > SCREEN_SIZE_Y - radius) {
            p.setPos(x, SCREEN_SIZE_Y - radius);
            p.setVelocity(vx, -vy * DAMPING);
        }
    }
    
    public void addNewParticle(Pane root, double x, double y, double size, double mass, Color color) {
        Particle p = new Particle(size, x, y);
        p.setMass(mass);
        p.getCircle().setFill(color);
        particles.add(p);
        root.getChildren().add(p.getCircle());
    }

    public void removeParticleAt(Pane root, double x, double y) {
        double radiusThreshold = 10;
        Particle toRemove = null;
        
        for (Particle p : particles) {
            double dx = p.getPosX() - x;
            double dy = p.getPosY() - y;
            double distanceSq = dx*dx + dy*dy;
            
            if (distanceSq < radiusThreshold * radiusThreshold) {
                toRemove = p;
                break;
            }
        }
        
        if (toRemove != null) {
            particles.remove(toRemove);
            root.getChildren().remove(toRemove.getCircle());
        }
    }

    public void instantiateParticles(Pane root, double size, double width, double height) {
        double spacing = size * 3;
        int particlesPerRow = (int) (width / spacing);
        int particlesPerCol = (int) (height / spacing);
        int totalParticles = Math.min(500, particlesPerRow * particlesPerCol);

        for (int i = 0; i < totalParticles; i++) {
            int row = i / particlesPerRow;
            int col = i % particlesPerRow;
            double x = 100 + col * spacing;
            double y = 100 + row * spacing;
            Particle p = new Particle(size, x, y);
            p.setMass(1);
            particles.add(p);
            root.getChildren().add(p.getCircle());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Classe Particle aprimorada
    public static class Particle {
        private static final AtomicLong nextId = new AtomicLong(0);
        
        private final long id = nextId.getAndIncrement();
        private final Circle circle;
        private double posX;
        private double posY;
        private double velocityX;
        private double velocityY;
        private double forceX;
        private double forceY;
        private double mass = 1.0;
        private final double radius;

        public Particle(double radius, double x, double y) {
            this.radius = radius;
            this.posX = x;
            this.posY = y;
            this.circle = new Circle(radius);
            updateView();
        }

        public void resetForces() {
            forceX = 0;
            forceY = 0;
        }

        public void applyForce(double fx, double fy) {
            forceX += fx;
            forceY += fy;
        }

        public void updatePhysics(double deltaTime) {
            // a = F/m
            double ax = forceX / mass;
            double ay = forceY / mass;
            
            // Atualizar velocidade
            velocityX += ax * deltaTime;
            velocityY += ay * deltaTime;
            
            // Atualizar posição
            posX += velocityX * deltaTime;
            posY += velocityY * deltaTime;
        }

        public void updateView() {
            circle.setCenterX(posX);
            circle.setCenterY(posY);
        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        public void setVelocity(double vx, double vy) {
            this.velocityX = vx;
            this.velocityY = vy;
        }

        // Getters
        public Circle getCircle() { return circle; }
        public double getPosX() { return posX; }
        public double getPosY() { return posY; }
        public double getVelocityX() { return velocityX; }
        public double getVelocityY() { return velocityY; }
        public double getRadius() { return radius; }
        public double getMass() { return mass; }
        public long getId() { return id; }
        
        // Setters
        public void setMass(double mass) { this.mass = mass; }
    }
}