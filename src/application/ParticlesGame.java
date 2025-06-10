package application;

import java.util.Random;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticlesGame extends Application {

	private static int NUMBER_OF_PARTICLES = 500;
	private static final int SCREEN_SIZE_X = 800;
	private static final int SCREEN_SIZE_Y = 600;
	private static final int MAX_PARTICLE_SIZE = 2;
	private static final int MIN_PARTICLE_SIZE = 1;
	private static final int PARTICLE_SIZE = 2;
	private static ArrayList<Particle> particlesGroup = new ArrayList<Particle>();

	@Override
	public void start(Stage primaryStage) {
		Pane root = new Pane();
		root.setPrefSize(SCREEN_SIZE_X, SCREEN_SIZE_Y);
		Scene scene = new Scene(root);
		// Set the mouse click event handler
		primaryStage.setScene(scene);
		primaryStage.setTitle("Particles Game");
		primaryStage.show();

		instantiateParticles (root, PARTICLE_SIZE, SCREEN_SIZE_X-200, SCREEN_SIZE_Y-200);

		root.setOnMouseClicked(e -> {
			
			int	newParticleSize = 10;
			double newParticleMass = 10000;
			addNewParticle (root, e.getX(), e.getY() , newParticleSize , newParticleMass );
			System.out.println("New Particle Added");

			//particlesGroup.get(NUMBER_OF_PARTICLES-1).velocityX = 0;
			//particlesGroup.get(NUMBER_OF_PARTICLES-1).velocityY = 0;//-1000000;
		});

		root.setOnMouseDragged(e -> {
			int	newParticleSize = 2;
			double newParticleMass = 10000;
			double dx = particlesGroup.get(NUMBER_OF_PARTICLES-1).posX - e.getX();
			double dy = particlesGroup.get(NUMBER_OF_PARTICLES-1).posY - e.getY();
			double distance = Math.sqrt(dx*dx+dy*dy);
			if (distance > particlesGroup.get(NUMBER_OF_PARTICLES-1).circleSize+5) {
				addNewParticle (root, e.getX(), e.getY() , newParticleSize , newParticleMass);
				System.out.println("New Particle Added");
			}
		});

		startCalculationThread();


		AnimationTimer timer = new AnimationTimer() {

			@Override
			public void handle(long currentTime) {
				for(int i = 0; i<NUMBER_OF_PARTICLES; i++) {

					particlesGroup.get(i).particleCircle.setCenterX(particlesGroup.get(i).posX);
					particlesGroup.get(i).particleCircle.setCenterY(particlesGroup.get(i).posY);
				}
			}
		};
		timer.start();

	}
	/*
	public static void collideParticles(Particle p1, Particle p2) {
		double COR = 0.96; // a COR of 0.8, which means 20% energy loss
		double maxRepulsion = 5; // the maximum repulsive force
		double repulsionDistance = p1.circleSize+p2.circleSize; // the distance at which the repulsion force reaches its maximum
		double dx = p2.posX - p1.posX;
		double dy = p2.posY - p1.posY;
		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist < repulsionDistance) {
			// if the particles are too close, move them apart
			double overlap = repulsionDistance - dist;
			double angle = Math.atan2(dy, dx);
			p1.posX -= overlap * Math.cos(angle);
			p1.posY -= overlap * Math.sin(angle);
			p2.posX += overlap * Math.cos(angle);
			p2.posY += overlap * Math.sin(angle);
			dx = p2.posX - p1.posX;
			dy = p2.posY - p1.posY;
			dist = Math.sqrt(dx * dx + dy * dy);
		}

		// calculate relative velocity
		double nx = dx / dist;
		double ny = dy / dist;
		double v1n = p1.velocityX * nx + p1.velocityY * ny;
		double v1t = -p1.velocityX * ny + p1.velocityY * nx;
		double v2n = p2.velocityX * nx + p2.velocityY * ny;
		double v2t = -p2.velocityX * ny + p2.velocityY * nx;

		// calculate resulting velocities
		double v1nAfter = (v1n * (p1.mass - p2.mass) + 2 * p2.mass * v2n ) / (p1.mass + p2.mass);
		double v2nAfter = (v2n * (p2.mass - p1.mass) + 2 * p1.mass * v1n ) / (p1.mass + p2.mass);
		v1nAfter = COR * v1nAfter + (1 - COR) * v1n;
		v2nAfter = COR * v2nAfter + (1 - COR) * v2n;

		// update velocities
		p1.velocityX = v1nAfter * nx - v1t * ny;
		p1.velocityY = v1nAfter * ny + v1t * nx;
		p2.velocityX = v2nAfter * nx - v2t * ny;
		p2.velocityY = v2nAfter * ny + v2t * nx;
	}
	 */
	
	private void startCalculationThread() {
		// Create a new thread for continuous calculations
		Thread calculationThread = new Thread(() -> {
			while (true) {


				//calculate total force of each particle
				int k = 1;
				for(int i = 0; i<NUMBER_OF_PARTICLES-1; i++) {

					for(int j = k; j<NUMBER_OF_PARTICLES; j++) {
						//if(distance < 2*particlesGroup.get(i).circleSize+2*particlesGroup.get(j).circleSize ) {
						//collideParticles(particlesGroup.get(i),particlesGroup.get(j),distance,dx,dy);
						//collideParticles(particlesGroup.get(i),particlesGroup.get(j));

						//} else {
						calculateGravityForceOfTwoParticles(particlesGroup.get(i),particlesGroup.get(j));
						//}
					}
					k++;
				}

				double energyOfParticles = 0;
				// apply total force for each particle
				for(int i = 0; i<NUMBER_OF_PARTICLES; i++) {

					particlesGroup.get(i).applyForce2();


					particlesGroup.get(i).forceX = 0;
					particlesGroup.get(i).forceY = 0;


					//screen limits
					if(particlesGroup.get(i).posX>SCREEN_SIZE_X-particlesGroup.get(i).circleSize) {
						particlesGroup.get(i).posX=SCREEN_SIZE_X-particlesGroup.get(i).circleSize;
						particlesGroup.get(i).velocityX = -particlesGroup.get(i).velocityX;
					}
					if(particlesGroup.get(i).posX<particlesGroup.get(i).circleSize) {
						particlesGroup.get(i).posX=particlesGroup.get(i).circleSize;
						particlesGroup.get(i).velocityX = -particlesGroup.get(i).velocityX;
					}
					if(particlesGroup.get(i).posY>SCREEN_SIZE_Y-particlesGroup.get(i).circleSize) {
						particlesGroup.get(i).posY=SCREEN_SIZE_Y-particlesGroup.get(i).circleSize;
						particlesGroup.get(i).velocityY = -particlesGroup.get(i).velocityY;
					}
					if(particlesGroup.get(i).posY<particlesGroup.get(i).circleSize) {
						particlesGroup.get(i).posY=particlesGroup.get(i).circleSize;
						particlesGroup.get(i).velocityY = -particlesGroup.get(i).velocityY;
					}

					energyOfParticles += particlesGroup.get(i).particleEnergy();
				}
				//System.out.println("energy is: "+energyOfParticles);

			}
		});

		// Start the calculation thread
		calculationThread.setDaemon(true);
		calculationThread.start();
	}


	public static void calculateGravityForceOfTwoParticles(Particle p1, Particle p2) {

		double gravitationalConstant = 0.00050; // adjust this to control the strength of the attractive force
		double repelConstant = 100; 				// adjust this to control the strength of the repulsive force
		double dx = p2.posX - p1.posX;
		double dy = p2.posY - p1.posY;
		double distance = Math.sqrt(dx*dx+dy*dy); 
		double magnitude1 = gravitationalConstant*p1.mass * p2.mass / Math.pow(distance, 2);
		double fx1 = dx * magnitude1/distance;
		double fy1 = dy * magnitude1/distance;
		//double magnitude2 = repelConstant*p1.mass * p2.mass*Math.exp(-2*distance);// *Math.exp(-distance*1e15);
		double magnitude2 = repelConstant*((p1.circleSize+p2.circleSize)-distance);
		//double magnitude2 = 0;// *Math.exp(-distance*1e15);
		double fx2 = dx * magnitude2/distance;
		double fy2 = dy * magnitude2/distance;

		/*
		p1.forceX+=fx1-fx2;
		p2.forceX-=fx1+fx2;
		p1.forceY+=fy1-fy2;
		p2.forceY-=fy1+fy2;
		 */

		if((distance > p1.circleSize+p2.circleSize)) {

			p1.forceX+=fx1;
			p2.forceX-=fx1;
			p1.forceY+=fy1;
			p2.forceY-=fy1;

		}
		else {
			/*
			p1.velocityX = p1.velocityX - 1*p1.velocityX/100;
			p1.velocityY = p1.velocityY - 1*p1.velocityY/100;
			p2.velocityX = p2.velocityX - 1*p2.velocityX/100;
			p2.velocityY = p2.velocityY - 1*p1.velocityY/100;
			 */
			//System.out.println(fx2);

			//p1.forceX=-fx2;
			//p2.forceX= fx2;
			//p1.forceY=-fy2;
			//p2.forceY= fy2;
		}	
	}

	public static void addNewParticle (Pane root, double mousePosX, double mousePosY , int particleRadius , double newParticleMass ) {
		double dx = particlesGroup.get(NUMBER_OF_PARTICLES-1).posX - mousePosX; //verifica se esta perto 
		double dy = particlesGroup.get(NUMBER_OF_PARTICLES-1).posY - mousePosY; //da ultima particula adicionada
		double distance = Math.sqrt(dx*dx+dy*dy);
		if (distance > particlesGroup.get(NUMBER_OF_PARTICLES-1).circleSize+particleRadius) {

			particlesGroup.add(new Particle(particleRadius,mousePosX,mousePosY,0,0));
			root.getChildren().add(particlesGroup.get(NUMBER_OF_PARTICLES).particleCircle);
			particlesGroup.get(NUMBER_OF_PARTICLES).mass = newParticleMass;
			particlesGroup.get(NUMBER_OF_PARTICLES).particleCircle.setFill(Color.RED);
			NUMBER_OF_PARTICLES++;
		}
	}


	public static void instantiateParticles (Pane root, int particleRadius, int windowWidth, int windowLength) {
		for(int i = 0; i<NUMBER_OF_PARTICLES; i++) {
			double randomX = Math.random()*windowWidth + 200;
			double randomY = Math.random()*windowLength + 100;
			//double randomY = 300;
			//double randomX = rand.nextDouble()*SCREEN_SIZE_X;
			//double randomY = rand.nextDouble()*SCREEN_SIZE_Y;
			double randomvX = (Math.random()*2-1)*1;
			double randomvY = (Math.random()*2-1)*1;
			//particlesGroup.get(i) = new Particle(PARTICLE_SIZE,randomX,randomY,randomvX,randomvY);
			//particlesGroup.get(i) = new Particle(MIN_PARTICLE_SIZE+(int)(Math.random()*MAX_PARTICLE_SIZE),randomX,randomY,0,0);
			//particlesGroup.get(i) = new Particle(10,randomX,randomY,0,0);
			particlesGroup.add(new Particle(particleRadius,randomX,randomY,0,0));
			root.getChildren().add(particlesGroup.get(i).particleCircle);

		}
		int k = 1;
		for(int i = 0; i<NUMBER_OF_PARTICLES-1; i++) {
			for(int j = k; j<NUMBER_OF_PARTICLES; j++) {
				double dx = particlesGroup.get(j).posX - particlesGroup.get(i).posX;
				double dy = particlesGroup.get(j).posY - particlesGroup.get(i).posY;
				double distance = Math.sqrt(dx*dx+dy*dy);
				if (distance < 2*particlesGroup.get(i).circleSize+2*particlesGroup.get(j).circleSize) {
					// if the particles are too close, move them apart
					double overlap = 2*particlesGroup.get(i).circleSize+2*particlesGroup.get(j).circleSize - distance;
					double angle = Math.atan2(dy, dx);
					particlesGroup.get(i).posX -= overlap * Math.cos(angle);
					particlesGroup.get(i).posY -= overlap * Math.sin(angle);
					particlesGroup.get(j).posX += overlap * Math.cos(angle);
					particlesGroup.get(j).posY += overlap * Math.sin(angle);
				}
			}
			k++;
		}
	}
	public static void main(String[] args) {
		launch(args);
	}

}
