package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Particle {
	public Circle particleCircle;
	public double posX;
	public double posY;
	public double velocityX=0;
	public double velocityY=0;
	public double accelX=0;
	public double accelY=0;
	public double forceX=0;
	public double forceY=0;
	public double kinectEnergy=0;
	public double mass=1000;
	public double circleSize;
	private static double timeStep = 0.01;


	public Particle(int circleSize, double x, double y) {
		posX = x;
		posY = y;
		this.circleSize = circleSize;
		mass = 3.14159265359*circleSize*circleSize;
		particleCircle = new Circle(x, y, circleSize, Color.BLACK);
	}
	public Particle(int circleSize,double x, double y,double vx,double vy) {
		posX = x;
		posY = y;
		velocityX=vx;
		velocityY=vy;
		this.circleSize = circleSize;
		mass = 3.14159265359*circleSize*circleSize;
		particleCircle = new Circle(x, y, circleSize, Color.BLACK);
	}
	public Particle(int circleSize,double x, double y,double vx,double vy,double ax,double ay) {
		posX = x;
		posY = y;
		velocityX=vx;
		velocityY=vy;
		accelX=ax;
		accelY=ay;
		this.circleSize = circleSize;
		mass = 3.14159265359*circleSize*circleSize;
		particleCircle = new Circle(x, y, circleSize, Color.BLACK);
	}
		
	public void applyForce(double forceX, double forceY) {
	    double accelerationX = forceX / mass;
	    double accelerationY = forceY / mass;
	    velocityX += accelerationX * timeStep;
	    velocityY += accelerationY * timeStep;
	    posX += velocityX * timeStep;
	    posY += velocityY * timeStep;
		//System.out.println(velocityX);
		//System.out.println(velocityY);

	}
	
	public void applyForce2() {
	    accelX = forceX / mass;
	    accelY = forceY / mass;
	    velocityX += accelX * timeStep;
	    velocityY += accelY * timeStep;
	    posX += velocityX * timeStep;
	    posY += velocityY * timeStep;
		//System.out.println(Math.sqrt(forceX*forceX+forceY*forceY));
		//System.out.println(velocityY);

	}
	
	public double particleEnergy() {
		kinectEnergy = mass*(velocityX*velocityX+velocityY*velocityY)/2;
		
		//System.out.println(kinectEnergy);
		return kinectEnergy;
	}
	public double particleEnergyX() {
		kinectEnergy = mass*(velocityX*velocityX)/2;
		
		//System.out.println(kinectEnergy);
		return kinectEnergy;
	}
	public double particleEnergyY() {
		kinectEnergy = mass*(velocityY*velocityY)/2;
		
		//System.out.println(kinectEnergy);
		return kinectEnergy;
	}
	
}