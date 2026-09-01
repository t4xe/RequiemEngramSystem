package me.t4xe.engram.reward;

public class RewardHelper {
    private final double chance;
    private final String command;
    private final String message;

    public RewardHelper(double chance, String command, String message) {
        this.chance = chance;
        this.command = command;
        this.message = message;
    }

    public double getChance() { return chance; }
    public String getCommand() { return command; }
    public String getMessage() { return message; }
}
