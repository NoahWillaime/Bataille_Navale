package rmi.client;

import controleur.PositionClientController;
import controleur.PositionController;
import modele.GameManager;
import modele.bateaux.Case;
import rmi.client.Modele;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observer;

public class VueSelectionClient extends JPanel implements Observer {
    private Modele modele;
    protected JButton boardJoueur[][] = new JButton[10][10];
    private JPanel board;
    private JPanel options;
    private JButton bateaux[] = new JButton[5];
    private JButton valider;

    public VueSelectionClient(Modele modele){
        super();
        modele.addObserver(this);
        this.modele = modele;
        this.board = new JPanel();
        this.options = new JPanel();
        this.valider = new JButton("Valider la position");
        setAffichage();
    }

    public void setAffichage(){
        JFrame f = new JFrame();
        f.setTitle("Bataille Navale");
        f.setPreferredSize(new Dimension(600,600));
        this.setLayout(new BorderLayout());
        board.setLayout(new GridLayout(11, 11));
        options.setLayout(new GridLayout(7, 1));
        for (int i = 0; i < 11; i++){
            JButton caseindexJ = new JButton(i+"");
            caseindexJ.setEnabled(false);
            board.add(caseindexJ);
        }


        for (int i = 0; i < boardJoueur.length; i++){
            JButton caseindexJ = new JButton((i+1)+"");
            caseindexJ.setEnabled(false);
            board.add(caseindexJ);
            for (int j = 0; j < boardJoueur.length; j++){
                boardJoueur[i][j] = new JButton();
                boardJoueur[i][j].addMouseListener(new PositionClientController(this.modele, j, i));
                boardJoueur[i][j].setBackground(Color.CYAN);
                board.add(boardJoueur[i][j]);
            }
        }

        bateaux[0] = new JButton("Bateau 2");
        bateaux[1] = new JButton("Bateau 3(1)");
        bateaux[2] = new JButton("Bateau 3(2)");
        bateaux[3] = new JButton("Bateau 4");
        bateaux[4] = new JButton("Bateau 5");
        bateaux[0].addActionListener(e -> {
            modele.setTaille(GameManager.BATEAUDEUX);
        });
        bateaux[1].addActionListener(e -> {
            modele.setTaille(GameManager.BATEAUTROIS1);
        });
        bateaux[2].addActionListener(e -> {
            modele.setTaille(GameManager.BATEAUTROIS2);
        });
        bateaux[3].addActionListener(e -> {
            modele.setTaille(GameManager.BATEAUQUATRE);
        });
        bateaux[4].addActionListener(e -> {
            modele.setTaille(GameManager.BATEAUCINQ);
        });
        options.add(bateaux[0]);
        options.add(bateaux[1]);
        options.add(bateaux[2]);
        options.add(bateaux[3]);
        options.add(bateaux[4]);
        options.add(new JLabel("Clic droit : rotation."));

     /*   this.valider.addActionListener(e -> {
            this.modele.confirmerSelection();
        });*/
        options.add(this.valider);

        this.add(board, BorderLayout.CENTER);
        this.add(options, BorderLayout.EAST);

        f.add(this);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        for (int i = 0; i < boardJoueur.length; i++){
            for (int j = 0; j < boardJoueur.length; j++){
                boardJoueur[i][j].setBackground(Color.CYAN);
            }
        }
        if (modele.getTaille() >= 2) {
            for (int i = 0; i < boardJoueur.length; i++) {
                for (int j = 0; j < boardJoueur[i].length; j++) {
                    if (!boardJoueur[i][j].getBackground().equals(Color.GREEN))
                        boardJoueur[i][j].setBackground(Color.CYAN);
                }
            }
            int selection[][] = modele.getSelectionBateau();
            for (int i = 0; i < selection.length; i++){
                for (int j = 0; j < selection[i].length; j++){
                    if (selection[i][j] == 2)
                        boardJoueur[i][j].getBackground().equals(Color.GREEN);
                    else if (selection[i][j] == 3)
                        boardJoueur[i][j].setBackground(Color.RED);
                    else if (selection[i][j] == 1)
                        boardJoueur[i][j].setBackground(Color.BLUE);
                    }
                }
            }
        }
    }