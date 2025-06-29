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

public class ParticlesGame extends Application {
    private static final int SCREEN_SIZE_X = 800;
    private static final int SCREEN_SIZE_Y = 600;
    private static final int PARTICLE_SIZE = 5;
    private static double GRAVITY = 10.00;
    private static final double REPEL_CONSTANT = 100;
    private static final double MIN_DISTANCE = 0.01;
    private static final double MAX_FORCE = 1000.0;
    private static final double FIXED_STEP_SECONDS = 0.016; // 60 FPS

    private final List<Particle> particles = new ArrayList<>();
    private long frameCount = 0;
    private long startTime = System.nanoTime();
    private Label fpsLabel;
    private Label particleLabel;
    private Pane simulationPane; // Referência mantida para remoção de partículas

    public ParticlesGame() {
    }

    @Override
    public void start(Stage primaryStage) {
        simulationPane = new Pane();
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

        instantiateParticles(PARTICLE_SIZE, SCREEN_SIZE_X - 200, SCREEN_SIZE_Y - 200);

        simulationPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                addNewParticle(e.getX(), e.getY(), 10, 10000, Color.RED);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                removeParticleAt(e.getX(), e.getY());
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
        // Resetar forças
        for (Particle p : particles) {
            p.resetForces();
        }

        // Calcular forças entre todos os pares de partículas
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                calculateForces(p1, p2);
            }
        }

        // Atualizar física e verificar colisões com as paredes
        List<Particle> particlesToRemove = new ArrayList<>();
        
        for (Particle p : particles) {
            p.updatePhysics(FIXED_STEP_SECONDS);
            
            // Verificar se a partícula tocou em alguma parede
            if (isTouchingWall(p)) {
                particlesToRemove.add(p);
            }
        }
        
        // Remover partículas que tocaram nas paredes
        for (Particle p : particlesToRemove) {
            removeParticle(p);
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

    private boolean isTouchingWall(Particle p) {
        double radius = p.getRadius();
        double x = p.getPosX();
        double y = p.getPosY();

        // Verificar se a partícula tocou em alguma parede
        return (x - radius <= 0) || 
               (x + radius >= SCREEN_SIZE_X) ||
               (y - radius <= 0) || 
               (y + radius >= SCREEN_SIZE_Y);
    }

    public void addNewParticle(double x, double y, double size, double mass, Color color) {
        Particle p = new Particle(size, x, y);
        p.setMass(mass);
        p.getCircle().setFill(color);
        particles.add(p);
        simulationPane.getChildren().add(p.getCircle());
    }

    public void removeParticleAt(double x, double y) {
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
            removeParticle(toRemove);
        }
    }
    
    private void removeParticle(Particle particle) {
        particles.remove(particle);
        simulationPane.getChildren().remove(particle.getCircle());
    }

    public void instantiateParticles(double size, double width, double height) {
        double spacing = size * 3;
        int particlesPerRow = (int) (width / spacing);
        int particlesPerCol = (int) (height / spacing);
        int totalParticles = Math.min(500, particlesPerRow * particlesPerCol);

        for (int i = 0; i < totalParticles; i++) {
            int row = i / particlesPerRow;
            int col = i % particlesPerRow;
            double x = 100 + col * spacing;
            double y = 100 + row * spacing;
            addNewParticle(x, y, size, 1, Color.BLUE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    // Classe Particle permanece inalterada
    public static class Particle {
        private static long nextId = 0;
        private final long id;
        private final Circle circle;
        private double posX;
        private double posY;
        private double velocityX;
        private double velocityY;
        private double accelerationX;
        private double accelerationY;
        private double mass = 1.0;
        private double radius;

        public Particle(double radius, double posX, double posY) {
            this.id = nextId++;
            this.radius = radius;
            this.circle = new Circle(radius);
            this.posX = posX;
            this.posY = posY;
            updateView();
        }

        public long getId() {
            return id;
        }

        public Circle getCircle() {
            return circle;
        }

        public double getPosX() {
            return posX;
        }

        public double getPosY() {
            return posY;
        }

        public double getVelocityX() {
            return velocityX;
        }

        public double getVelocityY() {
            return velocityY;
        }

        public double getMass() {
            return mass;
        }

        public double getRadius() {
            return radius;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        public void setVelocity(double vx, double vy) {
            this.velocityX = vx;
            this.velocityY = vy;
        }

        public void applyForce(double fx, double fy) {
            accelerationX += fx / mass;
            accelerationY += fy / mass;
        }

        public void resetForces() {
            accelerationX = 0;
            accelerationY = 0;
        }

        public void updatePhysics(double deltaTime) {
            // Atualizar velocidade
            velocityX += accelerationX * deltaTime;
            velocityY += accelerationY * deltaTime;

            // Atualizar posição
            posX += velocityX * deltaTime;
            posY += velocityY * deltaTime;
        }

        public void updateView() {
            circle.setCenterX(posX);
            circle.setCenterY(posY);
        }
    }
}