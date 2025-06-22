package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ParticlesGame extends Application {
    private static final int SCREEN_SIZE_X = 800;
    private static final int SCREEN_SIZE_Y = 600;
    private static final int PARTICLE_SIZE = 2;
    private static final double GRAVITY = 25.0;
    private static final double REPEL_CONSTANT = 100;
    private static final double DAMPING = 0.95;
    
    private final ObservableList<Particle> particles = FXCollections.observableArrayList();
    private final Object lock = new Object();

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setPrefSize(SCREEN_SIZE_X, SCREEN_SIZE_Y);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Particles Game");
        primaryStage.show();

        instantiateParticles(root, PARTICLE_SIZE, SCREEN_SIZE_X - 200, SCREEN_SIZE_Y - 200);

        root.setOnMouseClicked(e -> {
            addNewParticle(root, e.getX(), e.getY(), 10, 10000, Color.RED);
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000) {
                    updatePhysics();
                    lastUpdate = now;
                }
                
                // Atualizar visualização
                synchronized (lock) {
                    for (Particle p : particles) {
                        p.updateView();
                    }
                }
            }
        };
        timer.start();
    }

    private void updatePhysics() {
        synchronized (lock) {
            // 1. Resetar forças
            for (Particle p : particles) {
                p.resetForces();
            }
            
            // 2. Calcular interações
            for (int i = 0; i < particles.size(); i++) {
                for (int j = i + 1; j < particles.size(); j++) {
                    calculateForces(particles.get(i), particles.get(j));
                }
            }
            
            // 3. Atualizar física
            for (Particle p : particles) {
                p.updatePhysics();
                handleBoundaryCollision(p);
            }
        }
    }

    public void calculateForces(Particle p1, Particle p2) {
        double dx = p2.getPosX() - p1.getPosX();
        double dy = p2.getPosY() - p1.getPosY();
        double distance = Math.sqrt(dx*dx + dy*dy);
        double minDistance = p1.getCircleSize() + p2.getCircleSize();
        
        if (distance < 0.0001) return;
        
        if (distance > minDistance) {
            // Atração gravitacional
            double force = GRAVITY * p1.getMass() * p2.getMass() / (distance * distance);
            p1.addForce(force * dx/distance, force * dy/distance);
            p2.addForce(-force * dx/distance, -force * dy/distance);
        } else {
            // Repulsão
            double overlap = minDistance - distance;
            double repelForce = REPEL_CONSTANT * overlap;
            p1.addForce(-repelForce * dx/distance, -repelForce * dy/distance);
            p2.addForce(repelForce * dx/distance, repelForce * dy/distance);
            
            // Correção de posição
            double displace = overlap * 0.5;
            p1.setPos(
                p1.getPosX() - displace * dx/distance, 
                p1.getPosY() - displace * dy/distance
            );
            p2.setPos(
                p2.getPosX() + displace * dx/distance, 
                p2.getPosY() + displace * dy/distance
            );
        }
    }

    private void handleBoundaryCollision(Particle p) {
        double radius = p.getCircleSize();
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
        synchronized (lock) {
            Particle p = new Particle(size, x, y);
            p.setMass(mass);
            p.getParticleCircle().setFill(color);
            particles.add(p);
            root.getChildren().add(p.getParticleCircle());
        }
    }

    public void instantiateParticles(Pane root, double size, double width, double height) {
        synchronized (lock) {
            for (int i = 0; i < 500; i++) {
                double randomX = Math.random() * width + 200;
                double randomY = Math.random() * height + 100;
                Particle p = new Particle(size, randomX, randomY);
                particles.add(p);
                root.getChildren().add(p.getParticleCircle());
            }
            
            // Prevenir sobreposição inicial
            for (int i = 0; i < particles.size(); i++) {
                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p1 = particles.get(i);
                    Particle p2 = particles.get(j);
                    
                    double dx = p2.getPosX() - p1.getPosX();
                    double dy = p2.getPosY() - p1.getPosY();
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    double minDist = p1.getCircleSize() + p2.getCircleSize();
                    
                    if (distance < minDist) {
                        double overlap = minDist - distance;
                        double angle = Math.atan2(dy, dx);
                        p1.setPos(
                            p1.getPosX() - overlap * 0.5 * Math.cos(angle),
                            p1.getPosY() - overlap * 0.5 * Math.sin(angle)
                        );
                        p2.setPos(
                            p2.getPosX() + overlap * 0.5 * Math.cos(angle),
                            p2.getPosY() + overlap * 0.5 * Math.sin(angle)
                        );
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}