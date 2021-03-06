/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package civ.research;

import civ.enums.UnitType;
import java.util.ArrayList;


class SeafaringNode extends TreeNode {

    public SeafaringNode(ArrayList <UnitType> aU) {
        super(aU, "Seafaring");
        turnsToComplete = FIRSTLEVELTURNS;
    }

    @Override
    public void researchComplete() {
        this.setResearched(true);
        availableUnits.add(UnitType.CARAVEL);
        availableUnits.add(UnitType.TRIREME);
        availableUnits.add(UnitType.FRIGATE);
        availableUnits.add(UnitType.GALLEON);
    }

    public String getInfo(){
        return ("<html>Unlocks: CARAVEL, TRIREME, <br>"
                + "FRIGATE, GALLEON</html>");
    }

}
