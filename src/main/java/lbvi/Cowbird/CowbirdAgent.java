package lbvi.Cowbird;

import lbvi.LBVIEnvironment;
import lbvi.Nest;
import lbvi.Traps.TrapInfoIndentifier;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.ArrayList;
import java.util.List;

public class CowbirdAgent implements Steppable {
    public int cowbirdID;
    public int cowbirdCurrentTerrID; //the terrID of the cowbird's current Location
    public int cowbirdNDaysLeft;
    public double cowbirdCoordX;
    public double cowbirdCoordY;

    public Stoppable event; //allow the removal of an agent from the shcedule

    public CowbirdAgent(LBVIEnvironment state, int cowbirdID, int cowbirdCurrentTerrID, int cowbirdNDaysLeft) {
        this.cowbirdID = cowbirdID;
        this.cowbirdCurrentTerrID = cowbirdCurrentTerrID;
        this.cowbirdNDaysLeft = cowbirdNDaysLeft;
        this.cowbirdCoordX = state.vegTerrInfo.get(cowbirdCurrentTerrID).terrCoordX;
        this.cowbirdCoordY = state.vegTerrInfo.get(cowbirdCurrentTerrID).terrCoordY;
    }

    @Override
    public void step(SimState simState) {
        LBVIEnvironment state = (LBVIEnvironment) simState;
        // ODD3.19: at the beginning of each day the cowbird remains in the model, re-run the capture check.
        // isCaptured() only draws against traps within mpMaxCowbirdCaptureDistM, so this is a no-op
        // when no trap is in range. (The arrival-day check is handled once in Nest.step().)
        List<TrapInfoIndentifier> openTraps = logisticFunForCapture.findOpenTraps(state);
        if (logisticFunForCapture.isCaptured(state, this.cowbirdCurrentTerrID, openTraps)) {
            state.nCowbirdCaptured++;
            this.cowbirdCaptured(state);
            return;
        }
        if (this.cowbirdNDaysLeft > 0) {
            cowbirdNestSearch(state, state.activeNests);
        } else { //if cowbirdNDaysLeft == 0
            this.cowbirdDeath(state);
        }
    }

    /**
     * The cowbird searches for a nest to parasitize on the current day.
     *
     * (1) Random draw vs mpNextNestIsVireo:
     *     - draw > mpNextNestIsVireo → parasitize a different host species; decrement cowbirdNDaysLeft and return.
     *     - draw < mpNextNestIsVireo → search for an eligible Vireo nest.
     * (2) Build a square neighborhood of side mpCowbirdNestSearchDistance centred on the cowbird's
     *     current coordinates (half-width = mpCowbirdNestSearchDistance / 2).
     * (3) Collect all Vireo nests within the square that are eligible for parasitism (ODD 3.17).
     * (4) If no eligible nests are found, decrement cowbirdNDaysLeft and return.
     * (5) Randomly select one eligible nest, move the cowbird to that territory, and call
     *     cowbirdParasitism() on it.
     *
     * @param state     the simulation environment
     * @param allNests  all currently active Vireo nests (state.activeNests)
     */
    public void cowbirdNestSearch(LBVIEnvironment state, List<Nest> allNests) {
        // (1) decide whether to target a Vireo nest or another host species
        if (!state.random.nextBoolean(state.mpNextNestIsVireo)) {
            // draw > mpNextNestIsVireo: parasitize a different host species today
            this.cowbirdNDaysLeft--;
            return;
        }
        // (2) square neighbourhood: side = mpCowbirdNestSearchDistance, centred on cowbird
        double halfSide = state.mpCowbirdNestSearchDistance / 2.0;
        double xMin = this.cowbirdCoordX - halfSide;
        double xMax = this.cowbirdCoordX + halfSide;
        double yMin = this.cowbirdCoordY - halfSide;
        double yMax = this.cowbirdCoordY + halfSide;
        // (3) collect eligible Vireo nests within the square
        List<Nest> eligibleNests = new ArrayList<>();
        for (Nest nest : allNests) {
            if (nest.nestCoordX >= xMin && nest.nestCoordX <= xMax &&
                nest.nestCoordY >= yMin && nest.nestCoordY <= yMax &&
                nest.isEligibleForParasitism()) {
                eligibleNests.add(nest);
            }
        }
        // (4) no eligible nest found: decrement and wait for next day
        if (eligibleNests.isEmpty()) {
            this.cowbirdNDaysLeft--;
            return;
        }
        // (5) randomly select a nest, move to it, and parasitize
        Nest targetNest = eligibleNests.get(state.random.nextInt(eligibleNests.size()));
        this.cowbirdCurrentTerrID = targetNest.nestTerrID;
        this.cowbirdCoordX = targetNest.nestCoordX;
        this.cowbirdCoordY = targetNest.nestCoordY;
        targetNest.cowbirdParasitism(state);
        this.cowbirdNDaysLeft--;
    }

    public void cowbirdCaptured(LBVIEnvironment state) { //this cowbird is captured at a trap and removed from the simulation
        //recording logCowbirdArrivalOrDeparture.csv: "currentStep", "date", "terrID", "CowbirdID", "ArrivalOrDepature"
        String cowbirdMovement = String.format("%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay,
                this.cowbirdCurrentTerrID, this.cowbirdID, "Captured");
        state.logCowbirdArrivalOrDepartureWriter.addToFile(cowbirdMovement);
        //remove the agent from the schedule
        event.stop();
    }

    public void cowbirdDeath(LBVIEnvironment state) { //this cowbird is leaving the simulation for the season
        //recording logCowbirdArrivalOrDeparture.csv: "currentStep", "date", "terrID", "CowbirdID", "ArrivalOrDepature"
        String cowbirdMovement = String.format("%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay,
                this.cowbirdCurrentTerrID, this.cowbirdID, "Departure");
        state.logCowbirdArrivalOrDepartureWriter.addToFile(cowbirdMovement);
        //remove the agent from the space
        event.stop(); //remove the agent from the schedule

    }

}
