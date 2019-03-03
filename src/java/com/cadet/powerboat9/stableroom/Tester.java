
package com.cadet.powerboat9.stableroom;

import static com.cadet.powerboat9.stableroom.StableRoommate.*;

public class Tester {
    public static void main(String[] args) {
        /*
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < x; y++) {
                if (StableRoommate.getMapping(x, y) != StableRoommate.getMapping(y, x)) {
                    System.out.println("(" + x + ", " + y + ") != (" + y + ", " + x + ")");
                }
            }
        }
        */
        /*
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < x; y++) {
                System.out.println("(" + x + ", " + y + "): " + StableRoommate.getMapping(x, y));
            }
        }
        */
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.println("#####" + i + ":" + j + "#####");
                if (test(i)) {
                    System.out.println(i + ": true");
                } else {
                    System.out.println(i + ": false");
                    return;
                }
            }
        }
    }

    public static boolean test(int num) {
        Double[] objects = new Double[num];
        for (int i = 0; i < objects.length; i++) objects[i] = Math.random() * 100;
        int[] ret = StableRoommate.runProblem(objects, Tester::score);
        for (int i = 0; i < ret.length; i++) {
            System.out.println("#" + i + ": " + ret[i]);
        }
        boolean v = true;
        for (int i = 0; i < objects.length; i++) {
            if (ret[i] == -1) continue;
            if (ret[ret[i]] != i) {
                System.out.println("Mismatch with " + i + " and " + ret[i]);
                v = false;
            }
        }
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < i; j++) {
                if (willEnlope(ret, objects, i, j)) {
                    System.out.println("Could have matched (" + i + ", " + j + ")");
                    v = false;
                }
            }
        }
        return v;
    }

    public static boolean prefersOther(double as, double dCur, double dOther) {
        boolean b = score(as, dCur) < score(as, dOther);
        //System.out.println(b);
        return b;
    }

    public static boolean willEnlope(double partner1, double as1, double as2, double partner2) {
        return prefersOther(as1, partner1, as2) && prefersOther(as2, partner2, as1);
    }

    public static boolean willEnlope(int[] ret, Double[] objs, int as1, int as2) {
        if (ret[as1] == -1) {
            if (ret[as2] == -1) return false;
            return prefersOther(objs[as2], objs[ret[as2]], objs[as1]);
        } else if (ret[as2] == -1) {
            return prefersOther(objs[as1], objs[ret[as1]], objs[as2]);
        }
        return willEnlope(objs[ret[as1]], objs[as1], objs[as2], objs[ret[as2]]);
    }

    public static double score(double d1, double d2) {
        return -Math.abs(d2 - d1);
    }

    public static void printScores(int[] ranking, double[] scores, int len) {
        int lenMinusOne = len - 1;
        for (int i = 0; i < len; i++) {
            int rOff = i * lenMinusOne;
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < lenMinusOne; j++) {
                s.append(scores[StableRoommate.getMapping(i, ranking[rOff + j])]);
                s.append('\t');
            }
            System.out.println(s.toString());
        }
    }

    public static void printRankTwo(int[] ranking, boolean[] isMatched, int[] numItems, int len) {
        int lenMinusOne = len - 1;
        for (int i = 0; i < len; i++) {
            if (isMatched[i]) {
                System.out.println("C");
            } else {
                int rOff = i * lenMinusOne;
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < numItems[i]; j++) {
                    s.append(ranking[rOff + j]);
                    s.append('\t');
                }
                System.out.println(s.toString());
            }
        }
    }

    public static void printBoardOne(boolean[] isMatched, byte[] matching, int len) {
        int lenMinusOne = len - 1;
        for (int i = 0; i < len; i++) {
            if (isMatched[i]) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < lenMinusOne; j++) s.append('C');
                System.out.println(s.toString());
                continue;
            }
            int rowOffset = i * lenMinusOne;
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < lenMinusOne; j++) {
                switch (matching[rowOffset + j]) {
                    case NONE:
                        s.append('-');
                        break;
                    case ASKING:
                        s.append('?');
                        break;
                    case ASKED_BY:
                        s.append('#');
                        break;
                    case REJECTED:
                        s.append('X');
                }
            }
            System.out.println(s.toString());
        }
    }
}