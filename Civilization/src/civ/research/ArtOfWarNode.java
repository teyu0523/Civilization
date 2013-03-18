/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package civ.research;

import civ.enums.UnitType;
import java.util.ArrayList;


class ArtOfWarNode extends TreeNode {

    public ArtOfWarNode(ArrayList <UnitType> aU) {
        super(aU, "ArtOfWar");
        turnsToComplete = THIRDLEVELTURNS;
    }
    @Override
    public void researchComplete() {
        this.setResearched(true);
        availableUnits.add(UnitType.CANNON);
        availableUnits.add(UnitType.MUSKETEER);
        availableUnits.add(UnitType.RIFLEMAN);
        availableUnits.add(UnitType.DRAGOON);
        availableUnits.add(UnitType.ENGINEER);
        
    }

    public String getInfo(){
        return ("<html>Unlocks: CANNON, MUSKETEER, <br>"
                + "RIFLEMAN, DRAGOON, ENGINEER</html>");
    }

}
