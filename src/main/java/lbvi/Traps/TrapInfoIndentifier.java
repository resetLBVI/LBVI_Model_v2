package lbvi.Traps;

/*
Initial Trap information:
"POINT_X"    "POINT_Y"    "TrapID"     "Year"       "OpenDate"   "CloseDate"  "NDaysClose"
 */

public class TrapInfoIndentifier {
    public final double trapPOINT_X;
    public final double trapPOINT_Y;
    public final int TrapID;
    public final int year;
    public final int trapOpenDate;
    public final int trapCloseDate;
    public final int trapNDaysClose;

    public TrapInfoIndentifier(double POINT_X, double POINT_Y, int trapID, int year, int OpenDate, int CloseDate, int NDaysClose) {
        this.trapPOINT_X = POINT_X;
        this.trapPOINT_Y = POINT_Y;
        TrapID = trapID;
        this.year = year;
        this.trapOpenDate = OpenDate;
        this.trapCloseDate = CloseDate;
        this.trapNDaysClose = NDaysClose;
    }
}
