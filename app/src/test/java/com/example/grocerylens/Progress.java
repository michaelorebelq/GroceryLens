package com.example.grocerylens;

public class Progress {
    // returns the users progress as a
    // capped percentage from 0 to 100
    public static int getProgressPercentage(int viewed, int goal) {

        int percentage = (viewed * 100) / goal;
        return Math.min(percentage, 100);
    }

    // the badge level based
    // on how many recipes the user has viewed
    public static int getUserBadgeLevel(int viewed) {
        if (viewed >= 100) return 5;
        if (viewed >= 50) return 4;
        if (viewed >= 25) return 3;
        if (viewed >= 10) return 2;
        return 1;
    }
}
