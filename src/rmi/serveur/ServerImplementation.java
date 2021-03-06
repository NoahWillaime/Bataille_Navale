package rmi.serveur;

import modele.GameManager;
import modele.bateaux.Case;
import rmi.client.CaseClient;
import rmi.client.ClientInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * The type Server implementation.
 */
public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
    private GameManager gameManager;
    private int playerConnected;
    private ClientInterface client1;
    private ClientInterface client2;
    private int rdyToPlay;

    /**
     * Instantiates a new Server implementation.
     *
     * @param gm the gm
     * @throws RemoteException the remote exception
     */
    protected ServerImplementation(GameManager gm) throws RemoteException {
        this.gameManager = gm;
        this.playerConnected = 0;
        this.rdyToPlay = 0;
    }

    /**
     * Reset server.
     */
    public void resetServer(){
        this.gameManager.resetGame();
        this.playerConnected = 0;
        this.rdyToPlay = 0;
        client1 = null;
        client2 = null;
    }

    public boolean askConnect() throws RemoteException{
        if (playerConnected < 2){
            playerConnected++;
            return true;
        }
        return false;
    }

    public void connexion(ClientInterface clientInterface) throws RemoteException{
        if (playerConnected == 1) {
            client1 = clientInterface;
        }
        else {
            client2 = clientInterface;
            client2.notifyConnected();
            client1.notifyConnected();
        }
    }

    public int getPlayerConnected() throws RemoteException{
        return playerConnected;
    }

    @Override
    public void validerEpoque() throws RemoteException {
        client1.notifySelection();
        client2.notifySelection();
    }

    @Override
    public void setFactory(int facto) throws RemoteException {
        gameManager.setFactory(facto);
    }

    @Override
    public int[][] setSelection(int x, int y, int taille, int player) throws RemoteException {
        if (player == 1)
            return setSelection(x, y, taille);
        else
            return setSelectionJ2(x, y, taille);
    }

    /**
     * Set plateau int [ ] [ ].
     *
     * @param player  the player
     * @param plateau the plateau
     * @return the int [ ] [ ]
     */
    public int[][] setPlateau(int player, int[][] plateau){
        ArrayList<Case> bateaux = new ArrayList<>();
        Case[] cases = new Case[0];
        if (player == 1) {
            bateaux = this.gameManager.getCaseValider();
            cases = this.gameManager.getSelectionBateau();
        } else {
            bateaux = this.gameManager.getCaseValiderJ2();
            cases = this.gameManager.getSelectionBateauJ2();
        }
        for (Case c : bateaux){
            if (c.getX() >= 0 && c.getY() >= 0){
                plateau[c.getY()][c.getX()] = 2;
            }
        }
        for (Case c : cases){
            int casex=c.getX();
            int casey=c.getY();
            if (casex >= 0 && casey >= 0) {
                if (plateau[casey][casex] == 2)
                    plateau[casey][casex] = 3;
                else
                    plateau[casey][casex] = 1;
            }
        }
        return plateau;
    }

    /**
     * Init plateau int [ ] [ ].
     *
     * @return the int [ ] [ ]
     */
    public int[][] initPlateau(){
        int[][] plateau = new int[10][10];
        for (int i = 0; i < plateau.length; i++){
            for (int j = 0; j < plateau[i].length; j++){
                plateau[i][j] = 0;
            }
        }
        return plateau;
    }

    /**
     * Set selection int [ ] [ ].
     *
     * @param x      the x
     * @param y      the y
     * @param taille the taille
     * @return the int [ ] [ ]
     */
    public int[][] setSelection(int x, int y, int taille) {
        int[][] plateau = initPlateau();
        this.gameManager.setTaille(taille);
        this.gameManager.setSelection(x, y);
        return setPlateau(1, plateau);
    }

    /**
     * Set selection j 2 int [ ] [ ].
     *
     * @param x      the x
     * @param y      the y
     * @param taille the taille
     * @return the int [ ] [ ]
     * @throws RemoteException the remote exception
     */
    public int[][] setSelectionJ2(int x, int y, int taille) throws RemoteException {
        int[][] plateau = initPlateau();
        this.gameManager.setTailleJ2(taille);
        this.gameManager.setSelectionJ2(x, y);
        return setPlateau(2, plateau);
    }

    @Override
    public boolean isValide(int player){
        if (player == 1) {
            this.gameManager.validerSelection();
            return gameManager.getTaille() == -1;
        } else {
            this.gameManager.validerSelectionJ2();
            return gameManager.getTailleJ2() == -1;
        }
    }

    @Override
    public int[][] validerSelection(int player) throws RemoteException {
        if (player == 1)
            return validerSelection();
        else
            return validerSelectionJ2();
    }

    /**
     * Valider selection int [ ] [ ].
     *
     * @return the int [ ] [ ]
     */
    public int[][] validerSelection() {
        int[][] plateau = initPlateau();
        ArrayList<Case> bateaux = this.gameManager.getCasesBateauxH();
        for (Case c : bateaux) {
            if (c.getX() >= 0 && c.getY() >= 0) {
                plateau[c.getY()][c.getX()] = 2;
            }
        }
        return plateau;
    }

    /**
     * Valider selection j 2 int [ ] [ ].
     *
     * @return the int [ ] [ ]
     * @throws RemoteException the remote exception
     */
    public int[][] validerSelectionJ2() throws RemoteException {
        int[][] plateau = initPlateau();
        ArrayList<Case> bateaux = this.gameManager.getCasesBateauxJ2();
        for (Case c : bateaux){
            if (c.getX() >= 0 && c.getY() >= 0){
                plateau[c.getY()][c.getX()] = 2;
            }
        }
        return plateau;
    }

    @Override
    public void tirer(int x, int y, int player) throws RemoteException{
        if (player == 1 && gameManager.getTurn() == 1)
            this.gameManager.tirer(x, y);
        else if (player == 2 && gameManager.getTurn() == 2)
            this.gameManager.tirerJ2(x, y);
        client1.notifyShot();
        client2.notifyShot();
        if (gameManager.isJ1Winner()){
            client1.notifyVictoryJ1();
            client2.notifyVictoryJ1();
        } else if (gameManager.isJ2Winner()){
            client1.notifyVictoryJ2();
            client2.notifyVictoryJ2();
        }
    }

    @Override
    public int[][] getPlateauJ1() throws RemoteException {
        int[][] plateau =  new int[10][10];
        for (int i = 0; i < plateau.length; i++){
            for (int j = 0; j < plateau[i].length; j++)
                plateau[i][j] = 0;
        }
        Case caseVise = gameManager.getCaseViseeJ1();
        Case[] caseColat = gameManager.getCaseColatJ1();
        if (caseVise.getX() >= 0 && caseVise.getY() >= 0) {
            if (caseVise.getToucher())
                plateau[caseVise.getY()][caseVise.getX()] = 1; //ROUGE
            else
                plateau[caseVise.getY()][caseVise.getX()] = 2; //NOIR
        }
        for (Case c : caseColat){
            if (c.getX() >= 0 && c.getY() >= 0){
                if (c.getToucher())
                    plateau[c.getY()][c.getX()] = 1;
                else
                    plateau[c.getY()][c.getX()] = 2;
            }
        }
        return plateau;
    }

    @Override
    public int[][] getPlateauJ2() throws RemoteException {
        int[][] plateau =  new int[10][10];
        for (int i = 0; i < plateau.length; i++){
            for (int j = 0; j < plateau[i].length; j++){
                plateau[i][j] = 0;
            }
        }
        Case caseVise = gameManager.getCaseViseeJ2();
        Case[] caseColat = gameManager.getCaseColatJ2();
        if (caseVise.getX() >= 0 && caseVise.getY() >= 0) {
            if (caseVise.getToucher())
                plateau[caseVise.getY()][caseVise.getX()] = 1;
            else
                plateau[caseVise.getY()][caseVise.getX()] = 2;
        }
        for (Case c : caseColat){
            if (c.getX() >= 0 && c.getY() >= 0){
                if (c.getToucher())
                    plateau[c.getY()][c.getX()] = 1;
                else
                    plateau[c.getY()][c.getX()] = 2;
            }
        }
        return plateau;
    }

    @Override
    public int getVictory() throws RemoteException {
        return this.gameManager.getVictory();
    }

    @Override
    public void switchOrientation(int player) {
        if (player == 1)
            this.gameManager.switchOrientation();
        else
            this.gameManager.switchOrientationJ2();
    }

    @Override
    public boolean valider(int player) throws RemoteException {
        boolean valide = false;
        if (player == 1) {
            this.gameManager.confirmerSelection();
            if (this.gameManager.getLaunchGame()) {
                this.gameManager.resetLaunch();
                rdyToPlay++;
                valide = true;
            }
        } else {
            this.gameManager.confirmerSelectionJ2();
            if (this.gameManager.getLaunchGame()){
                this.gameManager.resetLaunch();
                rdyToPlay++;
                valide = true;
            }
        }
        if (rdyToPlay == 2){
            valide=false;
        }
        return valide;
    }

    @Override
    public void askLancerJeu() throws RemoteException {
        if (rdyToPlay == 2){
            client1.notifyJeu();
            client2.notifyJeu();
        }
    }

    @Override
    public ArrayList<CaseClient> getCasesJoueur(int player) throws RemoteException {
        if (player == 1){
            return getCasesJoueur();
        } else {
            return getCasesJoueur2();
        }
    }

    /**
     * Gets cases joueur.
     *
     * @return the cases joueur
     * @throws RemoteException the remote exception
     */
    public ArrayList<CaseClient> getCasesJoueur() throws RemoteException {
        ArrayList<Case> cases = this.gameManager.getCasesBateauxH();
        ArrayList<CaseClient> casesJ = new ArrayList<CaseClient>(cases.size());
        for (Case c : cases)
            casesJ.add(new CaseClient(c.getX(),c.getY()));
        return casesJ;
    }

    /**
     * Gets cases joueur 2.
     *
     * @return the cases joueur 2
     * @throws RemoteException the remote exception
     */
    public ArrayList<CaseClient> getCasesJoueur2() throws RemoteException {
        ArrayList<Case> cases = this.gameManager.getCasesBateauxJ2();
        ArrayList<CaseClient> casesJ2 = new ArrayList<CaseClient>(cases.size());
        for (Case c : cases)
            casesJ2.add(new CaseClient(c.getX(), c.getY()));
        return casesJ2;
    }

    @Override
    public int getTurn() throws RemoteException {
        return gameManager.getTurn();
    }

    @Override
    public boolean isAmmoGame(){
        return gameManager.isMunitionGame();
    }

    @Override
    public void setMun(int ammo, int ID) throws RemoteException {
        if (ID == 1)
            this.gameManager.setMunition(ammo);
        else
            this.gameManager.setMunitionJ2(ammo);
    }

    @Override
    public int getMun(int type,int ID) throws RemoteException {
        if (type == 2) {
            if (ID == 1)
                return gameManager.getXMunition();
            else if (ID == 2)
                return gameManager.getXMunitionJ2();
        } else if (type == 1) {
            if (ID == 1)
                return gameManager.getCrossMunition();
            else if (ID == 2)
                return gameManager.getCrossMunitionJ2();
        }
        return 0;
    }

    @Override
    public void endGame() throws RemoteException {
        this.resetServer();
    }

    @Override
    public void clientClose(int ID) throws RemoteException {
        if (ID == 1 && playerConnected >= 2){
            playerConnected--;
            client2.notifyOtherClose();
        } else if (ID == 2 && playerConnected >= 2){
            playerConnected--;
            client1.notifyOtherClose();
        }
        if (playerConnected < 2)
            this.resetServer();
    }

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(String[] args){
        try {
            ServerImplementation serverImplementation = new ServerImplementation(new GameManager());
            Registry registry = LocateRegistry.createRegistry(8081);
            registry.bind("bataille_navale", serverImplementation);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
