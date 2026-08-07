package lbvi;

import sim.engine.SimState;
import sim.engine.Steppable;

public class LBVITimer implements Steppable {

    @Override
    public void step(SimState simState) {
        LBVIEnvironment eState = (LBVIEnvironment) simState; //downcasting the PSHB environment
        //update the timer
        eState.updateYear();
        eState.updateJulianDay();
        System.out.println("===================================================");
        System.out.println("Update Current year: " + eState.currentYear);
        System.out.println("Update Current julianDay: " + eState.currentJulianDay);
        System.out.println("===================================================");
    }
}
