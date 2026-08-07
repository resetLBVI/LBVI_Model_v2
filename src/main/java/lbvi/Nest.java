package lbvi;

import lbvi.Cowbird.CowbirdAgent;
import lbvi.Cowbird.logisticFunForCapture;
import lbvi.Traps.TrapInfoIndentifier;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.List;

public class Nest implements Steppable {
    //Identification and personal information
    public int nestID = 0;
//    public int nestMaxNEggs; //i.e., clutch size
    public int nestPatchID;
    public int nestTerrID;
    public int nestFemaleID;
    public int nestMaxNVireoEggs; //clutch size in the nest
    public double nestCoordX;
    public double nestCoordY;
    public NESTStatus nestStatus;
    public String youngSpecies;
    //Nest state variables
    int nestCountdownToHatch;
    int nestCountdownToFledging;
    int nestCountdownToIndependence;
    int nestCountdownToRenesting;
    //cowbird related
    public boolean cowbirdArrived;
    public CowbirdAgent parasitizedCowbird;

    Stoppable event; //allow the removal of a nest from the shcedule

    public Nest(LBVIEnvironment state, int nestID, int nestPatchID, int nestTerrID, int nestFemaleID) {
        this.nestID = nestID;
        this.nestPatchID = nestPatchID;
        this.nestTerrID = nestTerrID;
        this.nestFemaleID = nestFemaleID;
        this.nestMaxNVireoEggs = state.random.nextInt(state.mpClutchSize);
        this.nestCoordX = state.vegTerrInfo.get(nestTerrID).terrCoordX;
        this.nestCoordY = state.vegTerrInfo.get(nestTerrID).terrCoordY;
        this.nestStatus = NESTStatus.CREATION;
        this.cowbirdArrived = false;
        this.parasitizedCowbird = null;
        this.youngSpecies = "LBVI";
        //nest countdown clock
        this.nestCountdownToHatch = state.mpIncubationStageDuration;
        this.nestCountdownToFledging = state.mpNestlingStageDuration;
        this.nestCountdownToIndependence = state.mpFledglingStageDuration;
        this.nestCountdownToRenesting = state.mpRenestingIntervalDuration;
    }

    @Override
    public void step(SimState simState) {
        LBVIEnvironment eState = (LBVIEnvironment) simState;
        // NESTStatus transitions are driven by the female LBVIAgent.
        // This step handles only side-effects: TLB/defoliation mortality and cowbird parasitism.
        if (nestStatus == NESTStatus.DEAD) {
            event.stop();
            return;
        }
        // TLB/defoliation mortality — method depends on current stage
        if (nestStatus == NESTStatus.FLEDGLING) {
            checkVireoMortalityDueToTLB_Fledgling(eState);
        } else if (nestStatus == NESTStatus.EGG || nestStatus == NESTStatus.NESTLING) {
            checkVireoMortalityDueToTLB_EggNestling(eState);
        }
        // Cowbird arrival
        if (!cowbirdArrived && cowbirdArrivalProb(eState.currentJulianDay, eState)) {
            this.cowbirdArrived = true;
            eState.nCowbirdArrival++;
            int cowbirdID = eState.cowbirdID++;
            int cowbirdNDaysLeft = eState.mpNCowbirdEggsPerSeason;
            CowbirdAgent newCowbirdAgent = new CowbirdAgent(eState, cowbirdID, nestTerrID, cowbirdNDaysLeft);
            newCowbirdAgent.event = eState.schedule.scheduleRepeating(newCowbirdAgent);
            //log Cowbird arrival
            // "currentStep", "date", "terrID", "CowbirdID", "ArrivalOrDepature"
            String cowbirdArrival = String.format("%s,%s,%s,%s,%s", eState.schedule.getSteps(), eState.currentJulianDay,
                    newCowbirdAgent.cowbirdCurrentTerrID, newCowbirdAgent.cowbirdID, "Arrival");
            eState.logCowbirdArrivalOrDepartureWriter.addToFile(cowbirdArrival);
            parasitizedCowbird = newCowbirdAgent;
            // capture check on arrival: if trapped nearby, remove it and log the departure (ODD 3.17)
            List<TrapInfoIndentifier> openTraps = logisticFunForCapture.findOpenTraps(eState);
            if (logisticFunForCapture.isCaptured(eState, this.nestTerrID, openTraps)) {
                eState.nCowbirdCaptured++;
                parasitizedCowbird.cowbirdCaptured(eState);
                parasitizedCowbird = null;
            }
        }
        // Parasitism check
        if (isEligibleForParasitism()) {
            if (eState.random.nextBoolean(eState.mpNextNestIsVireo)) {
                cowbirdParasitism(eState);
            }
        }
        if (parasitizedCowbird != null) {
            parasitizedCowbird.cowbirdNDaysLeft--;
        }
    }

    public void nestCreation (LBVIEnvironment state) {
        //log SuccessEvents.csv, log the creation of the nest
        // "currentStep", "Date", "EventType", "TerrID", "VireoID", "NestID", "NumIndividual", "EntityType", "YoungSp"
        String nestCreation = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay, "nest creation", this.nestTerrID,
                this.nestFemaleID, this.nestID, this.nestMaxNVireoEggs, "nest", this.youngSpecies);
        state.logSuccessWriter.addToFile(nestCreation);
    }

    /**
     * Determines whether a cowbird arrives on the given Julian day using a
     * triangular date-adjusted arrival probability (see cowbird_arrival_probability.drawio.pdf).
     *
     * The probability rises linearly from Min to Max between mpCowbirdFirstDate and
     * mpPeakCowbirdDate, then falls linearly back to Min between mpPeakCowbirdDate
     * and mpCowbirdLastDate. Outside that window the probability is Min (0).
     * A single uniform random draw (0–1) is compared to the scaled probability;
     * the cowbird arrives when draw < date-adjusted probability.
     *
     * @param julianDay current Julian day of the simulation step
     * @param state     reference to LBVIEnvironment for model parameters and RNG
     * @return true if the cowbird arrives this day, false otherwise
     */
    public boolean cowbirdArrivalProb(int julianDay, LBVIEnvironment state) {
        double minProb = state.mpMinCowbirdArrivalProb;
        double maxProb = state.mpMaxCowbirdArrivalProb;
        int firstDate  = state.mpCowbirdFirstDate;
        int peakDate   = state.mpPeakCowbirdDate;
        int lastDate   = state.mpCowbirdLastDate;

        double dateAdjustedProb;
        if (julianDay < firstDate || julianDay > lastDate) {
            dateAdjustedProb = minProb;
        } else if (julianDay <= peakDate) {
            // ascending limb: Min → Max
            dateAdjustedProb = minProb + (maxProb - minProb)
                    * (double)(julianDay - firstDate) / (peakDate - firstDate);
        } else {
            // descending limb: Max → Min
            dateAdjustedProb = minProb + (maxProb - minProb)
                    * (double)(lastDate - julianDay) / (lastDate - peakDate);
        }
        return state.random.nextDouble() < dateAdjustedProb;
    }

    /**
     * Returns true if this nest is currently eligible for cowbird parasitism (ODD 3.17).
     * The full eligibility criteria includes (1) Cowbird arrives (2) No cowbird traps are open with in
     * mpMaxCowbirdCaptureDistM or one or more cowbird traps are present, but the cowbird eludes capture. To check the
     * second criteria by using the isCaptureed() function in logisticFunForCapture.java
     */
    public boolean isEligibleForParasitism() {
        // Pure predicate (no side effects): eligible when a cowbird arrived at this nest and was
        // not captured. Capture is resolved once at arrival in step(), which nulls parasitizedCowbird.
        return this.cowbirdArrived && this.parasitizedCowbird != null;
    }

    public void cowbirdParasitism(LBVIEnvironment state) {
        state.nParasitism++;
        //log MortalityEvents.csv
        // "step", "Date", "EventType", "TerrID", "VireoID", "NestID", "CowbirdID", "NumIndividual", "EntityType", "YoungSp"
        String parasitismLog = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay, "parasitism", this.nestTerrID,
            this.nestFemaleID, this.nestID, this.parasitizedCowbird.cowbirdID, this.nestMaxNVireoEggs, this.nestStatus, "Cowbird");
        state.logMortalityWriter.addToFile(parasitismLog);
    }

    /*
    ****************************************************************************************
    *                           Nest Mortality
    * ***************************************************************************************
     */
    public void checkVireoMortalityDueToTLB_EggNestling (LBVIEnvironment state) {
        if (state.vegTerrInfo.get(this.nestTerrID).terrDefoliated == true) {
            this.nestDeath(state, "defoliation", this.youngSpecies);
        } else if (state.vegTerrInfo.get(this.nestTerrID).terrTamariskMortalityByTLB) {
            this.nestDeath(state, "TLB Mortality", this.youngSpecies);
        }
    }

    public void checkVireoMortalityDueToTLB_Fledgling (LBVIEnvironment state) {
        int chickAged = state.mpFledglingStageDuration - this.nestCountdownToIndependence;
        if (state.vegTerrInfo.get(this.nestTerrID).terrDefoliated == true && chickAged < state.mpNDaysChickThermoregulate) {
            this.nestDeath(state, "defoliation", this.youngSpecies);
        } else if (state.vegTerrInfo.get(this.nestTerrID).terrTamariskMortalityByTLB && chickAged < state.mpNDaysChickThermoregulate) {
            this.nestDeath(state, "TLB Mortality", this.youngSpecies);
        }
    }

    /**
     * Returns the number of young currently in the nest, read from the owning
     * female agent (looked up by nestFemaleID, which is the female's vireoID).
     * The relevant count depends on the nest's current developmental stage:
     * eggs when EGG, nestlings when NESTLING, fledglings when FLEDGLING.
     * Returns 0 if the female can no longer be found or the nest holds no young
     * (e.g. CREATION/DEAD).
     */
    public int getNumIndividualsInNest(LBVIEnvironment state) {
        LBVIAgent female = state.lbviAgentMap.get(this.nestFemaleID);
        if (female == null) {
            return 0;
        }
        switch (this.nestStatus) {
            case EGG:
                return female.vireoNumEggs;
            case NESTLING:
                return female.vireoNumNestlings;
            case FLEDGLING:
                return female.vireoNumFledglings;
            default:
                return 0;
        }
    }

    public void nestDeath (LBVIEnvironment state, String eventType, String youngSpecies) {
        //1. the newborn will die (eggs, nestlings, or fledglings) -
        // I don't know which stage the agent will be created, but if the agent is already created, it should die (TODO! 2026-06-01)
        //2. record the mortality of nest - "step", "Date", "EventType", "TerrID", "VireoID", "NestID", "CowbirdID", "NumIndividual", "EntityType", "YoungSp"
        int NumIndividualDead = getNumIndividualsInNest(state);
        String nestDeath = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay, eventType, this.nestTerrID,
                this.nestFemaleID, this.nestID, "NA", NumIndividualDead, this.nestStatus, youngSpecies);
        state.logMortalityWriter.addToFile(nestDeath);
        this.nestStatus = NESTStatus.DEAD;
        state.activeNests.remove(this);
        event.stop();
        state.nestMap.remove(this.nestID);

    }

}
