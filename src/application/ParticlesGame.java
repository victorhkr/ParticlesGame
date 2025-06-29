package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticlesGame extends Application {
    private static final int SCREEN_SIZE_X = 800;
    private static final int SCREEN_SIZE_Y = 600;
    private static final int PARTICLE_SIZE = 5;
    private static double GRAVITY = 10.00;
    private static final double MIN_DISTANCE = 0.01;
    private static final double MAX_FORCE = 1000.0;
    private static final double FIXED_STEP_SECONDS = 0.016; // 60 FPS
    private static boolean COLLISIONS_ENABLED = true;
    private static boolean PAUSED = false;

    private final List<Particle> particles = new ArrayList<>();
    private long frameCount = 0;
    private long startTime = System.nanoTime();
    private Label fpsLabel;
    private Label particleLabel;
    private Pane simulationPane;

    public ParticlesGame() {
    }

    @Override
    public void start(Stage primaryStage) {
        simulationPane = new Pane();
        simulationPane.setPrefSize(SCREEN_SIZE_X, SCREEN_SIZE_Y);

        // Painel de controle principal
        VBox mainControlPanel = new VBox(15);
        mainControlPanel.setPadding(new Insets(15));
        mainControlPanel.setAlignment(Pos.TOP_CENTER);
        mainControlPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");
        
        // Título do jogo
        Label titleLabel = new Label("Particles Game");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Descrição do jogo
        Text description = new Text("Simulação de partículas com gravidade e colisões.\n" +
                                   "Clique com botão esquerdo para adicionar uma partícula,\n" +
                                   "botão direito para remover. Partículas são destruídas\n" +
                                   "quando tocam nas bordas da tela.");
        description.setTextAlignment(TextAlignment.CENTER);
        description.setFont(Font.font("Arial", 14));
        
        // Painel de controle para FPS e contagem
        HBox statsPanel = new HBox(20);
        statsPanel.setAlignment(Pos.CENTER);
        
        fpsLabel = new Label("FPS: --");
        fpsLabel.setFont(Font.font("Arial", 14));
        particleLabel = new Label("Partículas: 0");
        particleLabel.setFont(Font.font("Arial", 14));
        
        statsPanel.getChildren().addAll(fpsLabel, particleLabel);
        
        // Painel de controle para gravidade
        VBox gravityPanel = new VBox(5);
        gravityPanel.setAlignment(Pos.CENTER);
        
        Slider gravitySlider = new Slider(0, 50, GRAVITY);
        gravitySlider.setShowTickLabels(true);
        gravitySlider.setShowTickMarks(true);
        gravitySlider.setMajorTickUnit(5);
        gravitySlider.setMinorTickCount(4);
        gravitySlider.setBlockIncrement(1);
        gravitySlider.setPrefWidth(200);
        gravitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            GRAVITY = newVal.doubleValue();
        });
        
        Label gravityValueLabel = new Label(String.format("Força: %.1f", GRAVITY));
        gravityValueLabel.setFont(Font.font("Arial", 12));
        gravitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            gravityValueLabel.setText(String.format("Força: %.1f", newVal.doubleValue()));
        });
        
        gravityPanel.getChildren().addAll(
            new HBox(5, new Label("Gravidade:"), 
            gravitySlider,
            gravityValueLabel)
        );
        
        // Controle de colisões
        VBox collisionPanel = new VBox(5);
        collisionPanel.setAlignment(Pos.CENTER);
        
        CheckBox collisionCheckbox = new CheckBox("Ativar colisões");
        collisionCheckbox.setSelected(COLLISIONS_ENABLED);
        collisionCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            COLLISIONS_ENABLED = newVal;
        });
        
        Label collisionDescription = new Label("Quando ativado, as partículas se fundem ao tocar uma na outra");
        collisionDescription.setFont(Font.font("Arial", 10));
        collisionDescription.setStyle("-fx-text-fill: #555;");
        collisionDescription.setWrapText(true);
        collisionDescription.setMaxWidth(250);
        
        collisionPanel.getChildren().addAll(collisionCheckbox, collisionDescription);
        
        // Botão de pausa
        Button pauseButton = new Button("Pausar");
        pauseButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        pauseButton.setPrefWidth(120);
        pauseButton.setOnAction(e -> {
            PAUSED = !PAUSED;
            if (PAUSED) {
                pauseButton.setText("Continuar");
                pauseButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            } else {
                pauseButton.setText("Pausar");
                pauseButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }
        });
        
        // Dicas de uso
        VBox tipsPanel = new VBox(5);
        tipsPanel.setAlignment(Pos.CENTER_LEFT);
        tipsPanel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10; -fx-border-radius: 5;");
        
        Label tipsTitle = new Label("Dicas:");
        tipsTitle.setFont(Font.font("Arial", 12));
        tipsTitle.setStyle("-fx-font-weight: bold;");
        
        Label tip1 = new Label("• Partículas vermelhas são mais pesadas");
        tip1.setFont(Font.font("Arial", 10));
        
        Label tip2 = new Label("• Partículas grandes surgem de fusões");
        tip2.setFont(Font.font("Arial", 10));
        
        Label tip3 = new Label("• Desative colisões para simulações rápidas");
        tip3.setFont(Font.font("Arial", 10));
        
        tipsPanel.getChildren().addAll(tipsTitle, tip1, tip2, tip3);
        
        // Adicionar todos os componentes ao painel principal
        mainControlPanel.getChildren().addAll(
            titleLabel,
            description,
            statsPanel,
            gravityPanel,
            collisionPanel,
            pauseButton,
            tipsPanel
        );
        
        // Layout principal
        BorderPane root = new BorderPane();
        root.setCenter(simulationPane);
        root.setRight(mainControlPanel);

        Scene scene = new Scene(root, SCREEN_SIZE_X + 320, SCREEN_SIZE_Y);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Particles Game");
        primaryStage.show();

        instantiateParticles(PARTICLE_SIZE, SCREEN_SIZE_X - 200, SCREEN_SIZE_Y - 200);

        simulationPane.setOnMouseClicked(e -> {
            if (PAUSED) return; // Não permite interação quando pausado
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

                if (PAUSED) {
                    return; // Não atualiza a física se o jogo estiver pausado
                }

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

        // Atualizar física
        for (Particle p : particles) {
            p.updatePhysics(FIXED_STEP_SECONDS);
        }

        // Verificar colisões e fundir partículas (se ativado)
        if (COLLISIONS_ENABLED) {
            Set<Particle> particlesToRemove = new HashSet<>();
            List<Particle> newParticles = new ArrayList<>();
            
            for (int i = 0; i < particles.size(); i++) {
                Particle p1 = particles.get(i);
                if (particlesToRemove.contains(p1)) continue;
                
                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p2 = particles.get(j);
                    if (particlesToRemove.contains(p2)) continue;
                    
                    if (areParticlesTouching(p1, p2)) {
                        particlesToRemove.add(p1);
                        particlesToRemove.add(p2);
                        
                        // Criar nova partícula fundida
                        Particle merged = mergeParticles(p1, p2);
                        newParticles.add(merged);
                        break; // Sair após fundir p1 com uma partícula
                    }
                }
            }
            
            // Remover partículas fundidas
            particles.removeAll(particlesToRemove);
            simulationPane.getChildren().removeAll(particlesToRemove.stream()
                    .map(Particle::getCircle)
                    .toList());
            
            // Adicionar novas partículas fundidas
            particles.addAll(newParticles);
            for (Particle p : newParticles) {
                simulationPane.getChildren().add(p.getCircle());
            }
        }
        
        // Verificar colisões com as paredes
        List<Particle> wallCollisions = new ArrayList<>();
        for (Particle p : particles) {
            if (isTouchingWall(p)) {
                wallCollisions.add(p);
            }
        }
        for (Particle p : wallCollisions) {
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
    }

    private boolean areParticlesTouching(Particle p1, Particle p2) {
        double dx = p2.getPosX() - p1.getPosX();
        double dy = p2.getPosY() - p1.getPosY();
        double distance = Math.sqrt(dx*dx + dy*dy);
        return distance < (p1.getRadius() + p2.getRadius());
    }
    
    private Particle mergeParticles(Particle p1, Particle p2) {
        // Calcular nova massa (soma das massas)
        double newMass = p1.getMass() + p2.getMass();
        
        // Calcular novo tamanho (proporcional à raiz quadrada da massa para manter densidade)
        double newSize = Math.sqrt(newMass) * 5; // Fator de escala para manter proporção
        
        // Calcular posição média (centro de massa)
        double newX = (p1.getPosX() * p1.getMass() + p2.getPosX() * p2.getMass()) / newMass;
        double newY = (p1.getPosY() * p1.getMass() + p2.getPosY() * p2.getMass()) / newMass;
        
        // Calcular nova velocidade (conservação do momento)
        double newVx = (p1.getVelocityX() * p1.getMass() + p2.getVelocityX() * p2.getMass()) / newMass;
        double newVy = (p1.getVelocityY() * p1.getMass() + p2.getVelocityY() * p2.getMass()) / newMass;
        
        // Criar nova partícula
        Particle merged = new Particle(newSize, newX, newY);
        merged.setMass(newMass);
        merged.setVelocity(newVx, newVy);
        
        // Usar cor da partícula com maior massa
        if (p1.getMass() > p2.getMass()) {
            merged.getCircle().setFill(p1.getCircle().getFill());
        } else {
            merged.getCircle().setFill(p2.getCircle().getFill());
        }
        
        return merged;
    }

    private boolean isTouchingWall(Particle p) {
        double radius = p.getRadius();
        double x = p.getPosX();
        double y = p.getPosY();

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
}