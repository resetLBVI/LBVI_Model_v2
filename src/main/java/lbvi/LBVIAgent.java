package lbvi;

import lbvi.Utils.ArrivalDate;
import lbvi.Utils.DispersalKernal;
import org.apache.commons.math3.distribution.PoissonDistribution;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.*;


public class LBVIAgent implements Steppable {
    //Identification and personal information
    int vireoID; //this is a unique ID for each Vireo agent, the ID is given when it survives through the first winter
    boolean vireoSexFemale; //if true, female. Otherwise, male
    boolean vireoAgeClassAdult; //if true, it's an adult; Otherwise, a first-year young
    int vireoAgeYears; //current age in years
    int vireoDeathAge; //adults die when they reach their death age; define in 3.19
    //Locations
    int vireoStartingLocation; //the starting location is a territoryID
    int vireoPreviousLocation; //the previous location is a territoryID
    int vireoCurrentLocation; //the current location is a territoryID
    double vireoCurrentX; //provide the Vireo current x coordinates, in meters, based on the patchID of currentLocation
    double vireoCurrentY; //provide the Vireo current y coordinates, in meters, based on the patchID of currentLocation
    //Arrival and start the breeding season
    int vireoArrivalDate; //determine the arrival date at the breeding site
    //reproductive state variables
    Stage vireoReproStage; //ten stages for both sexes
    boolean vireoMateStatus; //if mated, true; otherwise, false
    int vireoMateID; //identify the ID of an agent's mate
    int vireoNumNestAttempts; //female's state variable, indicate the number of nests in the current breeding season
    int vireoNestID; //female's state variable, indicate current Nest ID
    int vireoNumEggs; //female's state variable, indicate the number of eggs in current attempt
    int vireoNumNestlings; //female's state variable, indicate the number of nestlings in current attempt
    int vireoNumFledglings; //female's state variable, indicate the number of fledglings in current attempt
    int vireoNumRecruits; //female's state variable, indicate the number of babies in current breeding season
    int vireoNumNests; //number of nests the female agents have in a year
    boolean vireoYoungSpecies;//female's state variable, indicate the species of the youngs. True = "vireo, False = "cowbird"
    boolean vireoNestlingSpecies; //female's state variable, indicate the species of the nestlings. True = "vireo, False = "cowbird"
    ArrayList<Integer> vireoRecruitList; //collect and record the current offspring IDs
    //Dispersal & Nest Patch Selection
    double vireoDispDir;
    double vireoDispDist;
    ArrayList<Integer> potentialTerritoryList; //determine a potential patch list for dispersal
    int potentialMateID; //record the potential mate
    //Nest state variables
    int nestID; //a unique ID for each nest
    int nestMaxNVireoEggs; //the maximum number of vireo eggs that can be laid in the nest
    int vireoNestTerrID; // = female vrieoCurrentLocation
    int nestCountdownToHatch; //
    int nestCountdownToFledging;
    int nestCountdownToIndependence;
    int nestCountdownToRenesting;
    int vireoCurrentNumAttempts;
    NESTStatus vireoNestStatus; //a variable shows the current status of the nest - incubation, nestling, fledging, predated, finished, etc.
    Nest currentNest; // reference to the active Nest object; null when no nest is active
    //other scheduling variables
    long currentStep = -1; //get current step
    int currentJulianDate = -1; //get current Julian Date
    int currentYear = -1; //get current year
    Stoppable event; //allow the removal of an agent from the shcedule



    /*
    The constructor in JAVA is a special method that share the same name as its class and is used to initialize objects of that class. In this constructor
    define the identification and personal information and all the state variables when starting a LBVI agent
     */
    public LBVIAgent(LBVIEnvironment state, int vireoID, boolean vireoSexFemale, boolean vireoAgeClassAdult,
                     int vireoDeathAge, int vireoStartingLocation, boolean startup) {
        this.vireoID = vireoID; //a unique ID for each Vireo agent, the ID is given when it survives through the first winter
        this.vireoSexFemale = vireoSexFemale; //if true, female. Otherwise, male
        this.vireoAgeClassAdult = vireoAgeClassAdult; //if true, it's an adult; Otherwise, a first-year young
        if(startup) { //if at startup, set the initial age in the uniform random distribution between 0 and mpVireoMaxAge
            this.vireoAgeYears = state.random.nextInt(state.mpVireoMaximumAge); //current age in years
        } else {
            this.vireoAgeYears = 0; //if not startup, initial age is 0s, current age in years
        }
        this.vireoDeathAge = vireoDeathAge; //adults die when they reach their death age; define in 3.19
        this.vireoStartingLocation = vireoStartingLocation; //the starting location is a terrID
        this.vireoPreviousLocation = vireoStartingLocation; //the previous location is a terrID
        this.vireoCurrentLocation = vireoStartingLocation; //the current location is a terrID
        this.vireoArrivalDate = -1; //the arrival date was not defined before the breeding season starts
        this.vireoReproStage = Stage.ARRIVAL; //the first stage is "ARRIVAL" stage
        this.potentialTerritoryList = new ArrayList<>(); //create a new list
        this.potentialMateID = -1; // default no potential mate
        this.vireoMateStatus = false; //not mate yet
        this.vireoCurrentNumAttempts = 0; //no attempt in the beginning
        if(this.vireoSexFemale) { //only females track below state variables
            this.vireoNumNestAttempts = 0; //current number of nest attempts
            this.vireoNestID = 0; //indicate current Nest ID
            this.vireoNumEggs = 0; //current number of the eggs in the nest
            this.vireoNumNestlings = 0; //current number of the nestlings in the nest
            this.vireoNumFledglings = 0; //current number of the fledglings in the nest
            this.vireoNumRecruits = 0; //the number of babies in current breeding season
            this.vireoYoungSpecies = true; //the species of the young. True = "vireo, False = "cowbird"
            this.vireoNestlingSpecies = true; //the species of the nestling. True = "vireo, False = "cowbird"
            this.vireoRecruitList = new ArrayList<>(); //add the babies' IDs in the list
            //nest related
            this.currentNest = null;
            this.vireoNestStatus = null; //a variable shows the current status of the nest - incubation, nestling, fledging, predated, finished, etc.
            this.nestMaxNVireoEggs = 0;
            this.vireoNestTerrID = 0; //when nest is built, the nestPatchName = female vireoCurrentLocation
            this.nestCountdownToHatch = state.mpIncubationStageDuration;
            this.nestCountdownToFledging = state.mpNestlingStageDuration;
            this.nestCountdownToIndependence = state.mpFledglingStageDuration;
            this.nestCountdownToRenesting = state.mpRenestingIntervalDuration;
        }
        potentialTerritoryList = new ArrayList<>();
        vireoMateID = 0; //after parining, note spouse's ID for it's mate
    }

    @Override
    public void step(SimState state) {
        LBVIEnvironment eState = (LBVIEnvironment) state;
        currentStep = eState.schedule.getSteps();
        currentJulianDate = (int) (eState.schedule.getSteps() % 364);
        currentYear = (int) (currentStep / 364) + 1;
        Stage currentStage = this.vireoReproStage;
        //DEBUG: log each agent's stage at the start of every step (logDebug.txt)
        logDebugState(eState);
        switch (currentStage) {
            case ARRIVAL: //Stage 1
                checkArrival(eState);
                break;
            case DISPERSAL: //Stage 2
                //check if there is enough time for nesting
                if (currentJulianDate + eState.mpIncubationStageDuration + eState.mpNestlingStageDuration + eState.mpFledglingStageDuration + eState.mpRenestingIntervalDuration > eState.mpLastPossibleNestingDate) {
                    //if there is NO time for dispersal, actEndBreeding
                    this.vireoReproStage = Stage.NONBREEDING; //prepare for NON-Breeding
                    break; //skip everthing below, exit the switch
                }
                //make sure the potentialTerritoryList is not empty and move forward to the next stage
                if(this.potentialTerritoryList.isEmpty()) {
                    //(1) determine the dispersal direction and distance based on the dispersal kernel
                    dispersalKernel(eState); //return a distance and looking for a list of potential dispersal patches.
                    //(2) move to the new location and develop a potential patch list
                    for(int j=0; j< 3; j++) {
                        if(this.potentialTerritoryList.isEmpty()) {
                            this.potentialTerritoryList = findPotentialTerritoryList(eState, 1 + j * eState.bufferCoefficient);
                        } else { //if there are some potential territories already
                            this.vireoReproStage = Stage.NESTTERRSELECT;
                            //record the list
                        }
                    }
                } else {
                    this.vireoReproStage = Stage.NESTTERRSELECT;
                    //record the list
                }
                break;
            case NESTTERRSELECT: //Stage 3
                nestTerritorySelection(eState); //In this stage, agents choose a mate based on three criteria
                break;
            case PAIR: //Stage 4
                //check current location and find the Mr. or Mrs. Right!!
                //(1) locate current territory and find her mate
                if(this.vireoSexFemale) { //find Mr. Right
                    this.vireoMateID = eState.vegTerrInfo.get(this.vireoCurrentLocation).terrMaleID;
                    this.vireoMateStatus = true;
                    this.vireoReproStage = Stage.NESTCREATION;
                    //update mate's status at the same time
                    LBVIAgent spouse = eState.lbviAgentMap.get(this.vireoMateID);
                    spouse.vireoMateID = this.vireoID;
                    spouse.vireoMateStatus = true;
                    spouse.vireoReproStage = Stage.NESTCREATION;
                    eState.nPairs++;
                }
                break;
            case NESTCREATION: //Stage 5
                if (this.vireoSexFemale) { //only female create a nest
                    vireoNestTerrID = this.vireoCurrentLocation;
                    int nestPatchID = eState.vegTerrInfo.get(this.vireoCurrentLocation).patchID;
                    nestID++;
                    this.nestMaxNVireoEggs = getNestMaxNEggs(eState);
                    Nest newNest = new Nest(eState, nestID, nestPatchID, vireoNestTerrID, this.vireoID);
                    newNest.nestCreation(eState); // log creation event immediately
                    newNest.event = state.schedule.scheduleRepeating(newNest);
                    this.currentNest = newNest;
                    eState.activeNests.add(newNest);
                    eState.nestMap.put(nestID, newNest);
                    eState.nNests++;
                    this.vireoNestStatus = NESTStatus.EGG;
                    this.currentNest.nestStatus = NESTStatus.EGG;
                    this.vireoReproStage = Stage.EGGLAYING;
                }
                break;
            case EGGLAYING: //Stage 6
                if(this.vireoSexFemale) {
                    eggLaying(eState, this); //actEggLaying
                }
                break;
            case INCUBATION: //Stage 7
                if (this.vireoSexFemale) {
                    // TLB/defoliation may have killed the nest between steps
                    if (currentNest != null && currentNest.nestStatus == NESTStatus.DEAD) {
                        this.vireoNestStatus = NESTStatus.DEAD;
                        killCurrentNest(eState);
                        this.vireoReproStage = Stage.RENEST;
                        this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                        return;
                    }
                    if (this.vireoNumEggs > 0 && this.nestCountdownToHatch > 0) {
                        if (state.random.nextBoolean(eState.mpDailyNestMortality)) {
                            this.vireoNestStatus = NESTStatus.DEAD;
                            logDailyMortalityForNest(eState, "daily nest mortality", this.vireoNumEggs, NESTStatus.EGG);
                            killCurrentNest(eState);
                            this.vireoReproStage = Stage.RENEST;
                            this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                            return;
                        }
                        this.nestCountdownToHatch--;
                    } else if (this.vireoNumEggs > 0 && this.nestCountdownToHatch == 0) {
                        this.vireoReproStage = Stage.NESTLING;
                        if (currentNest != null) currentNest.nestStatus = NESTStatus.NESTLING;
                        this.nestCountdownToFledging = eState.mpNestlingStageDuration;
                        vireoNumNestlings = vireoNumEggs;
                        vireoNumEggs = 0;
                        eState.nNestlings += vireoNumNestlings;
                        //log to the "successEvents.csv" output file
                        String incubationEvent = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), eState.currentJulianDay, "hatch", this.vireoNestTerrID,
                                this.vireoID, this.nestID, this.vireoNumNestlings, "nest", currentNest.youngSpecies);
                        eState.logSuccessWriter.addToFile(incubationEvent);
                    }
                }
                break;
            case NESTLING: //Stage 8
                if (this.vireoSexFemale) {
                    if (currentNest != null && currentNest.nestStatus == NESTStatus.DEAD) {
                        this.vireoNestStatus = NESTStatus.DEAD;
                        killCurrentNest(eState);
                        this.vireoReproStage = Stage.RENEST;
                        this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                        return;
                    }
                    if (this.vireoNumNestlings > 0 && this.nestCountdownToFledging > 0) {
                        if (state.random.nextBoolean(eState.mpDailyNestMortality)) {
                            this.vireoNestStatus = NESTStatus.DEAD;
                            logDailyMortalityForNest(eState, "daily nest mortality", this.vireoNumNestlings, NESTStatus.NESTLING);
                            killCurrentNest(eState);
                            this.vireoReproStage = Stage.RENEST;
                            this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                            return;
                        }
                        this.nestCountdownToFledging--;
                    } else if (this.vireoNumNestlings > 0 && this.nestCountdownToFledging == 0) {
                        this.vireoReproStage = Stage.FLEDGLING;
                        if (currentNest != null) currentNest.nestStatus = NESTStatus.FLEDGLING;
                        this.nestCountdownToIndependence = eState.mpFledglingStageDuration;
                        vireoNumFledglings = vireoNumNestlings;
                        vireoNumNestlings = 0;
                        eState.nFledglings += vireoNumFledglings;
                        //log to the "successEvents.csv" output file
                        String nestlingEvent = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), eState.currentJulianDay, "fledge", this.vireoNestTerrID,
                                this.vireoID, this.nestID, this.vireoNumFledglings, "nest", currentNest.youngSpecies);
                        eState.logSuccessWriter.addToFile(nestlingEvent);

                    }
                }
                break;
            case FLEDGLING: //Stage 9
                if (this.vireoSexFemale) {
                    if (currentNest != null && currentNest.nestStatus == NESTStatus.DEAD) {
                        this.vireoNestStatus = NESTStatus.DEAD;
                        killCurrentNest(eState);
                        this.vireoReproStage = Stage.RENEST;
                        this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                        return;
                    }
                    if (this.vireoNumFledglings > 0 && this.nestCountdownToIndependence > 0) {
                        if (state.random.nextBoolean(eState.mpDailyFledglingMortality)) {
                            this.vireoNestStatus = NESTStatus.DEAD;
                            logDailyMortalityForNest(eState, "daily fledgling mortality", this.vireoNumFledglings, NESTStatus.FLEDGLING);
                            killCurrentNest(eState);
                            this.vireoReproStage = Stage.RENEST;
                            this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                            return;
                        }
                        this.nestCountdownToIndependence--;
                    } else if (this.vireoNumFledglings > 0 && this.nestCountdownToIndependence == 0) {
                        this.vireoCurrentNumAttempts++;
                        //male's vireoCurrentNumAttempts also need to be updated
                        //mate.vireoCurrentNumAttempts ++;
                        eState.nFledIndipendence += this.vireoNumFledglings;
                        eState.nSuccNests++;
                        // create recruits for each fledgling that reached independence
                        for (int f = 0; f < this.vireoNumFledglings; f++) {
                            LBVIAgent recruit = reproduceAgent(eState, this);
                            eState.lbviAgentMap.put(recruit.vireoID, recruit);
                            sim.util.Int2D loc = eState.territoryLocations.get(this.vireoCurrentLocation);
                            if (loc != null) eState.vegetationGrid.setObjectLocation(recruit, loc.x, loc.y);
                        }
                        //record the data to logSuccessEvent.csv (before zeroing vireoNumFledglings)
                        String fledgingEvent = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), eState.currentJulianDay,
                                "fledgling independence", this.vireoNestTerrID, this.vireoID, this.nestID,
                                this.vireoNumFledglings, "nest", currentNest.youngSpecies);
                        eState.logSuccessWriter.addToFile(fledgingEvent);
                        killCurrentNest(eState); // nest lifecycle complete
                        this.vireoReproStage = Stage.RENEST;
                        this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration;
                        vireoNumFledglings = 0;
                    }
                }
                break;
            case RENEST: //Stage 10
                //evaluate if there is enough time for re-nesting
                if (currentJulianDate + eState.mpIncubationStageDuration + eState.mpNestlingStageDuration + eState.mpFledglingStageDuration + eState.mpRenestingIntervalDuration > eState.mpLastPossibleNestingDate) {
                    //if there is NO time for re-nesting
                    //actEndBreeding
                    this.vireoReproStage = Stage.NONBREEDING; //prepare for NON-Breeding
                } else { //if there is enough time for re-nesting <= mpLastPossibleNestingDate
                    if(nestCountdownToRenesting > 0){
                        nestCountdownToRenesting--; //count down to renesting
                    } else { //go to renesting stage next step
                        this.nestCountdownToRenesting = eState.mpRenestingIntervalDuration; // reset for next attempt
                        this.vireoReproStage = Stage.NESTCREATION; // create a fresh nest for the new attempt
                    }
                }
                break;
            case NONBREEDING: //Stage 11
                if(currentJulianDate == eState.mpLastPossibleNestingDate || this.vireoCurrentNumAttempts == eState.mpMaxNumAttempts || this.vireoReproStage == Stage.NONBREEDING) {
                    //logReproductivePerformance.csv
                    //(1) NumPair (2) NumNest (3) NumEggs (4) NumNestlings (5) NumFledglings (6) NumIndependentFledglings (7) NumSuccessfulNests
                    if (!this.vireoMateStatus) {
                        eState.nSingles++;
                    }
                    //update a bunch of Vireo state variables
                    deathByOldAge(eState);
                    interannualMortality(eState);
                    updateLBVIStateVariables(eState);
                }
                break;
        }
    }

    /**
     * DEBUG: writes this agent's per-step state to logDebug.txt.
     * Header (LBVIEnvironment): "currentStep", "Date", "vireoID", "sex", "ageClass",
     * "LBVIStage", "arrivalDate", "currentLoc", "potentialTerrCount".
     * Compare arrivalDate against Date to confirm the ARRIVAL→DISPERSAL gate; a stuck agent
     * with an empty potentialTerrCount is bouncing NESTTERRSELECT→DISPERSAL with no eligible territory.
     */
    private void logDebugState(LBVIEnvironment state) {
        if (!state.debugLog) return;                        // debug logging disabled
        if (this.vireoReproStage == Stage.ARRIVAL) return;  // skip pre-arrival rows (agent hasn't arrived yet)
        String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                currentStep, currentJulianDate, vireoID,
                vireoSexFemale ? "F" : "M", vireoAgeClassAdult ? "Adult" : "Juvenile",
                vireoReproStage, vireoArrivalDate, vireoCurrentLocation, potentialTerritoryList.size());
        state.debugWriter.addToFile(row);
    }
    /*
    ****************************************************************************************************
    *                                      ARRIVAL stage method
    * **************************************************************************************************
     */

    /**
     * The checkArrival method determine the arrival date if the agent hasn't detemined its arrival date for this breeding season
     * If the currentStep is larger than the arrival date, the agent move to the previous year location
     * @param state
     */
    public void checkArrival(LBVIEnvironment state) {
        //determine the arrival date if it haven't been determined
        if(vireoArrivalDate < 0 ) {
            vireoArrivalDate = ArrivalDate.arrivalDate(state.lowerArrivalDate, state.modeArrivalDate, state.upperArrivalDate);
        }
        //change previous location to current location when arriving the breeding site and move on to the next stage
        if(currentJulianDate >= vireoArrivalDate) {
            this.vireoCurrentLocation = this.vireoPreviousLocation;
            this.vireoReproStage = Stage.DISPERSAL;
        }
    }

    /*
     ****************************************************************************************************
     *                                      DISPERSAL stage method
     * **************************************************************************************************
     */

    public void dispersalKernel(LBVIEnvironment state) {
        vireoDispDir = Math.random() * 2 * Math.PI; //math.random() returns a double in range (0.0, 1.0) 2*Math.PI giving us a random angle in radians
        vireoDispDist = 0; //initiate the distance variable
        if(vireoSexFemale == true && vireoAgeClassAdult == true) { //Female Adult
            if (state.random.nextBoolean(state.mpVireoLongDistanceDispersal)) { //long dispersal distance
                vireoDispDist = state.random.nextDouble() * (state.mpVireoUpperCutoffLDD - state.mpVireoLowerCutoffLDD) + state.mpVireoLowerCutoffLDD;
            } else {//regular dispersal distance
                //Use Inverse Gaussian Function to generate a random dispersal distance; mu=2.66, lambda=0.33
                vireoDispDist = DispersalKernal.sampleInverseGaussian_SSJ(2.66, 0.33);
            }
        } else if(vireoSexFemale == false && vireoAgeClassAdult == true) { //Male Adult
            if (state.random.nextBoolean(state.mpVireoLongDistanceDispersal)) {
                vireoDispDist = state.random.nextDouble() * (state.mpVireoUpperCutoffLDD - state.mpVireoLowerCutoffLDD) + state.mpVireoLowerCutoffLDD;
            } else { //regular dispersal distance
                //Use 2Dt Function to generate a random dispersal distance; a=0.078, b=1.47
                vireoDispDist = DispersalKernal.sampleBivariateT(0.078, 1.47);
            }
        } else if (vireoSexFemale == true && vireoAgeClassAdult == false) { //Female Juveniles
            if (state.random.nextBoolean(state.mpVireoLongDistanceDispersal)) {
                vireoDispDist = state.random.nextDouble() * (state.mpVireoUpperCutoffLDD - state.mpVireoLowerCutoffLDD) + state.mpVireoLowerCutoffLDD;
            } else { //regular dispersal distance
                //Use Weibull distribution Function to generate a random dispersal distance; scale_a=5.49, shape_b=1.22
                vireoDispDist = DispersalKernal.sampleWillBull(1.22, 5.49);
            }
        } else { //Male Juveniles
            if (state.random.nextBoolean(state.mpVireoLongDistanceDispersal)) {
                vireoDispDist = state.random.nextDouble() * (state.mpVireoUpperCutoffLDD - state.mpVireoLowerCutoffLDD) + state.mpVireoLowerCutoffLDD;
            } else { //regular dispersal distance
                //Use 2Dt Function to generate a random dispersal distance; a=3.33, b=2.41
                vireoDispDist = DispersalKernal.sampleBivariateT(3.33, 2.41);
            }
        }
    }

    public ArrayList<Integer> findPotentialTerritoryList(LBVIEnvironment state, double coefficient) {
        //(1) current location info
        VegInfoIdentifier currentTerritory = state.vegTerrInfo.get(vireoCurrentLocation); //currentLocation is a terrID, which is the index
        if (currentTerritory == null) {
            System.err.println("findPotentialTerritoryList: no vegInfo for terrID = " + currentTerritory);
            return potentialTerritoryList;
        }
        //current location POINT_X and POINT_Y from shapefile
        vireoCurrentX = currentTerritory.terrCoordX;
        vireoCurrentY = currentTerritory.terrCoordY;
        //(2) new location after dispersal
        double newVireoX = vireoCurrentX + vireoDispDist * Math.cos(vireoDispDir);
        double newVireoY = vireoCurrentY + vireoDispDist * Math.sin(vireoDispDir);
        //(3) define buffer zone around new location
        //define buffer zone
        double xLowerBound = newVireoX - coefficient * state.bufferDistance;
        double yLowerBound = newVireoY - coefficient * state.bufferDistance;
        double xUpperBound = newVireoX + coefficient * state.bufferDistance;
        double yUpperBound = newVireoY + coefficient * state.bufferDistance;
        //(4) loop through all territories from vegInfo
        for (VegInfoIdentifier p: state.vegTerrInfo.values()) {
            double px = p.terrCoordX;
            double py = p.terrCoordY;
            //check if territory center is inside the rectangle
            if(xLowerBound < px && px < xUpperBound && yLowerBound < py && py < yUpperBound) {
                potentialTerritoryList.add(p.terrID);
            }
        }
        // (5) exclude unsuitable territories from potentialTerritoryList
        if (!potentialTerritoryList.isEmpty()) {
            for (int i = potentialTerritoryList.size()-1; i>=0; i--) {
                VegInfoIdentifier terr = state.vegTerrInfo.get(potentialTerritoryList.get(i));
                if (!this.vireoSexFemale && terr.terrMaleID != 0) { //this is a male agent, and this territory already has another male
                    //exclude this territory from potentialList
                    potentialTerritoryList.remove(i);
                } else if (this.vireoSexFemale && terr.terrFemaleID != 0) { //this is a female agent, and this territory already has another female
                    //exclude this territory from potentialList
                    potentialTerritoryList.remove(i);
                } else if (terr.terrQuality == 0) {
                    //exclude this territory from potentialList
                    potentialTerritoryList.remove(i);
                }
            }
        }
        return potentialTerritoryList;
    }

    /*
This method decides which patch a vireo will choose as its nesting site. The decision depends on the bird’s patch
selection trait, which can follow one of three strategies:
(1) habitat quality only,
(2) Mate availability then rank habitat quality
(3) Mate availability only
(4) Randomly choose one territory from the potential list
Once the patch is chosen, the bird updates its location and changes stage to PAIR, meaning it’s ready to pair up
for breeding.
 */
    public void nestTerritorySelection(LBVIEnvironment state) {
        //capture the dispersal origin before a territory is chosen (vireoCurrentLocation is overwritten on selection)
        int dispersalOrigin = this.vireoCurrentLocation;
        //Step 1: remove territories that already has same-sex agent from potentialTerritoryList
        List<Integer> qualityRank;
        switch (state.mpTerrSelectionTrait) {
            case 0: //TRAIT 1 - habitat quality only
                qualityRank = VegetationChange.rankTerritoryByQuality(potentialTerritoryList, state.vegTerrInfo);
                if (qualityRank.isEmpty()) {
                    this.vireoReproStage = Stage.DISPERSAL;
                } else {
                    this.vireoCurrentLocation = qualityRank.get(0);
                    this.vireoReproStage = Stage.PAIR;
                }
                break;
            case 1: //TRAIT 2 - Mate availability only
                List<Integer> mateTerrList = this.vireoSexFemale
                        ? VegetationChange.queryTerrByPotentialMales(potentialTerritoryList, state.vegTerrInfo)
                        : VegetationChange.queryTerrByPotentialFemales(potentialTerritoryList, state.vegTerrInfo);
                if (potentialTerritoryList.isEmpty()) {
                    this.vireoReproStage = Stage.DISPERSAL;
                } else if (!mateTerrList.isEmpty()) {
                    this.vireoCurrentLocation = mateTerrList.get(state.random.nextInt(mateTerrList.size()));
                    this.vireoReproStage = Stage.PAIR;
                } else {
                    this.vireoCurrentLocation = potentialTerritoryList.get(state.random.nextInt(potentialTerritoryList.size()));
                    this.vireoReproStage = Stage.PAIR;
                }
                break;
            case 2: //TRAIT 3 - Rank habitat quality and then mate availability
                qualityRank = VegetationChange.rankTerritoryByQuality(potentialTerritoryList, state.vegTerrInfo);
                if (qualityRank.isEmpty()) {
                    this.vireoReproStage = Stage.DISPERSAL;
                } else {
                    int topQuality = state.vegTerrInfo.get(qualityRank.get(0)).terrQuality;
                    ArrayList<Integer> topTied = new ArrayList<>();
                    for (Integer id : qualityRank) {
                        if (state.vegTerrInfo.get(id).terrQuality == topQuality) {
                            topTied.add(id);
                        } else {
                            break;
                        }
                    }
                    if (topTied.size() > 1) {
                        List<Integer> mateInTied = this.vireoSexFemale
                                ? VegetationChange.queryTerrByPotentialMales(topTied, state.vegTerrInfo)
                                : VegetationChange.queryTerrByPotentialFemales(topTied, state.vegTerrInfo);
                        if (!mateInTied.isEmpty()) {
                            this.vireoCurrentLocation = mateInTied.get(state.random.nextInt(mateInTied.size()));
                        } else {
                            this.vireoCurrentLocation = topTied.get(state.random.nextInt(topTied.size()));
                        }
                    } else {
                        this.vireoCurrentLocation = topTied.get(0);
                    }
                    this.vireoReproStage = Stage.PAIR;
                }
                break;
            case 3: //TRAIT 4 - Mate availability and then habitat quality
                List<Integer> mateTerrs = this.vireoSexFemale
                        ? VegetationChange.queryTerrByPotentialMales(potentialTerritoryList, state.vegTerrInfo)
                        : VegetationChange.queryTerrByPotentialFemales(potentialTerritoryList, state.vegTerrInfo);
                if (potentialTerritoryList.isEmpty()) {
                    this.vireoReproStage = Stage.DISPERSAL;
                } else if (!mateTerrs.isEmpty()) {
                    qualityRank = VegetationChange.rankTerritoryByQuality(new ArrayList<>(mateTerrs), state.vegTerrInfo);
                    double topMateQuality = state.vegTerrInfo.get(qualityRank.get(0)).terrQuality;
                    ArrayList<Integer> topMateTied = new ArrayList<>();
                    for (Integer id : qualityRank) {
                        if (state.vegTerrInfo.get(id).terrQuality == topMateQuality) {
                            topMateTied.add(id);
                        } else {
                            break;
                        }
                    }
                    this.vireoCurrentLocation = topMateTied.get(state.random.nextInt(topMateTied.size()));
                    this.vireoReproStage = Stage.PAIR;
                } else {
                    this.vireoCurrentLocation = potentialTerritoryList.get(state.random.nextInt(potentialTerritoryList.size()));
                    this.vireoReproStage = Stage.PAIR;
                }
                break;
            case 4: //TRAIT 4 - totally random
                if (potentialTerritoryList == null || potentialTerritoryList.size() == 0) {
                    this.vireoReproStage = Stage.DISPERSAL;
                } else {
                    this.vireoCurrentLocation = potentialTerritoryList.get(state.random.nextInt(potentialTerritoryList.size()));
                    this.vireoReproStage = Stage.PAIR;
                }
                break;
        }
        //a territory was chosen this step (stage advanced to PAIR): record the realized dispersal
        if (this.vireoReproStage == Stage.PAIR) {
            this.vireoPreviousLocation = dispersalOrigin;
            logDispersalDistance(state, dispersalOrigin);
        }
    }

    /**
     * Records a realized dispersal to logDispersalDistance.csv, aligned to logDispersalDistanceHeader:
     * "currentStep", "Date", "terrID", "vireoID", "PrevLoc", "Distance"
     * Distance is the Euclidean distance (meters) between the previous territory's center and the
     * chosen territory's center, using terrCoordX/terrCoordY from vegTerrInfo.
     */
    private void logDispersalDistance(LBVIEnvironment state, int prevLoc) {
        VegInfoIdentifier from = state.vegTerrInfo.get(prevLoc);
        VegInfoIdentifier to = state.vegTerrInfo.get(this.vireoCurrentLocation);
        double distance = Math.hypot(to.terrCoordX - from.terrCoordX, to.terrCoordY - from.terrCoordY);
        String record = String.format("%s,%s,%s,%s,%s,%.4f", state.schedule.getSteps(), state.currentJulianDay,
                this.vireoCurrentLocation, this.vireoID, prevLoc, distance);
        state.logDispersalDistanceWriter.addToFile(record);
    }
    /*
    ***********************************************************************************************
    *                   REPRODUCTION & PARENTAL CARE
    * *********************************************************************************************
     */
    public void eggLaying(LBVIEnvironment state, LBVIAgent femaleAgent) {
        if(vireoNumEggs < femaleAgent.nestMaxNVireoEggs) {
            vireoNumEggs++;
        } else { //when the vireoNumEggs = nestMaxNEggs
            femaleAgent.vireoReproStage = Stage.INCUBATION;
            femaleAgent.nestCountdownToHatch = state.mpIncubationStageDuration;
            femaleAgent.vireoNumNestAttempts ++; //double check ODD with Casey
            state.nEggs += femaleAgent.vireoNumEggs;
            //record the clutch completion in output
            //log SuccessEvents.csv, log the egglaying of the nest
            String eggLayingEvent = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", state.schedule.getSteps(), state.currentJulianDay, "clutch completion", femaleAgent.vireoNestTerrID,
                    femaleAgent.vireoID, femaleAgent.nestID, femaleAgent.vireoNumEggs, "nest", femaleAgent.currentNest.youngSpecies);
            state.logSuccessWriter.addToFile(eggLayingEvent);
        }
        femaleAgent.vireoNestStatus = NESTStatus.EGG;
    }
    /**
     * The reproduction action is executed when the young-of-
     * @param state
     * @param femaleAgent
     * @return
     */
    public LBVIAgent reproduceAgent(LBVIEnvironment state, LBVIAgent femaleAgent) {
        int newbornID = state.lbviAgentID ++;
        boolean newbornSex = state.random.nextBoolean(state.mpProbVireoIsFemale); //true = female, false = male
        int newbornDeathAge = state.random.nextInt(state.mpVireoMaximumAge);
        LBVIAgent newAgent = new LBVIAgent(state, newbornID, newbornSex, false, state.mpVireoMaximumAge,
                femaleAgent.vireoCurrentLocation, false);
        femaleAgent.vireoRecruitList.add(newbornID);
        newAgent.event = state.schedule.scheduleRepeating(newAgent);
        return newAgent;
    }
    /*
    *****************************************************************************************
    *                           MORTALITY & DEATH
    * ***************************************************************************************
     */
    //Agent death by old age
    public void deathByOldAge (LBVIEnvironment state) {
        int random = (int) (state.random.nextInt(state.mpVireoMaximumAge));
        if (random <= this.vireoDeathAge) {
            this.die(state);
        }
    }
    //Agent inter-annual Mortality
    public void interannualMortality(LBVIEnvironment state) {
        if (this.vireoAgeClassAdult == false && this.vireoSexFemale == true) { //female young-of-the-year
            if (state.random.nextBoolean(state.mpSurvivalProbJuvenileF)) { //the female young-of-the-year survived
                this.vireoAgeClassAdult = true;
            } else {
                this.die(state); //this young-of-the-year doesnot survive through the winter
                //log the death in logVireoSurvivalOutcomes.csv
            }
        } else if (this.vireoAgeClassAdult == false && this.vireoSexFemale == false) { //male young-of-the-year
            if (state.random.nextBoolean(state.mpSurvivalProbJuvenileM)) {
                this.vireoAgeClassAdult = true;
            } else {
                this.die (state); //this young-of-the-year doesnot survive through the winter
                //log the death in logVireoSurvivalOutcomes.csv
            }
        } else if (this.vireoAgeClassAdult == true && this.vireoSexFemale == true) { //female adult
            if (state.random.nextBoolean(state.mpSurvivalProbAdultF)) { //this adult female survive
                updateLBVIStateVariables(state);
            } else {
                this.die(state); //female adult die during the winter
            }
        } else { //female adult
            if (state.random.nextBoolean(state.mpSurvivalProbAdultM)) { //this adult male survive
                updateLBVIStateVariables(state);
            } else {
                this.die(state); //male adult die during the winter
            }
        }
    }

    /*
    ***********************************************************************************************
    *                               HELPER METHODS
    * *********************************************************************************************
     */
    public int getNestMaxNEggs(LBVIEnvironment state) {
        double rate = state.mpClutchSize;
        PoissonDistribution poisson = new PoissonDistribution(rate);
        int sample = poisson.sample();
        int result = Math.max(1, sample); // ensure minimum of 1
        System.out.println("nestMaxNEggs: " + result);
        return result;
    }

    // Logs a daily-mortality event to logMortalityEvents.csv, aligned to logMortalityHeader:
    // "step", "Date", "EventType", "TerrID", "VireoID", "NestID", "CowbirdID", "NumIndividual", "EntityType", "YoungSp"
    // CowbirdID is "NA" because these deaths are not cowbird-related. Call before killCurrentNest,
    // which clears currentNest (used here to read youngSpecies).
    private void logDailyMortalityForNest(LBVIEnvironment eState, String eventType, int numIndividual, NESTStatus entityType) {
        String youngSp = (currentNest != null) ? currentNest.youngSpecies : "LBVI";
        String dailyMortality = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", eState.schedule.getSteps(), eState.currentJulianDay,
                eventType, this.vireoNestTerrID, this.vireoID, this.nestID, "NA", numIndividual, entityType, youngSp);
        eState.logMortalityWriter.addToFile(dailyMortality);
    }

    // Stops the active nest, removes it from global tracking, and clears the female's reference.
    // Safe to call regardless of which entity (female or Nest's TLB check) triggered the death.
    private void killCurrentNest(LBVIEnvironment eState) {
        if (currentNest == null) return;
        if (currentNest.nestStatus != NESTStatus.DEAD) {
            currentNest.nestStatus = NESTStatus.DEAD;
            currentNest.event.stop();
        }
        eState.activeNests.remove(currentNest);
        currentNest = null;
    }

    public void die(LBVIEnvironment state) {
        //1. remove from their social network (parents, mateID, etc.)
        //2. stop the event
        event.stop(); //remove the agent from the schedule
        state.vegetationGrid.remove(this);
        state.lbviAgentMap.remove(this.vireoID);
    }

    public void updateLBVIStateVariables(LBVIEnvironment state) {
        this.vireoAgeYears ++;
        if (this.vireoAgeYears >= 1) {
            this.vireoAgeClassAdult = true;
        } else {
            this.vireoAgeClassAdult = false;
        }
        this.vireoStartingLocation = vireoCurrentLocation;
        this.vireoPreviousLocation = vireoCurrentLocation;
        this.vireoArrivalDate = -1;
        this.vireoReproStage = Stage.ARRIVAL;
        this.vireoMateStatus = false;
        this.vireoCurrentNumAttempts = 0;
        if(this.vireoSexFemale) { //only females track below state variables
            this.vireoNumNestAttempts = 0;
            this.vireoNestID = 0;
            this.vireoNumEggs = 0;
            this.vireoNumNestlings = 0;
            this.vireoNumFledglings = 0;
            this.vireoNumRecruits = 0;
            this.vireoYoungSpecies = true;
            this.vireoNestlingSpecies = true;
            //nest related
            this.nestID = 0;
            this.nestMaxNVireoEggs = getNestMaxNEggs(state);
            this.vireoNestTerrID = 0;
            this.currentNest = null;
            this.nestCountdownToHatch = state.mpIncubationStageDuration;
            this.nestCountdownToFledging = state.mpNestlingStageDuration;
            this.nestCountdownToIndependence = state.mpFledglingStageDuration;
            this.nestCountdownToRenesting = state.mpRenestingIntervalDuration;
        }
        potentialTerritoryList = new ArrayList<>();
        vireoMateID = 0;
    }


    /* =========================================================
       ARCHIVED — kept for reference, not called by active code
       ========================================================= */

    @Deprecated
    public void dispersalKernel_old(LBVIEnvironment state) {
        vireoDispDir = Math.random() * 2 * Math.PI;
        vireoDispDist = 0;
        if(vireoSexFemale == true && vireoAgeClassAdult == true) { //Female Adult
            vireoDispDist = DispersalKernal.sampleInverseGaussian_SSJ(2.66, 0.33);
            if(vireoDispDist > state.vireoBeyondKernalFAd) {
                double[] arr = {24, 25, 29};
                Random random = new Random();
                vireoDispDist = arr[random.nextInt(arr.length)];
            }
        } else if(vireoSexFemale == false && vireoAgeClassAdult == true) { //Male Adult
            vireoDispDist = DispersalKernal.sampleBivariateT(0.078, 1.47);
            if(vireoDispDist > state.vireoBeyondKernalMAd) {
                double[] arr = {24, 24, 25, 35, 36, 40, 63, 72, 79, 84, 104, 158};
                Random random = new Random();
                vireoDispDist = arr[random.nextInt(arr.length)];
            }
        } else if (vireoSexFemale == true && vireoAgeClassAdult == false) { //Female Juveniles
            vireoDispDist = DispersalKernal.sampleWillBull(1.22, 5.49);
            if(vireoDispDist > state.vireoBeyondKernalFJu) {
                vireoDispDist = 0;
            }
        } else { //Male Juveniles
            vireoDispDist = DispersalKernal.sampleBivariateT(3.33, 2.41);
            if(vireoDispDist > state.vireoBeyondKernalMJu) {
                double[] arr = {22,23,25,26,61,89,97,105};
                Random random = new Random();
                vireoDispDist = arr[random.nextInt(arr.length)];
            }
        }
    }

//    public void nestTerritorySelection(LBVIEnvironment state) {  // old version using patch-based vegInfo
//        System.out.println(this.potentialTerritoryList);
//        for(int i=0; i< this.potentialTerritoryList.size(); i++) {
//            if(this.vireoSexFemale && state.vegInfo.get(potentialTerritoryList.get(i)).terrUnmatedFemaleID != -1) {
//                potentialTerritoryList.remove(i);
//            } else if (this.vireoSexFemale == false && state.vegInfo.get(potentialTerritoryList.get(i)).terrUnmatedMaleID != -1){
//                potentialTerritoryList.remove(i);
//            }
//        }
//        List<Integer> qualityRank;
//        switch (state.mpPatchSelectionTrait) {
//            case 0: //TRAIT 1 - habitat quality only
//                qualityRank = VegetationChange.rankTerrByQuality(potentialTerritoryList, state.vegInfo);
//                if (qualityRank.isEmpty()) { this.vireoReproStage = Stage.DISPERSAL; }
//                else { this.vireoCurrentLocation = qualityRank.get(0); this.vireoReproStage = Stage.PAIR; }
//                break;
//            case 1: //TRAIT 2 - Mate availability then rank habitat quality
//                ArrayList<Integer> mateAvailableList = new ArrayList<Integer>();
//                int index = -1; int sum = 0;
//                qualityRank = VegetationChange.rankTerrByQuality(potentialTerritoryList, state.vegInfo);
//                for (int i=0; i< qualityRank.size(); i++) {
//                    if (state.vegInfo.get(qualityRank.get(i)).territoryNumVireos == 0) { mateAvailableList.add(0); }
//                    else { mateAvailableList.add(1); index = i; break; }
//                }
//                for (int v : mateAvailableList) { sum += v; }
//                if(qualityRank.isEmpty()) { this.vireoReproStage = Stage.DISPERSAL; }
//                else {
//                    this.vireoCurrentLocation = (sum == 0) ? qualityRank.get(0) : qualityRank.get(index);
//                    this.vireoReproStage = Stage.PAIR;
//                }
//                break;
//            case 2: //TRAIT 3 - Mate availability only
//                ArrayList<Integer> mateTerrList = new ArrayList<>();
//                ArrayList<Integer> emptyList = new ArrayList<>();
//                for (int i=0; i< potentialTerritoryList.size(); i++) {
//                    if (state.vegInfo.get(potentialTerritoryList.get(i)).territoryNumVireos == 0)
//                        emptyList.add(potentialTerritoryList.get(i));
//                    else mateTerrList.add(potentialTerritoryList.get(i));
//                }
//                if (potentialTerritoryList == null || potentialTerritoryList.size() == 0) { this.vireoReproStage = Stage.DISPERSAL; }
//                else if (mateTerrList.size() != 0) {
//                    this.vireoCurrentLocation = mateTerrList.get(state.random.nextInt(mateTerrList.size()));
//                    this.vireoReproStage = Stage.PAIR;
//                } else {
//                    this.vireoCurrentLocation = emptyList.get(state.random.nextInt(emptyList.size()));
//                    this.vireoReproStage = Stage.PAIR;
//                }
//                break;
//            case 3: //TRAIT 4 - totally random
//                if (potentialTerritoryList == null || potentialTerritoryList.size() == 0) { this.vireoReproStage = Stage.DISPERSAL; }
//                else { this.vireoCurrentLocation = potentialTerritoryList.get(state.random.nextInt(potentialTerritoryList.size())); this.vireoReproStage = Stage.PAIR; }
//                break;
//        }
//    }

}
