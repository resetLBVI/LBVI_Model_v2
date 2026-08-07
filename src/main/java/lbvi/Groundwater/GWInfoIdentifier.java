package lbvi.Groundwater;

/**
 * Initial groundwater inputs
 * "PatchID"       "year"          "DGW"           "WaterYearType" "WaterStress"
 */

public class GWInfoIdentifier {
    public int patchID;
    public int year;
    public double DGW;
    public String WaterYearType; //5 levels - Above Normal, Below Normal, Critical, Dry, Wet
    public WaterStress WaterStress;

    public GWInfoIdentifier(int patchID, int year, double DGW, String waterYearType, WaterStress waterStress) {
        this.patchID = patchID;
        this.year = year;
        this.DGW = DGW;
        WaterYearType = waterYearType;
        WaterStress = waterStress;
    }
}
