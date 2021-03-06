package xyz.champrin.simplegame.schedule;

import xyz.champrin.simplegame.SimpleGame;

public class Countdown {

    public static String countDown(int number) {
        switch (number) {
            case 5:
                return getNumber5();
            case 4:
                return getNumber4();
            case 3:
                return getNumber3();
            case 2:
                return getNumber2();
            case 1:
                return getNumber1();
            default:
                return SimpleGame.getInstance().language.translateString("countdownToTheStartOfTheGame", number);
        }
    }

    private static String getNumber5() {
        return  "§c▇▇▇▇▇▇\n" +
                "§c▇           \n" +
                "§c▇▇▇▇▇▇\n" +
                "§c           ▇\n" +
                "§c▇▇▇▇▇▇\n";
    }

    private static String getNumber4() {
        return  "§c▇      ▇\n" +
                "§c▇       ▇\n" +
                "§c▇▇▇▇▇\n" +
                "§c       ▇\n" +
                "§c       ▇\n";
    }

    private static String getNumber3() {
        return  "§e▇▇▇▇▇▇\n" +
                "§e           ▇\n" +
                "§e▇▇▇▇▇▇\n" +
                "§e           ▇\n" +
                "§e▇▇▇▇▇▇\n";
    }

    private static String getNumber2() {
        return  "§e▇▇▇▇▇▇\n" +
                "§e           ▇\n" +
                "§e▇▇▇▇▇▇\n" +
                "§e▇           \n" +
                "§e▇▇▇▇▇▇\n";
    }

    private static String getNumber1() {
        return  "§6          ▇\n" +
                "§6          ▇\n" +
                "§6          ▇\n" +
                "§6          ▇\n" +
                "§6          ▇\n";
    }
}
