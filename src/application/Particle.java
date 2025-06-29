package application;

import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Particle {
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