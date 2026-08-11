package lbvi;

/*
Initial vegetation attributes
 [1] "PatchID"    "TerrID"     "CoordX"     "CoordY"     "Acres"      "MapCode"    "VegName"    "TWDens"     "SWMFDens"
   "ArundoDens"   "TamarDens"  "Quality1"
 */


import lbvi.Groundwater.WaterStress;
import sim.util.Int2D;
import org.locationtech.jts.geom.Geometry;


public class VegInfoIdentifier {
    //initial patch map information (shp file):
    public final int patchID; //#1
    public final int terrID; //#2
    public final double terrCoordX; //#3
    public final double terrCoordY; //#4
    public final double Acres; //#5
    public final int MapCode; //#6
    public final String VegName; //#7 Can change overtime
    public final double TWDens; //#8 Can change overtime
    public final double SWMFDens; //#9 Can change overtime
    public final double ArundoDens; //#10
    public final double TamarDens; //#11
    public final double quality1; //#12

    /* Geographic range */
    public int patchPopUnitID; // I don't know which name to correspond to the input
    public String patchStreamName; //should be streamName in the input??
    public int patchReachID; //Not include in the initiation input yet
    public String patchStreamNetwork; //L1NetworkN in the input file?
    public String patchStreamLevel; //StreamLeve??
    public final Geometry geometry;
    public final Int2D gridLoc;

    //track current ecological conditions
    /* update Patch vegetation health*/
    public int patchVegCondition; //a continuous numbe from 0-20
    public VegState patchVegState; //0-4, indicating dead(0), severely stressed(1), stressed(2), healthy(3), very healthy(4)
    public int patchNyrsSinceDeath; //patch N years since water stress mortality 2.1.3
    public int patchQuality;//a int value from 0-n when n may vary across different habitat quality indices
    /* update territory vegetation information */
    public String terrVegName;
    public int terrVegCondition; //0-20 numerical
    public int terrVegState; //0-4 categorical; indicating dead(0), severely stressed(1), stressed(2), healthy(3), very healthy(4)
    public int terrQuality;
    public int terrShWillowM; //territory tree willow mulefat density
    public int terrTrWillow;
    public int terrTamarisk;

    /* territory Vireo information */
    public int terrMaleID;
    public int terrFemaleID;

    /* Daily Vireo and Cowbird updates*/
    public int patchNVireos; //current number of Vireos in the patch
    public int patchNTerrWithPairs; //i.e., patchCurrentNests; current number of nests in the patch, which is equal to the number of territories with pairs in the patch
    public int patchNTerrNoPairs; //the current number of territories with no vireos or only a single vireo in them
    public int patchNVireoNests; //the current number of vireo nests in the patch
    public int patchNVireoEggs; //the current number of vireo eggs in the patch
    public int patchNVireoNestlings; //the current number of vireo nestlings in the patch
    public int patchNVireoFledglings; //the current number of vireo fledglings in the patch
    public int patchNCowbirds; //number of Cowbird in the patch

    /* Annual groundwater and flooding level updates (time series input files) from previous version of ODD*/
    public double patchGroundwaterLevels; //groundwater level (inputs: double- DGW)
    public WaterStress patchWaterStress; //stress due to groundwater level
    Integer patchNumGrowing_Flood; //the number of growing seasons since the last flood
    Integer patchNumGrowing_EffluentRemoval; //the number of growing seasons since stream flow from wastewater treatment

    /* Weekly PSHB updates (time series input files) */
    public int patchNPSHB; //number of PSHB in the patch
    public boolean patchVegMortalityByPSHB; //default == false, updated TRUE when Tamarisk has been killed
    public int patchNumGrowing_PSHB; //the number of growing seasons since the last PSHB induced mortality event
    public int patchShrubWillowKilled; //a value of 1-3. killed by PSHB, old version

    /* Weekly TLB updates (time series input files) */
    public int patchNTLB; //number of TLB in the patch
    public boolean terrTamariskMortalityByTLB; //default == false, updated TRUE when Tamarisk has been killed
    public int terrNYrsGrowing_TLB; //the number of growing seasons since the last TLB defolitation event
    public boolean terrDefoliated; //territory defoliated or not, boolean
    public int terrNDefoliatedEvents; //Not shown in updated ODD

    /* Administrative categories */
    public String patchManagerName;
    public String patchManageAreaName;
    public String patchRecoverRegion;
    public String patchCounty;
    public boolean patchIsSGMA;
    public String patchEffluSource;

    //From old version of ODD
    Integer patchPercentArundokilled; //, a value of 1-3, where 3 represents the highest amount of exotic grass killed by management.
    public int patchNumSuccessNests; //from old version of ODD
    Integer terrUnmatedMaleID;
    Integer terrUnmatedFemaleID;
    Integer territoryNumMales; //current # of males in the territory
    Integer territoryNumFemales; //current # of females in the territory
    Integer territoryNumVireos; //current total # of vireo agents in the territory

    public VegInfoIdentifier(int patchID, int terrID, double terrCoordX, double terrCoordY, double acres, int mapCode,
                             String vegName, double TWDens, double swmfDens, double arundoDens, double tamarDens, double quality1,
                             Geometry geometry, Int2D gridLoc) {
        this.patchID = patchID;
        this.terrID = terrID;
        this.terrCoordX = terrCoordX;
        this.terrCoordY = terrCoordY;
        Acres = acres;
        MapCode = mapCode;
        VegName = vegName;
        this.TWDens = TWDens;
        SWMFDens = swmfDens;
        ArundoDens = arundoDens;
        TamarDens = tamarDens;
        this.quality1 = quality1;
        this.terrQuality = (int) quality1; //initial habitat quality index (0-n) from the Quality1 input column
        this.geometry = geometry;
        this.gridLoc = gridLoc;
        this.terrMaleID = -1;
        this.terrFemaleID = -1;
    }

    public void vegConditionToState() {
        if(this.patchVegCondition >= 15) {
            this.patchVegState = VegState.VERYHEALTHY;
        } else if (this.patchVegCondition >= 10 && patchVegCondition < 15) {
            this.patchVegState = VegState.HEALTHY;
        } else if (this.patchVegCondition >= 5 && patchVegCondition <10) {
            this.patchVegState = VegState.STRESS;
        } else if (this.patchVegCondition >= 1 && patchVegCondition < 5) {
            this.patchVegState = VegState.SEVERELYSTRESSED;
        } else if (this.patchVegCondition == 0) {
            this.patchVegState = VegState.DEAD;
        } else {
            System.out.println ("this patch veg condition is beyond the scope.");
        }
    }

    public void updatePatchQuality(Integer patchQuality) {
        this.patchQuality = patchQuality;
    }

    /*
    **********************************************************************************
    *                       Getters and Setters
    * ********************************************************************************
     */


}