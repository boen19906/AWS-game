import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class FlappyBird implements ActionListener, KeyListener {

    public static FlappyBird flappyBird;
    public final int WIDTH = 1500, HEIGHT = 800;
    public Renderer renderer;
    public Rectangle birdBounds;
    public Image birdImage;
    public ArrayList<Rectangle> pipes;
    public int ticks, yMotion, score;
    public int highScore;
    public boolean gameOver, started;
    private Preferences prefs;

    public FlappyBird() {
        JFrame jframe = new JFrame();
        Timer timer = new Timer(20, this);

        renderer = new Renderer();
        jframe.add(renderer);
        jframe.setTitle("Flappy Bird");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(WIDTH, HEIGHT);
        jframe.addKeyListener(this);
        jframe.setResizable(false);
        jframe.setVisible(true);

        // Load bird image
        try {
            birdImage = ImageIO.read(new File("gayflapsprite.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize Preferences to store high score
        prefs = Preferences.userRoot().node(this.getClass().getName());
        highScore = prefs.getInt("highScore", 0);

        birdBounds = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
        pipes = new ArrayList<>();
        addPipe(true);
        addPipe(true);
        addPipe(true);
        addPipe(true);

        timer.start();
    }

    public void addPipe(boolean start) {
        int space = 300;
        int width = 100;
        int height = 50 + (int) (Math.random() * 300);

        if (start) {
            pipes.add(new Rectangle(WIDTH + width + pipes.size() * 300, HEIGHT - height - 120, width, height));
            pipes.add(new Rectangle(WIDTH + width + (pipes.size() - 1) * 300, 0, width, HEIGHT - height - space));
        } else {
            pipes.add(new Rectangle(pipes.get(pipes.size() - 1).x + 600, HEIGHT - height - 120, width, height));
            pipes.add(new Rectangle(pipes.get(pipes.size() - 1).x, 0, width, HEIGHT - height - space));
        }
    }

    public void paintPipe(Graphics g, Rectangle pipe) {
        g.setColor(Color.green.darker());
        g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
    }

    public void jump() {
        if (gameOver) {
            // Update high score if current score is greater
            if (score > highScore) {
                highScore = score;
                prefs.putInt("highScore", highScore); // Save high score
            }
        
            // Create a delay timer for 3 seconds (3000 ms)
            Timer delayTimer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Reset game state after delay
                    birdBounds = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
                    pipes.clear();
                    yMotion = 0;
                    score = 0;
        
                    addPipe(true);
                    addPipe(true);
                    addPipe(true);
                    addPipe(true);
        
                    gameOver = false; // Game can restart now
                    ((Timer) evt.getSource()).stop(); // Stop the delay timer
                }
            });
        
            delayTimer.setRepeats(false); // Run only once
            delayTimer.start(); // Start the delay
        }
        

        if (!started) {
            started = true;
        }

        if (yMotion > 0) {
            yMotion = 0;
        }

        yMotion -= 10;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int speed = 10;

        ticks++;

        if (started) {
            for (int i = 0; i < pipes.size(); i++) {
                Rectangle pipe = pipes.get(i);
                pipe.x -= speed;
            }

            if (ticks % 2 == 0 && yMotion < 15) {
                yMotion += 2;
            }

            for (int i = 0; i < pipes.size(); i++) {
                Rectangle pipe = pipes.get(i);

                if (pipe.x + pipe.width < 0) {
                    pipes.remove(pipe);

                    if (pipe.y == 0) {
                        addPipe(false);
                    }
                }
            }

            birdBounds.y += yMotion;

            for (Rectangle pipe : pipes) {
                if (pipe.y == 0 && birdBounds.x + birdBounds.width / 2 > pipe.x + pipe.width / 2 - 10 && birdBounds.x + birdBounds.width / 2 < pipe.x + pipe.width / 2 + 10) {
                    score++;
                }

                if (pipe.intersects(birdBounds)) {
                    gameOver = true;

                    if (birdBounds.x <= pipe.x) {
                        birdBounds.x = pipe.x - birdBounds.width;
                    } else {
                        if (pipe.y != 0) {
                            birdBounds.y = pipe.y - birdBounds.height;
                        } else if (birdBounds.y < pipe.height) {
                            birdBounds.y = pipe.height;
                        }
                    }
                }
            }

            if (birdBounds.y > HEIGHT - 120 || birdBounds.y < 0) {
                gameOver = true;
            }

            if (birdBounds.y + yMotion >= HEIGHT - 120) {
                birdBounds.y = HEIGHT - 120 - birdBounds.height;
            }
        }

        renderer.repaint();
    }

    public void repaint(Graphics g) {
        g.setColor(Color.cyan);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.orange);
        g.fillRect(0, HEIGHT - 120, WIDTH, 120);

        g.setColor(Color.green);
        g.fillRect(0, HEIGHT - 120, WIDTH, 20);

        // Draw bird image instead of rectangle
        if (birdImage != null) {
            g.drawImage(birdImage, birdBounds.x, birdBounds.y, birdBounds.width, birdBounds.height, null);
        } else {
            g.setColor(Color.red);
            g.fillRect(birdBounds.x, birdBounds.y, birdBounds.width, birdBounds.height);
        }

        for (Rectangle pipe : pipes) {
            paintPipe(g, pipe);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 100));

        if (!started) {
            g.drawString("Press SPACE to Start", 100, HEIGHT / 2 - 50);
        }

        if (gameOver) {
            g.drawString("Game Over", 200, HEIGHT / 2 - 50);
            g.drawString("High Score: " + highScore, WIDTH / 2 - 200, HEIGHT / 2 + 50);
        }

        if (!gameOver && started) {
            g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
        }
    }

    public static void main(String[] args) {
        flappyBird = new FlappyBird();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public class Renderer extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            flappyBird.repaint(g);
        }
    }
}
