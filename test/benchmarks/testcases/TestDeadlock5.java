package benchmarks.testcases;

//We were getting a java.lang.VerifyError while instrumenting this example because of the 
//super() call in Lift(). The problem was that we were trying to access the object which was
//being initialized in the Lift() constructor before the super() call.


import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class TestDeadlock5 {
    //shared control object
    private Controls controls;
    private Vector events;
    private Lift[] lifts;
    private int numberOfLifts;

    // Initializer for main class, reads the input and initlizes
    // the events Vector with ButtonPress objects
    private TestDeadlock5(String file) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }
        StreamTokenizer st = new StreamTokenizer(reader);
        st.lowerCaseMode(true);
        st.parseNumbers();

        events = new Vector();

        int numFloors = 0, numLifts = 0;
        try {
            numFloors = readNum(st);
            numLifts = readNum(st);

            int time = 0, to = 0, from = 0;
            do {
                time = readNum(st);
                if (time != 0) {
                    from = readNum(st);
                    to = readNum(st);
                    events.addElement(new ButtonPress(time, from, to));
                }
            } while (time != 0);
        }
        catch (IOException e) {
            System.err.println("error reading input: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // Create the shared control object
        controls = new Controls(numFloors);
        numberOfLifts = numLifts;
        lifts = new Lift[numLifts];
        // Create the elevators
        for (int i = 0; i < numLifts; i++)
            lifts[i] = new Lift(numFloors, controls);
    }

    private int readNum(StreamTokenizer st) throws IOException {
        int tokenType = st.nextToken();

        if (tokenType != StreamTokenizer.TT_NUMBER)
            throw new IOException("Number expected!");
        return (int) st.nval;
    }

    // Press the buttons at the correct time
    private void begin() {
        // Get the thread that this method is executing in
        Thread me = Thread.currentThread();
        // First tick is 1
        int time = 1;

        for (int i = 0; i < events.size();) {
            ButtonPress bp = (ButtonPress) events.elementAt(i);
            // if the current tick matches the time of th next event
            // push the correct buttton
            if (time == bp.time) {
                System.out.println("Elevator::begin - its time to press a button");
                if (bp.onFloor > bp.toFloor)
                    controls.pushDown(bp.onFloor, bp.toFloor);
                else
                    controls.pushUp(bp.onFloor, bp.toFloor);
                i += 1;
            }
            // wait 1/2 second to next tick
            try {
                me.sleep(500);
            } catch (InterruptedException e) {
            }
            time += 1;
        }
    }


    private void waitForLiftsToFinishOperation() {
        for (int i = 0; i < numberOfLifts; i++) {
            try {
                lifts[i].join();
            }
            catch (InterruptedException e) {
                System.err.println("Error while waitinf for lift" + i + "to finish");
            }
        }
    }

    public static void main(String args[]) {
        TestDeadlock5 building = new TestDeadlock5(args[0]);
        long start = new Date().getTime();
        building.begin();
        building.waitForLiftsToFinishOperation();
        long end = new Date().getTime();

        System.out.println("Time taken in ms : " + (end - start));
    }
}

class Controls {
    private Floor[] floors;

    public Controls(int numFloors) {
        floors = new Floor[numFloors + 1];
        for (int i = 0; i <= numFloors; i++) floors[i] = new Floor();
    }

    //this is called to inform the control object of a down call on floor
    // onFloor
    public void pushDown(int onFloor, int toFloor) {
        synchronized (floors[onFloor]) {
            System.out.println("*** Someone on floor " + onFloor +
                    " wants to go to " + toFloor);
            floors[onFloor].downPeople.addElement(new Integer(toFloor));
            if (floors[onFloor].downPeople.size() == 1)
                floors[onFloor].downFlag = false;
        }
    }

    // this is called to inform the control object of an up call on floor
    // onFloor
    public void pushUp(int onFloor, int toFloor) {
        synchronized (floors[onFloor]) {
            System.out.println("*** Someone on floor " + onFloor +
                    " wants to go to " + toFloor);
            floors[onFloor].upPeople.addElement(new Integer(toFloor));
            if (floors[onFloor].upPeople.size() == 1)
                floors[onFloor].upFlag = false;
        }
    }

    //  An elevator calls this if it wants to claim an up call
    // Sets the floor's upFlag to true if he has not already been set to true
    // Returns true if the elevator has successfully claimed the call, and
    // False if the call was already claimed (upFlag was already true)
    public boolean claimUp(String lift, int floor) {
        if (checkUp(floor)) {
            synchronized (floors[floor]) {
                if (!floors[floor].upFlag) {
                    floors[floor].upFlag = true;
                    return true;
                }
            }
        }
        return false;
    }

    // An elevator calls this if it wants to claim an down call
    // Sets the floor's downFlag to true if he has not already been set to true
    // Returns true if the elevator has successfully claimed the call, and
    // False if the call was already claimed (downFlag was already true)
    public boolean claimDown(String lift, int floor) {
        if (checkDown(floor)) {
            synchronized (floors[floor]) {
                if (!floors[floor].downFlag) {
                    floors[floor].downFlag = true;
                    return true;
                }
            }
        }
        return false;
    }

    // An elevator calls this to see if an up call has occured on the given
    // floor.  If another elevator has already claimed the up call on the 
    // floor, checkUp() will return false.  This prevents an elevator from
    // wasting its time trying to claim a call that has already been claimed
    public boolean checkUp(int floor) {
        synchronized (floors[floor]) {
            boolean ret = floors[floor].upPeople.size() != 0;
            ret = ret && !floors[floor].upFlag;
            return ret;
        }
    }

    // An elevator calls this to see if a down call has occured on the given
    // floor.  If another elevator has already claimed the down call on the 
    // floor, checkUp() will return false.  This prevents an elevator from
    // wasting its time trying to claim a call that has already been claimed
    public boolean checkDown(int floor) {
        synchronized (floors[floor]) {
            boolean ret = floors[floor].downPeople.size() != 0;
            ret = ret && !floors[floor].downFlag;
            return ret;
        }
    }

    // An elevator calls this to get the people waiting to go up.  The
    // returned Vector contains Integer objects that represent the floors
    // to which the people wish to travel.  The floors vector and upFlag
    // are reset.
    public Vector getUpPeople(int floor) {
        synchronized (floors[floor]) {
            Vector temp = floors[floor].upPeople;
            floors[floor].upPeople = new Vector();
            floors[floor].upFlag = false;
            return temp;
        }
    }

    // An elevator calls this to get the people waiting to go down.  The
    // returned Vector contains Integer objects that represent the floors
    // to which the people wish to travel.  The floors vector and downFlag
    // are reset.
    public Vector getDownPeople(int floor) {
        synchronized (floors[floor]) {
            Vector temp = floors[floor].downPeople;
            floors[floor].downPeople = new Vector();
            floors[floor].downFlag = false;
            return temp;
        }
    }
}

class Lift extends Thread {
    private static int count = 0;

    public static final int IDLE = 0;
    public static final int UP = 1;
    public static final int DOWN = 2;

    private int travelDir;
    private int currentFloor;
    private int[] peopleFor;
    private int[] pickupOn;
    private int firstFloor, lastFloor;
    private Controls controls;

    public Lift(int numFloors, Controls c) {
        super("Lift " + count++);
        controls = c;
        firstFloor = 1;
        lastFloor = numFloors;
        travelDir = IDLE;
        currentFloor = firstFloor;
        pickupOn = new int[numFloors + 1];
        peopleFor = new int[numFloors + 1];
        for (int i = 0; i <= numFloors; i++) {
            pickupOn[i] = IDLE;
            peopleFor[i] = 0;
        }
        start();
    }

    public void run() {
        int numIterations = 100;
        int i = 0;
        while (i < numIterations) {
            if (travelDir == IDLE) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                }
                doIdle();
            } else {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
                doMoving();
            }
            i++;
        }
    }

    // IDLE
    // First check to see if there is an up or down call on what ever floor
    // the elevator is idle on.  If there isn't one, then check the other floors.
    private void doIdle() {
        boolean foundFloor = false;
        int targetFloor = -1;

        if (controls.claimUp(getName(), currentFloor)) {
            // System.out.println("Lift::doIdle - could claim upcall on current floor"); // CARE
            foundFloor = true;
            targetFloor = currentFloor;
            travelDir = UP;
            addPeople(controls.getUpPeople(currentFloor));
        } else if (controls.claimDown(getName(), currentFloor)) {
            // System.out.println("Lift::doIdle - could claim downcall on current floor"); // CARE
            foundFloor = true;
            targetFloor = currentFloor;
            travelDir = DOWN;
            addPeople(controls.getDownPeople(currentFloor));
        }

        // System.out.println("Lift::doIdle - lookuing for calls on other floors"); // CARE
        for (int floor = firstFloor; !foundFloor && floor <= lastFloor; floor++) {
            // System.out.println("Lift::doIdle - checking floor " + floor); // CARE
            if (controls.claimUp(getName(), floor)) {
                // System.out.println("Lift::doIdle - success with claimUp " + floor); // CARE
                foundFloor = true;
                targetFloor = floor;
                pickupOn[floor] |= UP;
                travelDir = (targetFloor > currentFloor) ? UP : DOWN;
            } else if (controls.claimDown(getName(), floor)) {
                // System.out.println("Lift::doIdle - success with claimDown " + floor); // CARE
                foundFloor = true;
                targetFloor = floor;
                pickupOn[floor] |= DOWN;
                travelDir = (targetFloor > currentFloor) ? UP : DOWN;
            }
        }

        if (foundFloor) {
            System.out.println(getName() + " is now moving " +
                    ((travelDir == UP) ? "UP" : "DOWN"));
        }
    }

    // MOVING
    // First change floor (up or down as appropriate)
    // Drop off passengers if we have to
    // Then pick up passengers if we have to
    private void doMoving() {
        currentFloor += (travelDir == UP) ? 1 : -1;
        int oldDir = travelDir;

        if (travelDir == UP && currentFloor == lastFloor) travelDir = DOWN;
        if (travelDir == DOWN && currentFloor == firstFloor) travelDir = UP;
        System.out.println(getName() + " now on " + currentFloor);

        if (peopleFor[currentFloor] > 0) {
            System.out.println(getName() + " delivering " +
                    peopleFor[currentFloor] + " passengers on " +
                    currentFloor);
            peopleFor[currentFloor] = 0;
        }

        // Pickup people who want to go up if:
        //   1) we previous claimed an up call on this floor, or
        //   2) we are travelling up and there is an unclaimed up call on this
        //      floor
        if (((pickupOn[currentFloor] & UP) != 0) ||
                (travelDir == UP && controls.claimUp(getName(), currentFloor))) {
            addPeople(controls.getUpPeople(currentFloor));
            pickupOn[currentFloor] &= ~UP;
        }

        // Pickup people who want to go down if:
        //   1) we previous claimed an down call on this floor, or
        //   2) we are travelling down and there is an unclaimed down call on this
        //      floor
        if (((pickupOn[currentFloor] & DOWN) != 0) ||
                (travelDir == DOWN && controls.claimDown(getName(), currentFloor))) {
            addPeople(controls.getDownPeople(currentFloor));
            pickupOn[currentFloor] &= ~DOWN;
        }

        if (travelDir == UP) {
            // If we are travelling up, and there are people who want to get off
            // on a floor above this one, continue to go up.
            if (stopsAbove()) ;
            else {
                // If we are travelling up, but no one wants to get off above this
                // floor, but they do want to get off below this one, start
                // moving down
                if (stopsBelow()) travelDir = DOWN;
                    // Otherwise, no one is the elevator, so become idle
                else travelDir = IDLE;
            }
        } else {
            // If we are travelling down, and there are people who want to get off
            // on a floor below this one, continue to go down.
            if (stopsBelow()) ;
            else {
                // If we are travelling down, but no one wants to get off below this
                // floor, but they do want to get off above this one, start
                // moving up
                if (stopsAbove()) travelDir = UP;
                    // Otherwise, no one is the elevator, so become idle
                else travelDir = IDLE;
            }
        }

        // Print out are new direction
        if (oldDir != travelDir) {
            System.out.print(getName());
            if (travelDir == IDLE) System.out.println(" becoming IDLE");
            else if (travelDir == UP) System.out.println(" changing to UP");
            else if (travelDir == DOWN) System.out.println(" changing to DOWN");
        }
    }

    //	  Returns true if there are passengers in the elevator who want to stop
    // on a floor above currentFloor, or we claimed a call on a floor below
    // currentFloor
    private boolean stopsAbove() {
        boolean above = false;
        for (int i = currentFloor + 1; !above && i <= lastFloor; i++)
            above = (pickupOn[i] != IDLE) || (peopleFor[i] != 0);
        return above;
    }

    // Returns true if there are passengers in the elevator who want to stop
    // on a floor below currentFloor, or we claiemda call on a floor above
    // currentFloor
    private boolean stopsBelow() {
        boolean below = false;
        for (int i = currentFloor - 1; !below && (i >= firstFloor); i--)
            below = (pickupOn[i] != IDLE) || (peopleFor[i] != 0);
        return below;
    }

    // Updates peopleFor based on the Vector of destination floors received
    // from the control object
    private void addPeople(Vector people) {
        System.out.println(getName() + " picking up " + people.size() +
                " passengers on " + currentFloor);
        for (Enumeration e = people.elements(); e.hasMoreElements();) {
            int toFloor = ((Integer) e.nextElement()).intValue();
            peopleFor[toFloor] += 1;
        }
    }
}

class Floor {
    // Lists of people waiting to go up, and down
    // The Vectors will have instances of Integer objects.  The Integer will
    // store the floor that the person wants to go to
    public Vector upPeople, downPeople;

    // True if an elevator has claimed the the up or down call
    public boolean upFlag, downFlag;

    public Floor() {
        upPeople = new Vector();
        downPeople = new Vector();
        upFlag = false;
        downFlag = false;
    }
}

class ButtonPress {
    // floor on which the button is pressed
    public int onFloor;

    // floor to which the person wishes to travel
    public int toFloor;

    // tick at which the button is pressed 
    public int time;

    public ButtonPress(int t, int from, int to) {
        onFloor = from;
        toFloor = to;
        time = t;
    }
}




