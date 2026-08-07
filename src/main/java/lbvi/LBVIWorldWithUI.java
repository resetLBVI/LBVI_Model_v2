package lbvi;

import lbvi.Cowbird.CowbirdAgent;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import javax.swing.*;
import java.awt.*;

/**
 * This class includes:
 * (1) getName (2) getSimulationInspectiedObject (3) init (4) quit (5) setupPortrayals
 * (6) start (7) main
 */

public class LBVIWorldWithUI extends GUIState {
    private Display2D display; //create a display
    private JFrame displayFrame; //create a display frame
    //create the portrayals for (1) vegetation patches (2) Vireo agents (3) Nests (4) Cowbird agents
    private ObjectGridPortrayal2D backgroundPortrayal = new ObjectGridPortrayal2D(); //Background: 1×1 grid with ImagePortrayal2D
    private SparseGridPortrayal2D agentsPortrayal = new SparseGridPortrayal2D(); //all agents on vegetationGrid


    //Constructor
    public LBVIWorldWithUI(SimState state) {
        super(state);
    }

    public LBVIWorldWithUI() {
        super(new LBVIEnvironment(System.currentTimeMillis()));
    }

    public static String getName() {
        return "RESET: LBVI World";
    }

    public Object getSimulationInspectedObject() {
        return state;
    }

    public void init(Controller controller) {
        super.init(controller); //super from GUIState
        //create the display (size can be adjusted)
        this.display = new Display2D(800, 800, this); //initially create a UI display
        display.setClipping(false);
        //Attach portrayals in drawing order: background -> agents
        this.display.attach(backgroundPortrayal, "Vegetation Map", true);
        this.display.attach(agentsPortrayal, "Agents", true);
        //set up the frame
        this.displayFrame = display.createFrame(); //create a display frame
        controller.registerFrame(this.displayFrame); //setup this display
        this.displayFrame.setVisible(true); //setup this display
    }

    public void start() {
        super.start();
        this.setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void quit() {
        super.quit();
        if (this.displayFrame != null) {
            this.displayFrame.dispose(); //if there is a frame, dispose it
        }
        this.displayFrame = null;
        this.display = null;
    }

    public void setupPortrayals() {
        LBVIEnvironment eState = (LBVIEnvironment) state;

        /* ====================================
        1. Background (shapefile --> image)
        ======================================= */
        if (eState.backgroundGrid != null && eState.backgroundImage != null) {
            // tell the portrayal which grid to draw
            backgroundPortrayal.setField(eState.backgroundGrid);
            // Hide grid lines
//            backgroundPortrayal.setGridLines(false);

            // ensure there is at least one object to paint
            if (eState.backgroundGrid.get(0, 0) == null) {
                eState.backgroundGrid.set(0, 0, new Object());
            }
            // draw that object using the image. The "1.0" is scale: how much of the world each cell should cover.
            // Since backgroundGrid is 1x1, this just stretches the image to display size.
            backgroundPortrayal.setPortrayalForAll( new ImagePortrayal2D(eState.backgroundImage, 1.0)
            );
        }
        /* ============================
        2. Agents (LBVIAgent, Nest, CowbirdAgent) on vegetationGrid
        =============================== */
        agentsPortrayal.setField(eState.vegetationGrid);
        agentsPortrayal.setPortrayalForClass(LBVIAgent.class, new OvalPortrayal2D(Color.BLUE, 2.0));   // vireos: blue
        agentsPortrayal.setPortrayalForClass(Nest.class,      new OvalPortrayal2D(Color.ORANGE, 2.0)); // nests: orange
        agentsPortrayal.setPortrayalForClass(CowbirdAgent.class, new OvalPortrayal2D(Color.RED, 2.0)); // cowbirds: black

        /* =============================
        3. Refresh display
         =============================== */
        this.display.reset();
        this.display.setBackdrop(Color.WHITE); //set up the background color
        this.display.repaint();
    }

    public static void main(String[] args) {
        LBVIWorldWithUI worldGUI = new LBVIWorldWithUI();
        Console console = new Console(worldGUI);
        console.setVisible(true);
    }
}