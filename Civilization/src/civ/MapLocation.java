/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package civ;

import java.io.Serializable;


/**
 *
 * @author Scott
 */
public class MapLocation implements Serializable
{
    public int x;
    public int y;
    

    public MapLocation(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public MapLocation (MapLocation mapLocation) {
        this.x = mapLocation.x;
        this.y = mapLocation.y;
    }
    
    public String toString() {
        return this.x + ":" + this.y;
    }
    
    //Returns true if equal, false otherwise
    public boolean compare(MapLocation mapLocation) {
        if (this.x == mapLocation.x && this.y == mapLocation.y) {
            return true;
        }
        return false;
    }
}
