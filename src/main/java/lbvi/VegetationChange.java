package lbvi;

import lbvi.Groundwater.WaterStress;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VegetationChange implements Steppable {
    @Override
    public void step(SimState simState) {

    }

    /* #################################################################################
                                        Habitat Ranking
       ##################################################################################
     */
    public static List<Integer> rankTerritoryByQuality(ArrayList<Integer> potentialTerrList, Map<Integer, VegInfoIdentifier> vegTerrInfo) {
        return potentialTerrList.stream().sorted(Comparator.comparingDouble(
                (Integer id) -> vegTerrInfo.get(id).terrQuality).reversed()).collect(Collectors.toList());
    }

    public static List<Integer> queryTerrByPotentialMales(ArrayList<Integer> potentialTerrList, Map<Integer, VegInfoIdentifier> vegTerrInfo) {
        return potentialTerrList.stream().filter(id -> vegTerrInfo.get(id).terrMaleID != -1).collect(Collectors.toList());
    }

    public static List<Integer> queryTerrByPotentialFemales(ArrayList<Integer> potentialTerrList, Map<Integer, VegInfoIdentifier> vegTerrInfo) {
        return potentialTerrList.stream().filter(id -> vegTerrInfo.get(id).terrFemaleID != -1).collect(Collectors.toList());
    }

    /*
    #########################################################################################
                        Groundwater Impacts Vegetation Patch
    ##########################################################################################
     */
    public void vegResponseToGroundwater (LBVIEnvironment state, int patchID) {
        if (state.vegTerrInfo.get(patchID).patchWaterStress == WaterStress.ABOVENORMAL) {
            if(state.vegTerrInfo.get(patchID).patchVegState == VegState.VERYHEALTHY) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 1;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.HEALTHY) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 2;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.STRESS) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 3;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.SEVERELYSTRESSED) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 3;
            }
        } else if (state.vegTerrInfo.get(patchID).patchWaterStress == WaterStress.NORMAL) {
            if(state.vegTerrInfo.get(patchID).patchVegState == VegState.VERYHEALTHY) {
                state.vegTerrInfo.get(patchID).patchVegCondition -= 2;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.HEALTHY) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 1;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.STRESS) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 2;
            } else if (state.vegTerrInfo.get(patchID).patchVegState == VegState.SEVERELYSTRESSED) {
                state.vegTerrInfo.get(patchID).patchVegCondition += 2;
            }
        } else if (state.vegTerrInfo.get(patchID).patchWaterStress == WaterStress.BELOWNORMAL) {
            state.vegTerrInfo.get(patchID).patchVegCondition -= 3;
        } else { //patchWaterStress = SEVERE DROUGHT
            state.vegTerrInfo.get(patchID).patchVegCondition -= 5;
        }
    }


    /* =========================================================
       ARCHIVED — kept for reference, not called by active code
       ========================================================= */

//    public static List<Integer> rankPatchByQuality(ArrayList<Integer> potentialPatchList, Map<Integer, VegInfoIdentifier>
//            patchInfo) {
//        //sort PatchIDs by their patch quality values, from high to low
//        return potentialPatchList.stream().sorted(Comparator.comparingDouble(
//                        id -> patchInfo.get(id).patchQuality).reversed()).collect(Collectors.toList());
//    }
//
//    public static List<Integer> rankPatchByNumVireo(ArrayList<Integer> potentialPatchList, Map<Integer, VegInfoIdentifier> patchInfo) {
//        //sort patchIDs by the total number of Vireos in the patch, from high to low
//        return potentialPatchList.stream().sorted(Comparator.comparingDouble(
//                id -> patchInfo.get(id).territoryNumVireos).reversed()).collect(Collectors.toList());
//    }
//
//    public static List<Integer> rankPatchByNumMales(ArrayList<Integer> potentialPatchList, Map<Integer, VegInfoIdentifier> patchInfo) {
//        //sort patchIDs by the number of male vireo agents in the patch, from high to low
//        return potentialPatchList.stream().sorted(Comparator.comparingDouble(
//                id -> patchInfo.get(id).territoryNumMales).reversed()).collect(Collectors.toList());
//    }
//
//    public static List<Integer> rankPatchByNumFemales(ArrayList<Integer> potentialPatchList, Map<Integer, VegInfoIdentifier> patchInfo) {
//        //sort patchIDs by the number of female vireo agents in the patch, from high to low
//        return potentialPatchList.stream().sorted(Comparator.comparingDouble(
//                id -> patchInfo.get(id).territoryNumFemales).reversed()).collect(Collectors.toList());
//    }

}
