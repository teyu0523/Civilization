package civ.UI;

import civ.engine.GameEngine;
import civ.MapLocation;
import civ.*;
import civ.enums.*;
import civ.navigation.NavigationUtils;
import civ.navigation.Path;
import java.awt.image.BufferedImage;
import java.awt.*;
import civ.TerrainInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import civ.sprites.SpriteUtils;
import java.util.ArrayList;



public class MapView extends JPanel implements MouseListener, KeyListener, MouseMotionListener
{
    //DEBUG FLAG
    private static final boolean DEBUG = false;

    //An instance of the game engine for the current game
    protected GameEngine gameEngine;
    //the size of the tiles in pixels
    private int tileSize;
    //An image representing what the view currently looks like (this is
    //what we paint in)
    private BufferedImage currentView;
    //The Top left tile in the map view
    protected MapLocation CameraOffset;
    //The width of the view in mapTiles
    protected int viewTileWidth;
    //The height of the view in mapTiles
    protected int viewTileHeight;
    //The width of the map in tiles
    protected int mapWidth;
    //the height of the map in tiles
    protected int mapHeight;
    //Wrapping of main
    protected boolean mapWrap;
    //MiniMap
    private MiniMapView miniMap;
    // The current path of the selected unit (for displaying purposes only)
    private Path path;

    private JLabel infoText;
    
    public MapView(GameEngine engine, int width, int height, MiniMapView miniM)
    {
        gameEngine = engine;
        //Because java does not support multiple inheritance, I will work around
        //it to create an Observer-esk pattern between the MapView and miniMapView
        miniMap = miniM;
        this.tileSize = SpriteUtils.getInstance().getTileSize();
        
        viewTileWidth = (width-2)/tileSize;
        viewTileHeight = (height-2)/tileSize;
        this.mapWidth = gameEngine.getWorld().getMap().getMapWidth();
        this.mapHeight = gameEngine.getWorld().getMap().getMapHeight();
        this.mapWrap = gameEngine.getWorld().getMap().wraps();

        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.CameraOffset = new MapLocation(0,0);
        this.setLayout(null);

        this.currentView = new BufferedImage((viewTileWidth*tileSize)+2, (viewTileHeight*tileSize)+2, BufferedImage.TYPE_INT_ARGB);
        


        infoText = new JLabel();
        infoText.setSize(630, 20);
        infoText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        infoText.setBackground(Color.DARK_GRAY); //Set the background
        infoText.setOpaque(true); //Show the background
        infoText.setForeground(Color.LIGHT_GRAY);

        infoText.setBounds(0, 572, 632, 20);
        this.add(infoText);

        gameEngine.setMapView(this);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
        this.setFocusable(true);
    }

    //Override the painting of this component so we can paint our own view.
    @Override
    public void paintComponent(Graphics g)
    {
        g.drawImage(this.currentView, 0,0, currentView.getWidth(), currentView.getHeight(), this);
        this.miniMap.updateView(this);
    }

    
    public void paintMap()
    {
        Graphics g = this.currentView.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // Paint Terrain
        for(int x = 0; x < this.viewTileWidth; x++)
        {
            for(int y = 0; y < viewTileHeight; y++)
            {
                BufferedImage sprite = gameEngine.getTerrainSprite(new MapLocation((this.CameraOffset.x+x)%this.mapWidth,this.CameraOffset.y+y));
                g.drawImage(sprite, (x*tileSize)+1, (y*tileSize)+1, tileSize, tileSize, null);
            }
        }

        // Paint Cities
        for(int x = 0; x < viewTileWidth; x++)
        {
            for(int y = 0; y < viewTileHeight; y++)
            {
                BufferedImage sprite = gameEngine.getCitySprite(new MapLocation((this.CameraOffset.x+x)%this.mapWidth,this.CameraOffset.y+y));
                g.drawImage(sprite, (x*tileSize)+1, (y*tileSize)+1, tileSize, tileSize, null);
                //this if statement will make sure the selected unit on top of the city after clicking g will show           
            }
        }
        
        // Paint Units
        for(int x = 0; x < viewTileWidth; x++)
        {
            for(int y = 0; y < viewTileHeight; y++)
            {   
                MapLocation mapLocation = new MapLocation((this.CameraOffset.x+x)%this.mapWidth,this.CameraOffset.y+y);
                if (!gameEngine.isCity(mapLocation) || (gameEngine.getCurrentlySelectedUnitTile()!=null && gameEngine.getCurrentlySelectedUnitTile().compare(mapLocation))) {
                    BufferedImage sprite = gameEngine.getUnitSprite(mapLocation);
                    g.drawImage(sprite, (x*tileSize)+1, (y*tileSize)+1, tileSize, tileSize, null);        
                }                 
            }
        }
        
        // Paint Pathfinding
        for(int x = 0; x < mapWidth; x++)
        { 
            for(int y = 0; y < mapHeight; y++)
            {
                if(path != null && path.contains(x, y)) {

                    // TODO: make this change color based on the distance the target is away from the unit?
                    g.setColor(new Color(255, 0, 0, 50));
                    MapLocation init = getDrawOffset(new MapLocation(x, y));
                    g.fillRect((init.x) * tileSize, (init.y) * tileSize, tileSize, tileSize);
                }
            }
        }

        if(this.gameEngine.getCurrentlySelectedUnit() != null)
        {
            MapLocation init = getDrawOffset(gameEngine.getCurrentlySelectedUnitTile());
            g.drawImage(SpriteUtils.getInstance().getHighlight(), (init.x*tileSize)+1, (init.y*tileSize)+1, tileSize, tileSize, null);
        }

        this.repaint();
    }

    // This can be called as follows: if(input.isKeyDown(KeyEvent.VK_V)) --> Checks for v key being pressed
    
    // Handle all mouse and keyboard strokes.
    // There will be if statements here to check for right mouse button/WASD events. 
    private void moveCamera(MapLocation newCenter)
    {
        MapLocation cCenter = getCurrentViewCenter();
        
        MapLocation nOffset = new MapLocation((this.CameraOffset.x + (newCenter.x - cCenter.x)) % mapWidth, (this.CameraOffset.y + (newCenter.y - cCenter.y)) % mapHeight);
        if(nOffset.y < 0) nOffset.y = 0;
        if(nOffset.y > this.mapHeight - (this.viewTileHeight)) nOffset.y = this.mapHeight - (this.viewTileHeight);
        nOffset.x = (nOffset.x+this.mapWidth)%this.mapWidth;

        this.CameraOffset = nOffset;
        this.paintMap();
    }

    //Given an amount to move in the x and y directions, move the camera
    //by this amount. a postivie x moves the camera east and a positive y moves
    //the camera south.
    private void shiftCamera(int x, int y)
    {
        int ncx = getCurrentViewCenter().x + x;
        int ncy = getCurrentViewCenter().y + y;
        if(!this.gameEngine.getWorld().getMap().wraps())
        {
            int width = this.gameEngine.getWorld().getMap().getMapWidth();
            if(ncx < ((this.viewTileWidth/2) + (this.viewTileWidth%2 -1)))
                ncx = ((this.viewTileWidth/2) + (this.viewTileWidth%2 -1));
            else if(ncx > width - ((this.viewTileWidth/2) + (this.viewTileWidth%2 -1) + 1))
                ncx = width - ((this.viewTileWidth/2) + (this.viewTileWidth%2 -1) + 1);
        }
        this.moveCamera(new MapLocation(ncx, ncy));
    }

    /**
     * Used to center (or get as close to centering) the camera on a
     * mapLocation provided. The method will take into account wrapping
     * and the like.
     * @param l Desired center of map view
     */
    public void centerCameraOn(MapLocation l)
    {
        shiftCamera(l);
    }

    private void shiftCamera(MapLocation l)
    {
        MapLocation cCent = getCurrentViewCenter();
        shiftCamera(l.x-cCent.x, l.y-cCent.y);
    }

    public MapLocation getCurrentViewCenter()
    {
        return new MapLocation(
                this.CameraOffset.x + (this.viewTileWidth/2) + (this.viewTileWidth%2 -1),
                this.CameraOffset.y + (this.viewTileHeight/2) + (this.viewTileHeight%2 -1)
                );
    }

    //unitLocation is in the REFERENCE OF THE MAP, NOT THE VIEW!
    public void highlight(BufferedImage highlight, MapLocation unitLocation){
        Graphics g = this.currentView.createGraphics();
        unitLocation = getDrawOffset(unitLocation);
        
        if(gameEngine.getCurrentlySelectedUnit() != null || gameEngine.getCurrentlySelectedCity() != null) {
            g.drawImage(highlight, (unitLocation.x) * tileSize + 1, (unitLocation.y) * tileSize + 1, tileSize, tileSize, null);
        } else {
            drawTile(unitLocation);
        }
        this.repaint();
    }
    
    public void drawMovedUnit(MapLocation previousLocation, MapLocation newLocation) {       
        drawTile(previousLocation);
        drawTile(newLocation);
    }

    //mapLocation here is in REFERENCE TO THE MAP, NOT THE VIEW!
    public void drawTile(MapLocation mapLocation) {
        
        Graphics g = currentView.createGraphics();
        MapLocation drawMapLocation = new MapLocation(mapLocation.x, mapLocation.y);
        MapLocation mapViewLocation = getDrawOffset(mapLocation);
         
        // Is the tile within the visible bounds?
        if((mapLocation.x <= CameraOffset.x + viewTileWidth || mapLocation.x >= CameraOffset.x) ||
                (mapLocation.y <= CameraOffset.y + viewTileHeight || mapLocation.y >= CameraOffset.y)) 
        {        
            g.drawImage(gameEngine.getTerrainSprite(drawMapLocation), (mapViewLocation.x) * tileSize + 1, 
                    (mapViewLocation.y) * tileSize + 1, tileSize, tileSize, null);
            g.drawImage(gameEngine.getCitySprite(drawMapLocation), (mapViewLocation.x) * tileSize + 1, 
                    (mapViewLocation.y) * tileSize, tileSize, tileSize, null);
            if (!gameEngine.isCity(mapLocation)) {
                g.drawImage(gameEngine.getUnitSprite(drawMapLocation), (mapViewLocation.x) * tileSize + 1, 
                        (mapViewLocation.y) * tileSize + 1, tileSize, tileSize, null);
            }
            repaint();
        }
    }

    //Takes a map location that is in the reference of the View and
    //maps it to the Map reference that we can use to paint in the view.
    public MapLocation getDrawOffset(MapLocation mapLocation) {
        MapLocation tempLocation = new MapLocation(mapLocation);
        tempLocation.x = (mapLocation.x - CameraOffset.x + mapWidth) % mapWidth;
        tempLocation.y = (mapLocation.y - CameraOffset.y + mapHeight) % mapHeight;
        return tempLocation;
    }
    
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        MapLocation cLoc = getMapLocation(e);
        this.infoText.setText(getInfo(cLoc));
        if(DEBUG) this.infoText.setText(cLoc.toString());

        if(e.getButton() == MouseEvent.BUTTON1)
            gameEngine.handleMouseClick(e);
        else if(e.getButton() == MouseEvent.BUTTON3)
        {
            shiftCamera(cLoc);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
             
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //Are we panning a map :D?
        if (e.getKeyCode() == KeyEvent.VK_W)
            shiftCamera(0, -1);
        else if(e.getKeyCode() == KeyEvent.VK_S)
            shiftCamera(0, 1);
        else if(e.getKeyCode() == KeyEvent.VK_A)
            shiftCamera(-1, 0);
        else if(e.getKeyCode() == KeyEvent.VK_D)
            shiftCamera(1, 0);
        else
            gameEngine.handleKeyPress(e);
        gameEngine.addToInputArray(e.getKeyCode());

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.infoText.setText(getInfo(getMapLocation(e)));
        gameEngine.handleMouseMove(e);
    }

    //Given a mouse event that pertains to the MapView,
    //Find the mapLocation that the mouse is sitting on.
    //Note this is in the MAP Reference, not the views tile reference
    public MapLocation getMapLocation(MouseEvent e)
    {
        return new MapLocation(
                ((e.getX() / tileSize) + CameraOffset.x) % mapWidth,
                ((e.getY() / tileSize) + CameraOffset.y) % mapHeight);
    }
    
    public void setPath(Path p) {
        path = p;
    }

    public Object getPath() {
        return this.path;
    }

    public void drawUnit(Unit unit) {
        Graphics g = currentView.createGraphics();
        MapLocation drawMapLocation = new MapLocation(unit.getMapLocation().x, unit.getMapLocation().y);
        MapLocation mapViewLocation = getDrawOffset(unit.getMapLocation());   
        
        g.drawImage(gameEngine.getUnitSprite(unit.getUnitType()), (mapViewLocation.x) * tileSize + 1, 
                        (mapViewLocation.y) * tileSize + 1, tileSize, tileSize, null);
    }

    private String getInfo(MapLocation loc)
    {
        String result = "";
        City city = gameEngine.getWorld().getCity(loc);
        ArrayList<Unit> units = gameEngine.getWorld().getUnitList(loc);
        TerrainInfo ti = gameEngine.getTerrainInfo(loc);
        Tile t = gameEngine.getWorld().getTile(loc);
        if(city != null) //If there is a city on a tile.
        {
            //Example: Town - Lvl 1, 4 Units present, Producting Warriors
            result = city.getName() + ": Lvl " + city.getLevel()
                    + " / " + city.getUnits().size()
                    + " Units present / Producing: " + fc(city.getCurrentlyProducing().toString())
                    + " / Terrain: " + fc(t.getTerrainType().toString()) +
                    (t.getBonusType()!=TerrainBonusType.NONE?"/"+fc(t.getBonusType().toString()):"");
        }
        else if(units != null && !units.isEmpty()) //If there is a unit on a tile
        {
            if(units.size() > 1)
                result = units.size() + " units present";
            else
            {
                Unit u = units.get(0);
                result = fc(u.getUnitType().toString()) + ": " + u.getHealth() + " HP / " +
                        u.getBaseAttack() + " Att. / " +
                        u.getBaseDefense() + "x" + ti.getCombatModifier() + " Def. / " +
                        u.getFirepower() + " Pow. / "
                        + u.getMovementRange() + " Mov.";
            }
        }
        else  //Just looking at a tile.
        {
            result = fc(t.getTerrainType().toString()) +
                    (t.getBonusType()!=TerrainBonusType.NONE?"/"+fc(t.getBonusType().toString()):"")
                    + (t.riverPresent()==true?"/River":"")
                    + ": " + ti.getFoodResource() + " fd. / "
                    + ti.getProductionResource() + " prd. / "
                    + ti.getScienceResource() + " sci. / "
                    + ti.getMovementCost() + " mv. / "
                    + ti.getCombatModifier() + " defMulti";
        }
        return result;
    }

    //Fix caps for enums
    private String fc(String s)
    {
        return s.replace(s.substring(1), s.substring(1).toLowerCase());
    }
    
    public CivFrame getCivFrame() {
        return (CivFrame)this.getParent().getParent().getParent().getParent();
    }

    public void setInfoText(String s)
    {
        this.infoText.setText(s);
    }
}
