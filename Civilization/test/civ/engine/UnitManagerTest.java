/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package civ.engine;

import civ.Map;
import civ.Tile;
import civ.Unit;
import civ.World;
import civ.enums.UnitType;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Scott
 */
public class UnitManagerTest {
    
    public UnitManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getUnit method, of class UnitManager.
     */
    @Test
    public void testloadUnits() {
        System.out.println("loadUnits");
        UnitManager instance = new UnitManager(new World(new Map(new Tile[5][5], false)));
        Unit unit = instance.createUnit(UnitType.valueOf("WARRIORS"));
        assertEquals(1, unit.getBaseDefense());
        assertEquals(1, unit.getBaseAttack());
        assertEquals(10, unit.getHealth());
        assertEquals(UnitType.WARRIORS, unit.getUnitType());
        assertEquals(3, unit.getMovementRange());        
    }
}
