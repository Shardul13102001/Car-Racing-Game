package CarRacingGame;

import java.util.Random;
import java.util.Scanner;

public class RacingGame {
    private static final int WIDTH = 30;   // Width of the track
    private static final int HEIGHT = 15;  // Height of the track
    private static final char OBSTACLE = 'X';  // Obstacle symbol
    private static final char COIN = '$';      // Coin symbol
    private static final char EMPTY = ' ';     // Empty space
    private static final int LANE_WIDTH = 5;   // Width of each lane
    private static final int NUM_LANES = WIDTH / LANE_WIDTH;  // Number of lanes (6)
    private static final int START_LANE = NUM_LANES / 2;  // Start in the middle lane (lane 3)

    private static char[][] track = new char[HEIGHT][WIDTH];
    private static int playerLane = START_LANE;  // Current lane index (0 to 5)
    private static boolean isRunning = true;
    private static int score = 0;
    private static int coins = 0;

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Initialize track with empty spaces
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                track[i][j] = EMPTY;
            }
        }

        System.out.println("🚗 Welcome to Temple Run Racing Game!");
        System.out.println("Controls: 'A' → Move Left, 'D' → Move Right");
        System.out.println("Avoid obstacles (X), collect coins ($)!");

        // Start input thread
        Thread inputThread = new Thread(() -> {
            while (isRunning) {
                if (scanner.hasNext()) {
                    String input = scanner.next().toLowerCase();
                    if (input.equals("a") && playerLane > 0) {
                        playerLane--;  // Move left by one lane
                    } else if (input.equals("d") && playerLane < NUM_LANES - 1) {
                        playerLane++;  // Move right by one lane
                    }
                }
            }
        });

        inputThread.start();

        // Main game loop
        while (isRunning) {
            generateTrack(random);
            renderTrack();
            checkCollision();
            Thread.sleep(200);  // Continuous movement speed
        }

        System.out.println("🔥 Game Over! Final Score: " + score + ", Coins Collected: " + coins);
        scanner.close();
    }

    // Generate track with obstacles and coins
    private static void generateTrack(Random random) {
        // Move the entire track down
        for (int i = HEIGHT - 1; i > 0; i--) {
            System.arraycopy(track[i - 1], 0, track[i], 0, WIDTH);
        }

        // Add new row at the top
        for (int j = 0; j < WIDTH; j++) {
            track[0][j] = EMPTY;
        }

        // Calculate lane centers
        int numLanes = WIDTH / LANE_WIDTH;
        int[] laneCenters = new int[numLanes];
        for (int lane = 0; lane < numLanes; lane++) {
            laneCenters[lane] = lane * LANE_WIDTH + (LANE_WIDTH / 2);
        }

        // Add random obstacles
        if (random.nextInt(10) < 4) {  // 40% chance
            int lane = random.nextInt(numLanes);  // Random lane
            int obstaclePos = laneCenters[lane];  // Use the lane center
            if (obstaclePos < WIDTH) {
                track[0][obstaclePos] = OBSTACLE;
            }
        }

        // Add random coins
        if (random.nextInt(10) < 3) {  // 30% chance
            int lane = random.nextInt(numLanes);  // Random lane
            int coinPos = laneCenters[lane];  // Use the lane center
            if (coinPos < WIDTH) {
                track[0][coinPos] = COIN;
            }
        }

        score++;
        placePlayer();
    }

    // Display the track
    private static void renderTrack() {
        System.out.print("\033[H\033[2J");  // Clear console
        System.out.flush();
        System.out.println("Score: " + score + " | Coins: " + coins);

        // Render track with lanes
        for (char[] row : track) {
            System.out.print("|");  // Start of track
            for (int j = 0; j < row.length; j++) {
                System.out.print(row[j]);
                // Add lane separators every 5 characters
                if ((j + 1) % LANE_WIDTH == 0 && j != row.length - 1) {
                    System.out.print("|");
                }
            }
            System.out.println("|");  // End of track
        }
    }

    // Place the player car
    private static void placePlayer() {
        // Clear previous car position (bottom two rows)
        for (int i = HEIGHT - 2; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (track[i][j] != OBSTACLE && track[i][j] != COIN) {
                    track[i][j] = EMPTY;
                }
            }
        }

        // Car design (2x2 grid)
        String[] car = {
            "[]",  // Top of car
            "[]"   // Bottom of car
        };

        int carWidth = car[0].length();
        int carHeight = car.length;

        // Calculate the center of the lane based on playerLane
        int laneCenter = playerLane * LANE_WIDTH + (LANE_WIDTH / 2);  // Center of the lane
        int carStart = laneCenter - (carWidth / 2);  // Adjust car’s left position to center it

        // Ensure car stays within bounds
        if (carStart < 0) {
            carStart = 0;
            playerLane = 0;  // Adjust lane index if out of bounds
        } else if (carStart + carWidth > WIDTH) {
            carStart = WIDTH - carWidth;
            playerLane = NUM_LANES - 1;  // Adjust lane index if out of bounds
        }

        // Place car at the adjusted position
        for (int i = 0; i < carHeight; i++) {
            for (int j = 0; j < carWidth; j++) {
                int row = HEIGHT - carHeight + i;
                int col = carStart + j;
                if (col < WIDTH) {
                    if (track[row][col] != OBSTACLE && track[row][col] != COIN) {
                        track[row][col] = car[i].charAt(j);
                    }
                }
            }
        }
    }

    // Check for collision with obstacles or coins
    private static void checkCollision() {
        int carWidth = 2;  // Car width from "[]"
        int carBottomRow = HEIGHT - 1;

        // Calculate car’s left position based on playerLane
        int laneCenter = playerLane * LANE_WIDTH + (LANE_WIDTH / 2);
        int carStart = laneCenter - (carWidth / 2);

        // Check car’s position for obstacles or coins
        for (int j = carStart; j < carStart + carWidth; j++) {
            if (j < WIDTH) {
                char cell = track[carBottomRow][j];
                if (cell == OBSTACLE) {
                    isRunning = false;  // Game over on collision
                } else if (cell == COIN) {
                    coins++;  // Collect coin
                    track[carBottomRow][j] = EMPTY;  // Remove collected coin
                }
            }
        }
    }
}
