package src;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class GayFlappyBird implements ActionListener, KeyListener {

    public static GayFlappyBird flappyBird;
    public final int WIDTH = 1500, HEIGHT = 800;
    public Renderer renderer;
    public Rectangle birdBounds;
    private Image birdImage;
    public ArrayList<Pipe> pipes;
    public int ticks, yMotion, score;
    public int highScore;
    public boolean gameOver, started;
    private Preferences prefs;
    public int jumpHeight;
    public int speed;

    public GayFlappyBird() {
        JFrame jframe = new JFrame();
        Timer timer = new Timer(20, this);

        renderer = new Renderer();
        jframe.add(renderer);
        jframe.setTitle("Gay Flappy Bird");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(WIDTH, HEIGHT);
        jframe.addKeyListener(this);
        jframe.setResizable(false);
        jframe.setVisible(true);

        // Load bird image
        try {
            birdImage = ImageIO.read(new File("public/gayflapsprite.jpg"));
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

        jumpHeight = 10;

        timer.start();
    }

    public void addPipe(boolean start) {
        int space = 300;
        int width = 100;
        int height = 50 + (int) (Math.random() * 300);

        if (start) {
            pipes.add(new Pipe(new Rectangle(WIDTH + width + pipes.size() * 300, HEIGHT - height - 120, width, height)));
            pipes.add(new Pipe(new Rectangle(WIDTH + width + (pipes.size() - 1) * 300, 0, width, HEIGHT - height - space)));
        } else {
            pipes.add(new Pipe(new Rectangle(pipes.get(pipes.size() - 1).bounds.x + 600, HEIGHT - height - 120, width, height)));
            pipes.add(new Pipe(new Rectangle(pipes.get(pipes.size() - 1).bounds.x, 0, width, HEIGHT - height - space)));
        }
    }

    public void paintPipe(Graphics g, Pipe pipe) {
        g.setColor(Color.green.darker());
        g.fillRect(pipe.bounds.x, pipe.bounds.y, pipe.bounds.width, pipe.bounds.height);
    }

    public void jump() {
        if (gameOver) {
            if (score > highScore) {
                highScore = score;
                prefs.putInt("highScore", highScore); // Save high score
            }

            Timer delayTimer = new Timer(5, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    birdBounds = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
                    pipes.clear();
                    yMotion = 0;
                    score = 0;

                    addPipe(true);
                    addPipe(true);
                    addPipe(true);
                    addPipe(true);

                    gameOver = false;
                    ((Timer) evt.getSource()).stop();
                }
            });

            delayTimer.setRepeats(false);
            delayTimer.start();
        }

        if (!started) {
            started = true;
        }

        if (yMotion > 0) {
            yMotion = 0;
        }

        speed = score / 2 + 7;
        if (speed < 10) {
            speed = 10;
        }

        yMotion -= jumpHeight;
    }


    @Override
public void actionPerformed(ActionEvent e) {
    ticks++;

    if (started) {
        // Move pipes regardless of game state
        for (Pipe pipe : pipes) {
            pipe.bounds.x -= speed;
        }

        if (!gameOver) {
            // Update bird's yMotion as usual until game over
            if (ticks % 2 == 0 && yMotion < 15) {
                yMotion += 2;
            }
        } else {
            // When game is over, set yMotion to make the bird fall steadily
            if (yMotion < 15) {
                yMotion = 15; // Set a constant fall speed
            }
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);

            if (pipe.bounds.x + pipe.bounds.width < 0) {
                pipes.remove(pipe);

                if (pipe.bounds.y == 0) {
                    addPipe(false);
                }
            }
        }

        birdBounds.y += yMotion;

        // Scoring logic (only if game is not over)
        for (Pipe pipe : pipes) {
            if (!gameOver && pipe.bounds.y == 0 
                && !pipe.scored 
                && birdBounds.x + birdBounds.width / 2 > pipe.bounds.x + pipe.bounds.width / 2 - 10 
                && birdBounds.x + birdBounds.width / 2 < pipe.bounds.x + pipe.bounds.width / 2 + 10) {
                score++;
                pipe.scored = true; // Mark this pipe as scored
            }

            // Collision detection
            if (pipe.bounds.intersects(birdBounds)) {
                gameOver = true;
                if (score > highScore) {
                    highScore = score;
                    prefs.putInt("highScore", highScore); // Save high score
                }

                if (birdBounds.x <= pipe.bounds.x) {
                    birdBounds.x = pipe.bounds.x - birdBounds.width;
                } else {
                    if (pipe.bounds.y != 0) {
                        birdBounds.y = pipe.bounds.y - birdBounds.height;
                    } else if (birdBounds.y < pipe.bounds.height) {
                        birdBounds.y = pipe.bounds.height;
                    }
                }
            }
        }

        // Prevent the bird from going above or below the screen limits
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

        if (birdImage != null) {
            g.drawImage(birdImage, birdBounds.x, birdBounds.y, birdBounds.width, birdBounds.height, null);
        } else {
            g.setColor(Color.blue);
            g.fillRect(birdBounds.x, birdBounds.y, birdBounds.width, birdBounds.height);
        }

        for (Pipe pipe : pipes) {
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
        flappyBird = new GayFlappyBird();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public class Renderer extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            GayFlappyBird.this.repaint(g);
        }
    }
}

// New Pipe class definition
class Pipe {
    public Rectangle bounds;
    public boolean scored;

    public Pipe(Rectangle bounds) {
        this.bounds = bounds;
        this.scored = false;
    }
}
