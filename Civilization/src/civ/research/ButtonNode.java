/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package civ.research;

import javax.swing.JButton;

/**
 *
 * @author Scott
 */
public class ButtonNode extends JButton {
    public TreeNode treeNode;
    
    ButtonNode(String s, TreeNode n, int x, int y, int w, int h) {
        super(s);
        treeNode = n;
        this.setBounds(x, y, w, h);
        if(n.isAvailable())
            this.setEnabled(true);
        else
            this.setEnabled(false);
    }
}
