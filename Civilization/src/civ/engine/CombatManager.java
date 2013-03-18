/*
 * Handles combat between units when a collision is detected by the game engine
 */

package civ.engine;

import civ.City;
import civ.MapLocation;
import civ.Unit;
import civ.World;
import java.util.ArrayList;
import java.util.Random;

/*
 * Combat overview:
 * Most of this information can be found at http://www.civfanatics.com/civ2/strategy/combatguide/
 * 
 * Basics:
 * Combat is conducted between two units over a number of rounds. Each round,
 * either the Attacking Unit (hereby referred to as AU) or the Defending Unit 
 * (hereby referred to as DU) will deal damage equal to the unit's
 * firepower to the other unit during a round.
 * 
 * If no winner was found after one round, then a new round is started. The unit
 * that attacks is calculated and the either AU or DU will deal damage. This will
 * continue until either AU's or DU's hit points (hereby referred to as HP) reaches
 * 0. A battle is the set of rounds consisting of combat between two units until one
 * dies.
 * 
 * Unit's Variables:
 * Each Unit has variables used for combat, shown below:
 * 
 * a = BaseAttack. Used to measure the probability the AU will attack that round.
 *                  A unit with a = 0 cannot attack.
 * d = BaseDefense. Used to measure the probability the DU will attack that round.
 *                  A unit with d = 0 cannot defend, and therefore dies immediately if it enters combat.
 * fp = FirePower. The damage dealt to the opposing 's HP.
 * HP = Health or HitPoints. A unit with 0 HP is removed from the game and has lost the battle.
 * 
 * Bonuses
 * A unit's BaseDefense takes into account various factors to augment it. A unit
 * receives terrain bonuses to d depending on the terrain it is fighting on.
 * EXAMPLE:
 * A defending unit is on a mountain tile and gains a 3.0x bonus. In other words
 * the unit's d now equals 3.0d. A hill brings a 2.0x bonus, so a unit's d equals 2.0d.
 * 
 * Unit's also gain a 1.5x bonus to d for being fortified, which stacks with terrain. 
 * As such, a unit fortified on a mountain has a defense value of (1.5)(3.0)d.
 * 
 * Q: What about multiple units on one square?
 * A: If the AU attacks a stack of DUs, then the primary defender becomes the DU
 * with the highest d. If the primary defender's HP reaches 0, all DUs at that
 * location are destroyed.
 * The exception to this rule is if the DUs are stacked in a city. The primary defender is
 * still the DU with the highest d, but now if the primary defender dies, the battle
 * ends and the remaining DUs stacked on the city survive.
 * 
 * The Formula:
 * Now to the gist of it. To determine whether the AU or the DU attacks that round,
 * a random number is generated between 0 to ( (a | d) - 1) multiplied by some constant c, and 
 * the a or d is modified by the bonuses described above. The unit with the higher value wins
 * the round. The unit that attacks based on this formula deals its fp value to 
 * the opposing unit's HP. The probability of the AU attacking that round can be
 * roughly translated to:
 * 
 * If D > A: p = (A - 1)/(2D)
 * If A > D: p = 1 - ((D + 1)/(2A))
 * 
 * where A = (a * c) and D = (d * c)
 * 
 * Q: Air Units vs Water Units vs Land Units vs Special Units?
 * A: TO BE DETERMINED
 */

public class CombatManager { 
    
    private static final boolean DEBUG = false;
    
    private World world;
    private Random randomGenerator;    
    private static final int COMBATCONSTANT = 8;
    private TerrainManager terrainManager;
    
    
    public CombatManager(World world, TerrainManager terrainManager){
        this.world = world;
        this.terrainManager = terrainManager;
        randomGenerator = new Random();
    }
    
    public void collision(Unit attackingUnit, MapLocation enemyUnitLocation) {
        Unit defendingUnit = getPrimaryDefender(world.getUnitList(enemyUnitLocation));
        int auRandomNumber, duRandomNumber;
        float defensiveModifier;
        
        
        if (attackingUnit.getBaseAttack() == 0 && defendingUnit.getBaseDefense() == 0) {
            //TODO: What happens if a unit who can't attack hits a unit that can't defend? SCIENCE!
        }  else if (attackingUnit.getBaseAttack() == 0) {
            attackingUnit.setHealth(0);
        } else if (defendingUnit.getBaseDefense() == 0) {
            defendingUnit.setHealth(0);
        }
        
        //Each loop consists of one round, and all the rounds make up a battle to the death
        while (attackingUnit.getHealth() > 0 && defendingUnit.getHealth() > 0) {
            defensiveModifier = calculateModifier(defendingUnit);
            
            auRandomNumber = randomGenerator.nextInt((attackingUnit.getBaseAttack()) * COMBATCONSTANT);
            duRandomNumber = randomGenerator.nextInt((int)((defendingUnit.getBaseDefense()) * defensiveModifier
                    * COMBATCONSTANT));
            
            //Ties go to the defender
            if (auRandomNumber > duRandomNumber) {
                defendingUnit.wasAttacked(attackingUnit.getFirepower());
            } else {
                attackingUnit.wasAttacked(defendingUnit.getFirepower());
            }
            
            if (DEBUG) {
                int i = 0;
                System.out.println("Round " + i++);
                System.out.println("AU HEALTH: " + attackingUnit.getHealth() + " random#: " + auRandomNumber);
                System.out.println("DU HEALTH: " + defendingUnit.getHealth() + " random#: " + duRandomNumber);
            }
        }
        
        if (attackingUnit.getHealth() <= 0) {
//           removeUnit(attackingUnit);
            attackingUnit.getOwner().addToRemoveQueue(attackingUnit);
           attackingUnit = null;
        } else {
            removeDefendingUnits(defendingUnit);
        }
    } 
    
    /**
     * The primary defender from the defensive units. The primary defender is the
     * defender with the highest baseDefense, and is chosen as the hero to represent
     * the defending army.
     * @param defendingUnitList The list of defending units from which the primary defender
     *                          is chosen.
     * @return A Unit object that references the primary defender
     */
    private Unit getPrimaryDefender(ArrayList<Unit> defendingUnitList) {
        Unit primaryDefender = defendingUnitList.get(0);
        for (Unit unit : defendingUnitList) {
            if (unit.getBaseDefense() > primaryDefender.getBaseDefense()) {
                primaryDefender = unit;
            }
        }
        return primaryDefender;
    }
    
    /**
     * Calculated the bonuses a defending unit acquires based on terrain and if
     * they are fortified
     * @param defendingUnit The defendingUnit that is getting the modifier bonuses
     * @return 
     */
    private float calculateModifier(Unit defendingUnit) {
        return terrainManager.getTerrainInfo(defendingUnit.getMapLocation()).getCombatModifier();
    }

    /**
     * Removes a unit from the game. Removes it from the world, the owner, and
     * the city if possible.
     * @param unit The unit to be removed from the game.
     */
    private void removeUnit(Unit unit) {
        world.removeUnit(unit);
        unit.getOwner().removeUnit(unit);
        City city = world.getCity(unit.getMapLocation());
        if (city != null) {
            city.removeUnit(unit);
        }
    }
    
    /**
     * Removes the defending units on the tile if they were not on a city, and
     * only the defending unit that fought if they were on a city
     * @param defendingUnit The primary defending unit that lost the battle
     */
    private void removeDefendingUnits(Unit defendingUnit) {
        
        //Unit was destroyed on a city, only that one is destroyed
        if (world.getCity(defendingUnit.getMapLocation()) != null) {
            removeUnit(defendingUnit);
            return;
        }
        
        while(!world.getUnitList(defendingUnit.getMapLocation()).isEmpty()) {
            removeUnit(world.getUnitList(defendingUnit.getMapLocation()).get(0));
        }
    }
}
