/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package civ.navigation;

/**
 *
 * @author Scott
 */
public interface AStarHeuristic {
    
    public float getCost(int x, int y, int moveToX, int moveToY, int mapWidth, int mapHeight);
    
}
