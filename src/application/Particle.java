package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Particle {
    private double posX, posY;
    private double velocityX, velocityY;
    private double forceX, forceY;
    private double mass;
    private double circleSize;
    private Circle particleCircle;
    private static double timeStep = 0.01;

    public Particle(double size, double x, double y) {
        this.circleSize = size;
        this.posX = x;
        this.posY = y;
        this.mass = Math.PI * size * size;
        this.particleCircle = new Circle(x, y, size, Color.BLACK);
    }

    public void resetForces() {
        forceX = 0;
        forceY = 0;
    }

    public void addForce(double fx, double fy) {
        forceX += fx;
        forceY += fy;
    }

    public void updatePhysics() {
        double accelX = forceX / mass;
        double accelY = forceY / mass;
        
        velocityX += accelX * timeStep;
        velocityY += accelY * timeStep;
        
        posX += velocityX * timeStep;
        posY += velocityY * timeStep;
    }

    public void updateView() {
        particleCircle.setCenterX(posX);
        particleCircle.setCenterY(posY);
    }

    // Getters e Setters
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public void setPos(double x, double y) { posX = x; posY = y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public void setVelocity(double vx, double vy) { velocityX = vx; velocityY = vy; }
    public double getCircleSize() { return circleSize; }
    public double getMass() { return mass; }
    public void setMass(double mass) { this.mass = mass; }
    public Circle getParticleCircle() { return particleCircle; }
}