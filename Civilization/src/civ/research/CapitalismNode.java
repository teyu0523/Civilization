/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package civ.research;

import civ.enums.UnitType;
import java.util.ArrayList;

/**
 * 100% increase in gold production
 * @author Justin
 */
class CapitalismNode extends TreeNode {

    public CapitalismNode(ArrayList <UnitType> aU) {
        super(aU, "Capitalism");
        turnsToComplete = FIFTHLEVELTURNS;
    }

    @Override
    public void researchComplete() {
        this.setResearched(true);
        availableUnits.add(UnitType.FANATIC);
        availableUnits.add(UnitType.SPY);
    }

    public String getInfo(){
        return ("Unlocks: FANATIC, SPY");
    }

}
