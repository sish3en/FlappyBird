import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth = 360;
    private final int boardHeight = 640;

    private Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;
    private Bird bird;
    private ArrayList<Pipe> pipes;
    private Timer gameLoop, placePipeTimer;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private double score = 0;
    private double highestScore = ScoreManager.getHighestScore();

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        try {
            backgroundImg = new ImageIcon(getClass().getResource("/flappybirdbg.gif")).getImage();
            birdImg = new ImageIcon(getClass().getResource("/flappybird.gif")).getImage();
            topPipeImg = new ImageIcon(getClass().getResource("/toppipe.png")).getImage();
            bottomPipeImg = new ImageIcon(getClass().getResource("/bottompipe.png")).getImage();
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
        }

        bird = new Bird(birdImg, boardWidth / 8, boardHeight / 2, 40, 30);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);
    }

    private void placePipes() {
        int pipeWidth = 60;
        int pipeHeight = 450;
        int randomY = (int) (-pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int opening = boardHeight / 4;

        pipes.add(new Pipe(topPipeImg, boardWidth, randomY, pipeWidth, pipeHeight));
        pipes.add(new Pipe(bottomPipeImg, boardWidth, randomY + pipeHeight + opening, pipeWidth, pipeHeight));
    }

    private void resetGame() {
        bird = new Bird(birdImg, boardWidth / 8, boardHeight / 2, 40, 30);
        pipes.clear();
        score = 0;
        gameOver = false;
        gameStarted = false;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        bird.draw(g);
        for (Pipe p : pipes) p.draw(g);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));

        if (!gameStarted) {
            g.drawString("Get Started", 80, boardHeight / 2 - 40);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to play", 75, boardHeight / 2);
        } else if (gameOver) {
            g.drawString("Game Over", 90, boardHeight / 2 - 60);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + (int) score, 125, boardHeight / 2 - 20);
            g.drawString("Highest: " + (int) highestScore, 120, boardHeight / 2 + 10);
            g.drawString("Press SPACE to restart", 70, boardHeight / 2 + 50);
        } else {
            g.drawString("Score: " + (int) score, 10, 35);
            g.drawString("Best: " + (int) highestScore, 190, 35);
        }
    }

    private void updateGame() throws GameOverException {
        bird.update();
        for (Pipe p : pipes) {
            p.update();
            if (!p.isPassed() && bird.getX() > p.getX() + p.width) {
                score += 0.5;
                p.setPassed(true);
            }
            if (bird.isColliding(p)) {
                throw new GameOverException("You hit a pipe!");
            }
        }

        if (bird.getYPosition() >= boardHeight - bird.height) {
            throw new GameOverException("You fell!");
        }

        assert score >= 0 : "Score cannot be negative!";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (gameStarted && !gameOver) {
                updateGame();
            }
        } catch (GameOverException ex) {
            gameOver = true;
            placePipeTimer.stop();
            gameLoop.stop();
            ScoreManager.saveScore(score);
            highestScore = Math.max(highestScore, score);
            System.out.println("Game Over: " + ex.getMessage());
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();
                placePipeTimer.start();
                return;
            }
            if (!gameOver) {
                bird.jump();
            } else {
                resetGame();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
