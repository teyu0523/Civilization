/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package civ.research;

import civ.enums.UnitType;
import java.util.ArrayList;


class IronWorksNode extends TreeNode {

    public IronWorksNode(ArrayList <UnitType> aU) {
        super(aU, "IronWorks");
        turnsToComplete = SECONDLEVELTURNS;
    }

    @Override
    public void researchComplete() {
        this.setResearched(true);
        availableUnits.add(UnitType.CATAPULT);
        availableUnits.add(UnitType.LEGION);
        availableUnits.add(UnitType.PHALANX);
        availableUnits.add(UnitType.PIKEMAN);
        availableUnits.add(UnitType.CHARIOT);
        availableUnits.add(UnitType.IRONCLAD);
    }

    public String getInfo(){
        return ("<html>Unlocks: CATAPULT, LEGION, PHALANX, <br>"
                + "PIKEMAN CHARIOT, IRONCLAD</html>");
    }

}
