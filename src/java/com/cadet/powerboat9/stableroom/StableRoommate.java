package com.cadet.powerboat9.stableroom;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class StableRoommate {
    static final byte NONE = 0;
    static final byte ASKING = 1;
    static final byte ASKED_BY = 2;
    static final byte REJECTED = 3;

    public static int getMapping(int x, int y) {
        if (x == y) return -1;
        if (x > y) {
            int t = x;
            x = y;
            y = t;
        }
        // Average rows 1 and y - 1, then multiply by the number of rows between them inclusively
        //int beforeRow = (1 + (y - 1)) / 2 * (y - 1);
        int beforeRow = (int) ((y / 2.0) * (y - 1));
        // Adds column
        return beforeRow + x;
    }

    /*

    // The number of objects we're matching
    int len;
    // The number of objects in each ranking list (len - 1)
    int lenMinusOne;
    // The last index in each object's ranking list (len - 2)
    int lenMinusTwo;
    // Whether this problem is done
    boolean isDone = false;
    // What this problem will return
    int[] ret;
    // Whether an object has been matched yet
    boolean[] isMatched;
    // The matrix used to store match and reject information
    byte[] matches;
    // The number of objects that have been matched
    int numMatched = 0;
    // How every object ranks every other object
    int[] ranking;
    // The current positions objects are at on their ranking lists
    int[] posCache;

    // Stores the size of each object's ranking list
    int[] numInRankingList;

    // Acts as a lookup table for objects in another object's ranking table
    int[] lookupItemInRankingList;

    /**
     * For internal use
     * Creates a new StableRoommate object
     *
     * @param nObjects The number of objects we're matching
     * @param rankingIn All object's ranking lists, as a table
     *[/
    private StableRoommate(int nObjects, int[] rankingIn) {
        len = nObjects;
        lenMinusOne = len - 1;
        lenMinusTwo = len - 2;
        int tSize = len * lenMinusOne;
        ret = new int[nObjects];
        isMatched = new boolean[nObjects];
        matches = new byte[tSize];
        ranking = rankingIn;
        posCache = new int[nObjects];
        numInRankingList = new int[nObjects];
        for (int i = 0; i < nObjects; i++) {
            numInRankingList[i] = lenMinusOne;
        }
        lookupItemInRankingList = new int[tSize];
        for (int i = 0; i < nObjects; i++) {
            int iRowOffset = i * lenMinusOne;
            for (int j = 0; j < lenMinusOne; j++) {

            }
        }
    }

    /**
     * Do step one
     * Make sure everyone has successful proposals
     *[/
    private void stepOne() {
        for (int i = 0; i < len; i++) {
            propose(i);
            if (numMatched >= len) {
                isDone = true;
                return;
            }
        }
    }

    private void trimRejects(int as) {
        if (isMatched[as]) return;
        int toIndex = 0;
        int fromIndex = 0;
        int rowOffset = as * lenMinusOne;
        while (fromIndex < numInRankingList[as]) {
            int checkAddr = rowOffset + fromIndex;
            if (!hasBeenRejected(checkAddr)) {
                if (toIndex != fromIndex) {
                    int writeAddr = rowOffset + toIndex;
                    matches[writeAddr] = matches[checkAddr];
                    ranking[writeAddr] = ranking[checkAddr];
                }
                toIndex++;
            }
            fromIndex++;
        }
        if (toIndex == 0) {
            takeout(as);
        } else {
            numInRankingList[as] = toIndex;
        }
    }

    private void eliminateRivals(int as, int pos) {
        int asRowOffset = as * lenMinusOne;
        for (int eliminatePos = pos + 1; eliminatePos < numInRankingList[as]; eliminatePos++) {
            int eAddr = asRowOffset + eliminatePos;
            if (matches[eAddr] == ASKED_BY) {
                // Great, we found a rival
            } else {
                // Reject them anyway
                matches[eAddr] = REJECTED;
            }
        }
    }

    private void proposeNew(int as) {
        int pos = 0;
        int rowOffset = as * lenMinusOne;
        while (pos < numInRankingList[as]) {
            int posAddr = rowOffset + pos;
            updateRejection(as);
            switch (ranking[rowOffset + pos]) {
                case NONE:
                    // Try to match
                case ASKING:
                    // We're good, we can exit
                    // Also, pos will always be 0, so we can exit quickly
                    return;
                case ASKED_BY:
                    // We match perfectly
                    match();
                case REJECTED:
                    // We've been rejected, loop again
            }
        }
    }

    private void updateRejection(int addr) {
        if (isMatched[ranking[addr]]) matches[addr] = REJECTED;
    }

    private boolean hasBeenRejected(int addr) {
        updateRejection(addr);
        return matches[addr] == REJECTED;
    }

    private void rejectStepOne(int as, int index) {
        int asRowOffset = as * lenMinusOne;
        int asAddr = asRowOffset + index;
        int target = ranking[asAddr];
        if (isMatched[target]) {
            matches[asAddr] = REJECTED;
        } else {
            int targetRowOffset = target * lenMinusOne;
            int myAddrInTarget = targetRowOffset;
            boolean ok = false;
            for (int i = 0; i < numInRankingList[target]; i++) {
                if (ranking[myAddrInTarget] == as) {
                    ok = true;
                    break;
                }
                myAddrInTarget++;
            }
            if (ok) {
                matches[myAddrInTarget] = REJECTED;
            }
            matches[asAddr] = REJECTED;
        }
    }

    /**
     * Propose as an object, working down that object's ranking list
     *
     * @param as The object we're proposing as
     *[/
    private void propose(int as) {
        //Tester.printBoardOne(isMatched, matches, len);
        ///Tester.printRankOne(ranking, len);
        //System.out.println("Proposing as " + as);
        if (isMatched[as]) return;

        int asRowOffset = as * lenMinusOne;
        int pos = posCache[as];
        int asAddr = asRowOffset + pos;

        while (true) {
            int target = ranking[asAddr];
            if (isMatched[target]) matches[asAddr] = REJECTED;
            int targetRowOffset = target * lenMinusOne;
            int targetAddr = targetRowOffset;
            while (ranking[targetAddr] != as) targetAddr++;
            if (matches[targetAddr] == REJECTED) matches[asAddr] = REJECTED;
            switch (matches[asAddr]) {
                case ASKED_BY: // We match perfectly
                    //System.out.println("Matching perfectly (" + as + ", " + target + ")");
                    match(as, target); // No need to update pos cache
                    // Make the object who wanted us propose again
                    for (int i = pos + 1; i < lenMinusOne; i++) {
                        if (matches[asRowOffset + i] == ASKED_BY) {
                            int t = asRowOffset + i;
                            matches[t] = REJECTED;
                            propose(ranking[t]);
                            break;
                        }
                    }
                    // Make the object who wanted them propose again
                    for (int i = posCache[target] + 1; i < lenMinusOne; i++) {
                        if (matches[targetRowOffset + i] == ASKED_BY) {
                            int t = targetRowOffset + i;
                            matches[t] = REJECTED;
                            propose(ranking[t]);
                            return;
                        }
                    }
                    /*
                    for (int i = 0; i < len; i++) { // Update everyone
                        propose(i);
                    }
                    *[/
                    // We don't need a return statement, because switch statements are pretty fun
                case ASKING: // Why did you even call this method?
                    return; // By definition this must have been our first loop, so no need to update pos
                case NONE: // Request a match
                    //System.out.println("Matching with " + target);
                    matches[asAddr] = ASKING;
                    matches[targetAddr] = ASKED_BY;
                    posCache[as] = pos; // Store pos
                    // Reject everyone below us
                    int loopBound = targetRowOffset + lenMinusOne;
                    for (int rejectAddr = targetAddr + 1; rejectAddr < loopBound; rejectAddr++) {
                        if (matches[rejectAddr] == ASKED_BY) { // Someone needs to be rejected
                            matches[rejectAddr] = REJECTED;
                            propose(ranking[rejectAddr]); // They need to find someone new
                            break; // We can stop now, as they rejected every object below them
                        } else matches[rejectAddr] = REJECTED;
                    }
                    return;
                case REJECTED: // We've been rejected
                    //System.out.println("Rejected");
                    if ((++pos) >= lenMinusOne) { // No one wants us
                        takeout(as);
                        return;
                    }
                    asAddr++;
                    // Loop again
            }
        }
    }

    /**
     * Takes an object out of the running
     * This object won't be matched with anything
     *
     * @param object The object to take out
     *[/
    private void takeout(int object) {
        isMatched[object] = true;
        ret[object] = -1;
        numMatched++;
    }

    /**
     * Matches up two objects
     * Don't use as a general form of takeout()
     *
     * @param object The first object
     * @param with   The second object
     *[/
    private void match(int object, int with) {
        isMatched[object] = true;
        ret[object] = with + 1;
        isMatched[with] = true;
        ret[with] = object + 1;
        numMatched += 2;
    }

    /**
     * Do step two
     *[/
    private void stepTwo() {
        if (isDone) return;
        //System.out.println("Starting step two");
        // Removes all rejected elements from every ranking list
        for (int i = 0; i < len; i++) {
            if (isMatched[i]) continue; // If this has already been matched, ignore it
            int rowOffset = i * lenMinusOne;
            // Start copying values around
            int indexTo = 0;
            int indexFrom = 0;
            while (indexFrom < lenMinusOne) {
                if (isMatched[ranking[rowOffset + indexFrom]]) {
                    matches[rowOffset + indexFrom] = REJECTED;
                    //System.out.println(i + ":" + indexFrom + " should be rejected");
                }
                if (matches[rowOffset + indexFrom] != REJECTED) {
                    if (indexTo != indexFrom) ranking[rowOffset + indexTo] = ranking[rowOffset + indexFrom];
                    indexTo++;
                }
                indexFrom++;
            }
            if (indexTo == 0) {
                takeout(i);
                if (numMatched >= len) {
                    isDone = true;
                    return;
                }
            } else {
                numInRankingList[i] = indexTo;
            }
        }
        Tester.printRankTwo(ranking, isMatched, numInRankingList, len);
        // Start finding stable loops
        ArrayList<Integer> stableLoop;
        for (int i = 0; i < len; i++) {
            if (isMatched[i]) continue;
            //System.out.println("Finding loop for " + i);
            while ((stableLoop = findLoop(i)) != null) {
                for (int j = stableLoop.size() - 1; j > 1; j -= 2) {
                    // Removes pairs in the stable loop
                    int p1 = stableLoop.get(j);
                    int p2 = stableLoop.get(j - 1);
                    int p1IndexOfOther = p1 * lenMinusOne;
                    while (ranking[p1IndexOfOther] != p2) {
                        p1IndexOfOther++;
                    }
                    //System.out.println("p1: " + p1);
                    //System.out.println("p1i: " + p1IndexOfOther);
                    System.arraycopy(ranking, p1IndexOfOther + 1, ranking, p1IndexOfOther, numInRankingList[p1] - p1IndexOfOther - 1);
                    int p2IndexOfOther = p2 * lenMinusOne;
                    while (ranking[p2IndexOfOther] != p1) {
                        p2IndexOfOther++;
                    }
                    //System.out.println("p2: " + p2);
                    //System.out.println("p2i: " + p2IndexOfOther);
                    System.arraycopy(ranking, p2IndexOfOther + 1, ranking, p2IndexOfOther, numInRankingList[p2] - p2IndexOfOther - 1);
                }
            }
            match(i, ranking[i * lenMinusOne]);
            for (int j = 0; j < len; j++) {
                if (isMatched[j]) continue;
                int rOff = j * lenMinusOne;
                for (int k = 0; k < numInRankingList[j]; k++) {
                    if (ranking[rOff + k] == i) {
                        if (k != (numInRankingList[j] - 1)) {
                            //System.out.println(ranking.length + ", " + (rOff + k) + ", " + numInRankingList[j]);
                            System.arraycopy(ranking, rOff + k + 1, ranking, rOff + k, numInRankingList[j] - k - 1);
                        }
                        numInRankingList[j]--;
                        break;
                    }
                }
            }
        }
    }

    */

    public static <T> int[] runProblem(T[] data, BiFunction<T, T, Double> cmp) {
        return runProblem(data, cmp, Double.NEGATIVE_INFINITY);
    }

    // I wish this was c code
    public static <T> int[] runProblem(T[] data, BiFunction<T, T, Double> cmp, double minScore) {
        if (data.length == 0) return new int[0];
        else if (data.length == 1) return new int[] {-1};
        else if (data.length == 2) return new int[] {1, 0};
        int len = data.length;
        int lenMinusOne = len - 1;
        int lenMinusTwo = len - 2;
        int rowOffset; // Used to store len * row
        // Generates a score table
        double[] scores = new double[len * (len + 1) / 2];
        {
            int x;
            int i = 0;
            for (int y = 1; y < len; y++) {
                for (x = 0; x < y; x++) {
                    //System.out.println("(" + x + ", " + y + "): " + i + "|" + getMapping(x, y));
                    scores[i++] = cmp.apply(data[x], data[y]);
                }
            }
        }
        // Generates a proposal ranking for every item
        // It's (len) by (len - 1)
        /*
        int[] ranking = new int[len * (len - 1)];
        {
            for (int i = 0; i < len; i++) {
                rowOffset = i * lenMinusOne;
                // Generate list
                int n = 0;
                for (int j = 0; j < len; j++) {
                    if (j == i) continue;
                    ranking[rowOffset + (n++)] = j;
                }
                boolean c = true;
                while (c) {
                    c = false;
                    for (int j = 0; j < lenMinusTwo; j++) {
                        int mLow = getMapping(i, ranking[rowOffset + j]);
                        int mHigh = getMapping(i, ranking[rowOffset + j + 1]);
                        if (scores[mLow] < scores[mHigh]) {
                            c = true;
                            int tSwap1 = ranking[rowOffset + j];
                            ranking[rowOffset + j] = ranking[rowOffset + j + 1];
                            ranking[rowOffset + j + 1] = tSwap1;
                        }
                    }
                }
            }
        }
        */

        //Tester.printScores(ranking, scores, len);
        //Tester.printRankOne(ranking, len);

        FunMatrix m = new FunMatrix(scores, len);
        if (minScore > Double.NEGATIVE_INFINITY) m.rejectBelow(minScore, scores);
        //System.out.println("Executing...");
        return m.exec();
    }
}