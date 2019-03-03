package com.cadet.powerboat9.stableroom;

import java.util.ArrayList;

public class FunMatrix {
    private int[] ret;
    private int[] headers;
    public int[] data;
    private int objCnt;
    private int objCntMinus;
    private int objCntMinusTwo;
    private ArrayList<Integer> proposeQueue = null;

    private boolean isDone = false;

    private static final int MODE_NONE = 0;
    private static final int MODE_ASKING = 1;
    private static final int MODE_ASKED = 2;

    private void flip(int firstAddr) {
        int obj = getObject(firstAddr);
        int headerOff = obj * TOTAL_H_OFFSET;
        int beforeAddr = getPrevAddr(firstAddr);
        int secondAddr = getNextAddr(firstAddr);
        if (secondAddr == -1) {
            // We're flipping the ends of an expression
            secondAddr = getFirstAddr(getObject(firstAddr));
            if (secondAddr == firstAddr) {
                // There's only one item, so we can exit
                return;
            }
        }
        int afterAddr = getNextAddr(secondAddr);
        // Make the item before the pair point to the second item in the pair
        if (beforeAddr == -1) {
            // We need to modify the header
            headers[headerOff + FIRST_OFFSET] = secondAddr;
        } else {
            // We need to modify an element
            data[beforeAddr + AFTER_OFFSET] = secondAddr;
        }
        // Make the item after the pair point to the first item in the pair
        if (afterAddr == -1) {
            // We need to modify the header
            headers[headerOff + LAST_OFFSET] = firstAddr;
        } else {
            // We need to modify an element
            data[afterAddr + BEFORE_OFFSET] = firstAddr;
        }
        // Make the first item point to the second item
        data[firstAddr + BEFORE_OFFSET] = secondAddr;
        // Make the second item point to the first item
        data[secondAddr + AFTER_OFFSET] = firstAddr;
        // Make the first item point to the item after the pair
        data[firstAddr + AFTER_OFFSET] = afterAddr;
        // Make the second item point to the item before the pair
        data[secondAddr + BEFORE_OFFSET] = beforeAddr;
    }

    private static int getIndexValue(int obj, int i) {
        return (i >= obj) ? (i + 1) : i;
    }

    private void printAddr(int addr) {
        if (addr == -1) {
            System.out.println("[NULL]");
        } else {
            /*
            addr = addr / TOTAL_E_OFFSET;
            int obj = addr / objCntMinus;
            int index = addr - obj * objCntMinus;
            System.out.println("OBJECT: " + obj + ", INDEX: " + index + " (" + obj + ":" + getIndexValue(obj, index) + ")");
            */
            System.out.println("OBJECT: " + getObject(addr) + " POINTING " + getObject(getOtherAddr(addr)));
        }
    }

    private int getPredictedAddr(int obj, int v) {
        // If the object number is less than v, use v, otherwise use (v - 1)
        // to account for the lack of the object on its own list
        if (obj == v) return -1;
        return (obj * objCntMinus + ((v > obj) ? (v - 1) : v)) * TOTAL_E_OFFSET;
    }

    private void createElement(int obj, int pos) {
        //System.out.println("Creating element at " + obj + ":" + pos + "(" + obj + ":" + ((pos >= obj) ? (pos + 1) : pos) + ")");
        int addr = (obj * objCntMinus + pos) * TOTAL_E_OFFSET;
        data[addr + BEFORE_OFFSET] = (pos == 0) ? -1 : (addr - TOTAL_E_OFFSET);
        data[addr + AFTER_OFFSET] = (pos == objCntMinusTwo) ? -1 : (addr + TOTAL_E_OFFSET);
        int otherObject = getIndexValue(obj, pos);
        int otherAddr = getPredictedAddr(otherObject, obj);
        data[addr + OTHER_OFFSET] = otherAddr;
        data[addr + ASSOCIATE_OFFSET] = obj;
        // We don't have to set mode, as its zero by default
        //printElement(obj, pos);
    }

    private void printElement(int obj, int pos) {
        int addr = (obj * objCntMinus + pos) * TOTAL_E_OFFSET;
        System.out.println(obj + ":" + pos + " (" + obj + ", " + getIndexValue(obj, pos) + ")");
        System.out.print("BEFORE: ");
        printAddr(getPrevAddr(addr));
        System.out.print("AFTER: ");
        printAddr(getNextAddr(addr));
        System.out.print("OTHER: ");
        printAddr(getOtherAddr(addr));
    }

    public FunMatrix(double[] scores, int objCntIn) {
        objCnt = objCntIn;
        objCntMinus = objCntIn - 1;
        objCntMinusTwo = objCntIn - 2;
        int objCntMinusThree = objCntIn - 3;
        data = new int[objCntIn * objCntMinus * TOTAL_E_OFFSET];
        // Write elements
        for (int i = 0; i < objCntIn; i++) {
            for (int j = 0; j < objCntMinus; j++) {
                createElement(i, j);
            }
        }
        headers = new int[objCntIn * TOTAL_H_OFFSET];
        // Write header values
        for (int i = 0; i < objCntIn; i++) {
            int headerOff = i * TOTAL_H_OFFSET;
            // Sets the first address to its default
            headers[headerOff + FIRST_OFFSET] = i * objCntMinus * TOTAL_E_OFFSET;
            //System.out.print("Setting first address of " + i + " to : ");
            //printAddr(headers[headerOff + FIRST_OFFSET]);
            // Sets the last address to its default
            headers[headerOff + LAST_OFFSET] = (i * objCntMinus + objCntMinusTwo) * TOTAL_E_OFFSET;
            //System.out.print("Setting last address of " + i + " to : ");
            //printAddr(headers[headerOff + LAST_OFFSET]);
        }
        ret = new int[objCntIn];
        // Sort lists
        for (int i = 0; i < objCntIn; i++) {
            boolean hasChanged = true;
            while (hasChanged) {
                //System.out.println("Sorting...");
                //printRanking(this);
                //System.out.println("Done with print");
                hasChanged = false;
                int value = 0;
                int addr = getFirstAddr(i);
                int nextAddr;
                while ((nextAddr = getNextAddr(addr)) != -1) {
                    int obj1 = getObject(getOtherAddr(addr));
                    int obj2 = getObject(getOtherAddr(nextAddr));
                    //System.out.println(obj1 + ", " + obj2);
                    //printRanking(this);
                    if (scores[StableRoommate.getMapping(i, obj1)] < scores[StableRoommate.getMapping(i, obj2)]) {
                        hasChanged = true;
                        flip(addr);
                    } else {
                        addr = nextAddr;
                    }
                }
            }
            //System.out.println("Done sorting");
        }
        //printRanking(this);
        //printScoreRanking(this, scores);
    }

    public static void printScoreRanking(FunMatrix m, double[] scores) {
        for (int i = 0; i < m.objCnt; i++) {
            int addr = m.getFirstAddr(i);
            while (addr != -1) {
                //m.printAddr(addr);
                //System.out.print("Current: ");
                //m.printAddr(addr);
                int object = m.getObject(addr);
                int otherObject = m.getObject(m.getOtherAddr(addr));
                System.out.print(scores[StableRoommate.getMapping(object, otherObject)] + "\t");
                addr = m.getNextAddr(addr);
            }
            System.out.println("#");
        }
    }

    public int[] exec() {
        if (isDone) return ret;
        isDone = true;
        stepOne();
        //System.out.println("Starting step 2");
        //printRanking(this);
        stepTwo();
        for (int i = 0; i < objCnt; i++) if (ret[i] != -1) ret[i]--;
        return ret;
    }

    /*
    struct {
        int firstPtr;
        int lastPtr;
    }
     */
    private static final int FIRST_OFFSET = 0;
    private static final int LAST_OFFSET = 1;
    private static final int TOTAL_H_OFFSET = 2;

    /*
    struct {
        int beforePtr;
        int afterPtr;
        int otherPtr;
        int associatedObject;
        int mode;
    }
     */
    private static final int BEFORE_OFFSET = 0;
    private static final int AFTER_OFFSET = 1;
    private static final int OTHER_OFFSET = 2;
    private static final int ASSOCIATE_OFFSET = 3;
    private static final int MODE_OFFSET = 4;
    private static final int TOTAL_E_OFFSET = 5;

    private void unsafeRemove(int addr) {
        //System.out.print("Unsafe Removing ");
        //printAddr(addr);
        if (addr == -1) return;
        int beforePtr = getPrevAddr(addr);
        int afterPtr = getNextAddr(addr);
        int obj = getObject(addr);
        int headerAddr = obj * TOTAL_H_OFFSET;
        // Make the one before us point ahead of us
        if (beforePtr == -1) {
            if (afterPtr == -1) {
                // There is nothing before or after us
                ret[obj] = -1;
                return;
            } else {
                // The header is before us
                headers[headerAddr + FIRST_OFFSET] = afterPtr;
            }
        } else {
            // Another element is before us
            data[beforePtr + AFTER_OFFSET] = afterPtr;
        }
        // Make the one ahead of us point before of us
        if (afterPtr == -1) {
            // The header is after us
            headers[headerAddr + LAST_OFFSET] = beforePtr;
        } else {
            // Another element is after us
            data[afterPtr + BEFORE_OFFSET] = beforePtr;
        }
    }

    public void rejectBelow(double scoreMin, double[] scores) {
        for (int o = 0; o < objCnt; o++) {
            int prevAddr = -1;
            int addr = getFirstAddr(o);
            while (addr != -1) {
                int o2 = getObject(getOtherAddr(addr));
                if (scores[StableRoommate.getMapping(o, o2)] < scoreMin) {
                    stripAfter(o, prevAddr);
                    break;
                }
                prevAddr = addr;
                addr = getNextAddr(addr);
            }
        }
    }

    private void takeout(int addr) {
        //System.out.print("Taking out ");
        //printAddr(addr);
        if (addr == -1) return;
        //printRanking(this);
        //System.out.println("#####");
        unsafeRemove(getOtherAddr(addr));
        //printRanking(this);
        //System.out.println("#####");
        unsafeRemove(addr);
        //printRanking(this);
    }

    private int getFirstAddr(int obj) {
        if (ret[obj] != 0) return -1;
        return headers[obj * TOTAL_H_OFFSET + FIRST_OFFSET];
    }

    private int getSecondAddr(int obj) {
        return getNextAddr(getFirstAddr(obj));
    }

    private int getLastAddr(int obj) {
        if (ret[obj] != 0) return -1;
        return headers[obj * TOTAL_H_OFFSET + FIRST_OFFSET];
    }

    private int getPrevAddr(int addr) {
        return data[addr + BEFORE_OFFSET];
    }

    private int getNextAddr(int addr) {
        return data[addr + AFTER_OFFSET];
    }

    private int getObject(int addr) {
        return data[addr + ASSOCIATE_OFFSET];
    }

    private int getOtherAddr(int addr) {
        return data[addr + OTHER_OFFSET];
    }

    private boolean isMatched(int obj) {
        return ret[obj] != 0;
    }

    private void stripBefore(int addr) {
        int obj = getObject(addr);
        if (isMatched(obj)) return;
        int headerAddr = obj * TOTAL_H_OFFSET;
        int headerFirstAddr = headerAddr + FIRST_OFFSET;
        int removeAddr;
        while (!isMatched(obj) && ((removeAddr = headers[headerFirstAddr]) != addr)) {
            takeout(removeAddr);
        }
    }

    private void stripAfter(int obj, int addr) {
        //System.out.print(obj + ": stripping all after ");
        //printAddr(addr);
        int lastObjectOffset = obj * TOTAL_H_OFFSET + LAST_OFFSET;
        int rejectAddr;
        while (!isMatched(obj) && ((rejectAddr = headers[lastObjectOffset]) != addr)) {
            if (data[rejectAddr + MODE_OFFSET] == MODE_ASKED) {
                // Someone's asking us out
                // We should reject
                takeout(rejectAddr);
                if (proposeQueue != null) {
                    // And make it propose again
                    int proposeObj = getObject(getOtherAddr(headers[lastObjectOffset]));
                    if (!proposeQueue.contains(proposeObj)) {
                        proposeQueue.add(proposeObj);
                    }
                }
            } else {
                takeout(rejectAddr);
            }
        }
    }

    private void stripAfter(int addr) {
        //System.out.print("Stripping after ");
        //printAddr(addr);
        stripAfter(getObject(addr), addr);
    }

    private void stripAll(int obj) {
        stripAfter(obj, -1);
    }

    private void match(int addr) {
        //System.out.println("MATCHING");
        int obj1 = getObject(addr);
        int otherAddr = getOtherAddr(addr);
        int obj2 = getObject(otherAddr);
        //System.out.println(obj1 + ":" + obj2);
        takeout(addr);
        stripAll(obj2);
        stripAll(obj1);
        ret[obj1] = obj2 + 1;
        ret[obj2] = obj1 + 1;
        //System.out.println("DONE MATCHING");
    }

    private boolean isOnlyOne(int obj) {
        return !isMatched(obj) && (getFirstAddr(obj) == getLastAddr(obj));
    }

    /**
     * Gets the address of the element in obj that points at otherObj
     *
     * @param obj The object who's element list we're searching
     * @param otherObj The object we're searching for a reference to
     * @return The address of the element we found or -1 on failure
     */
    private int findAddr(int obj, int otherObj) {
        int addr = getFirstAddr(obj);
        while (addr != -1) {
            if (getObject(getOtherAddr(addr)) == otherObj) {
                return addr;
            } else {
                addr = getNextAddr(addr);
            }
        }
        return -1;
    }

    private void propose(int obj) {
        //System.out.println("Proposing with " + obj);
        //for (int i = 0; i < proposeQueue.size(); i++) System.out.print(proposeQueue.get(i) + "|");
        //System.out.println();
        //printRanking(this);
        if (isMatched(obj)) {
            //System.out.println("We're already matched");
            // We've already been matched
            return;
        }
        int addr = getFirstAddr(obj);
        int otherAddr = getOtherAddr(addr);
        int otherModeOffsetAddr = otherAddr + MODE_OFFSET;
        switch (data[otherModeOffsetAddr]) {
            case MODE_NONE:
                // Ask the target
                if (getPrevAddr(otherAddr) != -1) {
                    //System.out.println("Asking");
                    // We're not their guaranteed best option, just ask them
                    data[addr + MODE_OFFSET] = MODE_ASKING;
                    data[otherModeOffsetAddr] = MODE_ASKED;
                    // Reject everyone after us
                    stripAfter(otherAddr);
                    break;
                }
                //System.out.println("We're their best option");
                // We're their guaranteed best option, just continue and pretend they asked us
            case MODE_ASKING:
                //System.out.println("It's mutual");
                // We can match up together because it's mutual
                match(otherAddr);
            //case MODE_ASKED:
                // We're already asking them, so we can do nothing and exit
        }
    }

    private void stepOne() {
        proposeQueue = new ArrayList<>();
        for (int i = 0; i < objCnt; i++) {
            proposeQueue.add(i);
        }
        while (proposeQueue.size() > 0) {
            propose(proposeQueue.remove(0));
        }
        proposeQueue = null;
    }

    private boolean rejectLoop(int startObj) {
        //System.out.println("Rejecting for " + startObj);
        if (ret[startObj] != 0) {
            // Its already been matched
            //System.out.println("We're matched already");
            return false;
        }
        int firstAddr = getFirstAddr(startObj);
        int lastAddr = getLastAddr(startObj);
        if (firstAddr == lastAddr) {
            // There's only one item, so we match with it and exit
            //System.out.println("Matching");
            match(firstAddr);
            return false;
        }
        int nextObj = getObject(lastAddr);
        while (nextObj != startObj) {
            int secondAddr = getSecondAddr(nextObj);
            nextObj = getObject(getLastAddr(getObject(getOtherAddr(secondAddr))));
            takeout(secondAddr);
        }
        return true;
    }

    private void stepTwo() {
        for (int i = 0; i < objCnt;) {
            if (!rejectLoop(i)) i++;
        }
    }

    // Test code

    public static void printRanking(FunMatrix m) {
        for (int i = 0; i < m.objCnt; i++) {
            int addr = m.getFirstAddr(i);
            while (addr != -1) {
                //m.printAddr(addr);
                //System.out.print("Current: ");
                //m.printAddr(addr);
                System.out.print(m.getObject(m.getOtherAddr(addr)) + ((m.data[addr + MODE_ASKED] == MODE_ASKED) ? "!" : "") + "\t");
                addr = m.getNextAddr(addr);
            }
            System.out.println("#");
        }
    }
}