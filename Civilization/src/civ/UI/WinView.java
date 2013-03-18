package civ.UI;

import civ.Player;
import civ.engine.WindowUtils;
import civ.maps.MapInfo;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import sun.audio.*;

/*
 * Mark's, no touchy.
 */

public class WinView extends JFrame implements ActionListener{

    JButton newGame, loadGame, startGame, cancelGame;

    JFrame newFrame = new JFrame("New Window");
    JPanel BackPanel = new JPanel();
    JPanel BottomPanel = new JPanel();
    JPanel TopPanel = new JPanel();

    JLabel winningPlayer;

    AudioPlayer MGP = AudioPlayer.player;
    ContinuousAudioDataStream loop = null;

    private Container cp = getContentPane();

    public WinView(String winner){
        super("Winner " + winner);

        cp.setLayout(createLayout());
        cp.setBackground(Color.BLACK);

        GridBagConstraints c = new GridBagConstraints();

        TopPanel.setBackground(Color.DARK_GRAY);
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        cp.add(TopPanel, c);

        BackPanel.setBackground(Color.DARK_GRAY);
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        cp.add(BackPanel, c);

        BottomPanel.setBackground(Color.DARK_GRAY);
        c.gridx = 0;
        c.gridy = 2;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        cp.add(BottomPanel, c);

        try{
            BufferedImage myPicture = ImageIO.read(new File("resources/UI stuff/game-victory.jpg"));
            JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
            BackPanel.add(picLabel);
        } catch (IOException e){
//            e.printStackTrace();
        }

        newGame = new JButton("New Game");
        newGame.setBackground(Color.LIGHT_GRAY);
        newGame.setForeground(Color.BLACK);
        newGame.addActionListener(this);

        winningPlayer = new JLabel(winner + " wins!");
        winningPlayer.setForeground(Color.WHITE);
        winningPlayer.setFont(new Font("Arial", NORMAL, 30));
        TopPanel.add(winningPlayer);
        BottomPanel.add(newGame);

        AudioStream BGM;
        AudioData MD;

        try{
            InputStream test = new FileInputStream("resources/UI stuff/WinningSongSmaller.wav");
            BGM = new AudioStream(test);
            MD = BGM.getData();
            loop = new ContinuousAudioDataStream(MD);
        }catch(IOException e){
//            e.printStackTrace();
        }
        MGP.start(loop);
        WindowUtils.centerOnScreen(this, new Dimension(700,650));

        this.setSize(700, 650);
        this.setVisible(true);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newGame) {
            this.setVisible(false);
            this.dispose();
            MGP.stop(loop);
            SplashView civ = new SplashView();
        }
    }
    private GridBagLayout createLayout(){
        GridBagLayout GBL = new GridBagLayout();
        GBL.columnWidths = new int[] {650, 0};
        GBL.rowHeights = new int[] {50, 450, 50};
        return GBL;
    }

}