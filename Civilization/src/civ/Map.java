package civ;

import java.io.Serializable;

public class Map implements Serializable
{
    //Assuming that tiles are square, we need to set this so the mapView
    //can determine how large to paint tiles.
    private int tileSize;
    private int mapWidth;
    private int mapHeight;
    private Tile[][] tileSet;
    //An indicator as to whether or not the map wraps (east to west)
    private boolean wraps;
    private boolean[][] visited;

    
    public Map()
    {
        
    }

    public Map(Tile[][] tiles, boolean wrap)
    {
        this.mapWidth = tiles.length;
        this.mapHeight = mapWidth > 0?tiles[0].length:0;
        this.tileSet = tiles;
        visited = new boolean[mapWidth][mapHeight];
        this.wraps = wrap;
    }

    public boolean wraps()
    {
        return this.wraps;
    }

    public void setWraps(boolean w)
    {
       this.wraps = w;
    }

    public int getTileSize()
    {
        return tileSize;
    }

    public int getMapWidth()
    {
        return mapWidth;
    }

    public int getMapHeight()
    {
        return mapHeight;
    }

    public Tile tileAt(MapLocation location) throws IllegalArgumentException
    {
        return this.tileAt(location.x, location.y);
    }

    public Tile tileAt(int x, int y)
    {
        //We can wrap das map
        return this.tileSet[((x%mapWidth)+mapWidth)%mapWidth][((y%mapHeight)+mapHeight)%mapHeight];
    }
    
    public void pathFinderVisited(int x, int y) {
        visited[x][y] = true;
    }
    
    public boolean visited(int x, int y) {
        return visited[x][y];
    }
    
    public void clearVisited() {
        for(int i = 0; i < mapWidth; i++) {
            for(int j = 0; j < mapHeight; j++) {
                visited[i][j] = false;
            }
        }
    }
}
