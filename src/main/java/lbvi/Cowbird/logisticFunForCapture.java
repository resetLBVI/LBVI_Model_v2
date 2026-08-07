package lbvi.Cowbird;

import lbvi.LBVIEnvironment;
import lbvi.Traps.TrapInfoIndentifier;
import lbvi.VegInfoIdentifier;

import java.util.ArrayList;
import java.util.List;

public class logisticFunForCapture {

    /**
     * Computes the distance-adjusted capture probability using a logistic function
     * fit through two anchor points derived from the environment parameters:
     *   - At d = 0.1 * mpMaxCowbirdCaptureDistM:  P = mpP90CaptureProb  (high probability, close to trap)
     *   - At d = 0.9 * mpMaxCowbirdCaptureDistM:  P = mpP10CaptureProb  (low probability, far from trap)
     *
     * Standard logistic form:  P(d) = 1 / (1 + exp(-(a + b*d)))
     * Parameters a and b are solved via the logit transformation of the two anchor points.
     *
     * @param distance  distance from cowbird to trap (meters)
     * @param state     the simulation environment (provides mpMaxCowbirdCaptureDistM,
     *                  mpP10CaptureProb, mpP90CaptureProb)
     * @return          distance-adjusted capture probability at the given distance
     */
    public static double computeCaptureProb(LBVIEnvironment state, double distance) {
        double maxDist = state.mpMaxCowbirdCaptureDistM;
        double p10     = state.mpP10CaptureProb;
        double p90     = state.mpP90CaptureProb;
        double d1 = 0.1 * maxDist;  // anchor distance for high probability (p90)
        double d2 = 0.9 * maxDist;  // anchor distance for low probability  (p10)
        double logitP90 = Math.log(p90 / (1.0 - p90));
        double logitP10 = Math.log(p10 / (1.0 - p10));
        // slope b is negative (probability decreases with distance)
        double b = (logitP10 - logitP90) / (d2 - d1);
        double a = logitP90 - b * d1;
        return 1.0 / (1.0 + Math.exp(-(a + b * distance)));
    }

    /**
     * Returns all traps that are open on the current simulation day and year.
     * A trap is open when its trapYear matches the current year and
     * currentJulianDay falls within [trapOpenDate, trapCloseDate].
     *
     * @param state the simulation environment (provides trapInfo, currentYear, currentJulianDay)
     * @return list of currently open traps
     */
    public static List<TrapInfoIndentifier> findOpenTraps(LBVIEnvironment state) {
        List<TrapInfoIndentifier> openTraps = new ArrayList<>();
        for (TrapInfoIndentifier trap : state.trapInfo.values()) {
            if (trap.year == state.currentYear &&
                state.currentJulianDay >= trap.trapOpenDate &&
                state.currentJulianDay <= trap.trapCloseDate) {
                openTraps.add(trap);
            }
        }
        return openTraps;
    }

    /**
     * Determines whether a newly arrived cowbird is captured by any eligible trap.
     * The cowbird's location is resolved from nestTerrID via state.vegInfo, replacing
     * the need for a separate findNearbyCowbirdTraps() call.
     * For each trap within mpMaxCowbirdCaptureDistM, a separate random draw is made
     * against the distance-adjusted capture probability. If any draw succeeds, the
     * cowbird is captured.
     *
     * @param state       the simulation environment
     * @param nestTerrID  territory ID of the nest (and cowbird) location
     * @param openTraps      list of active traps to check
     * @return            true if the cowbird is captured by any eligible trap, false otherwise
     */
    public static boolean isCaptured(LBVIEnvironment state, int nestTerrID, List<TrapInfoIndentifier> openTraps) {
        VegInfoIdentifier currentTerritory = state.vegTerrInfo.get(nestTerrID);
        if (currentTerritory == null) {
            System.err.println("isCaptured: nestTerrID " + nestTerrID + " not found in vegInfo");
            return false;
        }
        double cowbirdX = currentTerritory.terrCoordX;
        double cowbirdY = currentTerritory.terrCoordY;
        for (TrapInfoIndentifier trap : openTraps) {
            double dx = cowbirdX - trap.trapPOINT_X;
            double dy = cowbirdY - trap.trapPOINT_Y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > state.mpMaxCowbirdCaptureDistM) continue; // trap not eligible
            double captureProb = computeCaptureProb(state, distance);
            if (state.random.nextDouble() < captureProb) {
                return true;
            }
        }
        return false;
    }
}
