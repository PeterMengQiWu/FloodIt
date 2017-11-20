import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.LinkedList;
import java.io.*;

import javax.swing.*;


/**
 * The class <b>GameController</b> is the controller of the game. It has a method
 * <b>selectColor</b> which is called by the view when the player selects the next
 * color. It then computesthe next step of the game, and  updates model and view.
 *
 * @author Guy-Vincent Jourdan, University of Ottawa
 */


public class GameController implements ActionListener, Serializable {

    /**
     * Reference to the view of the board
     */
    private GameView gameView;
    /**
     * Reference to the model of the game
     */
    private GameModel gameModel;
    /**
     * Constant for plane mode
     */
    private static final int MODE_PLANE = 0;
    /**
     * Constant for torus mode
     */
    private static final int MODE_TORUS = 1;
    /**
     * Constant for orthogonal direction
     */
    private static final int DIRECTION_ORTHOGONAL = 0;
    /**
     * Constant for diagonal direction
     */
    private static final int DIRECTION_DIAGONAL = 1;
    /**
     * Reference to undo stack
     */
    private Stack<GameModel> undoState = new GenericLinkedStack<GameModel>();
    /**
     * Reference to redo stack
     */
    private Stack<GameModel> redoState = new GenericLinkedStack<GameModel>();
 
    /**
     * Constructor used for initializing the controller. It creates the game's view 
     * and the game's model instances
     * 
     * @param size
     *            the size of the board on which the game will be played
     */
    public GameController(int size) {
        String fileName = "savedGame.ser";
        try{
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(fileName));
            gameModel = (GameModel) is.readObject();
            undoState = (GenericLinkedStack<GameModel>) is.readObject();
            redoState = (GenericLinkedStack<GameModel>) is.readObject();
        }catch(FileNotFoundException e){
            System.out.println("File not found. Starting a new game");
            gameModel = new GameModel(size);
        }catch(IOException e){
            System.out.println("No object to be read. Starting a new game");
            gameModel = new GameModel(size);
        }catch(ClassNotFoundException e){
            System.out.println("Class is not GameModel. Starting a new game");
            gameModel = new GameModel(size);
        }
        gameView = new GameView(gameModel, this);
        //flood();
        gameView.update(gameModel, canUndo(), canRedo());
    }

    /**
     * resets the game
     */
    public void reset(){
        gameModel.reset();
        //flood();
        gameView.update(gameModel, canUndo(), canRedo());
    }

    /**
     * Callback used when the user clicks a button (reset, quit, settings, undo or redo)
     *
     * @param e
     *            the ActionEvent
     */

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (e.getSource() instanceof DotButton) {

            redoState.clear();
            DotButton button = (DotButton)e.getSource();
            if(gameModel.allDotsNotCaptured()){
                addStates();
                gameModel.capture(button.getRow(), button.getColumn());
                flood();
                //addStates();
                gameView.update(gameModel, canUndo(), canRedo());
            }
            else{
                selectColor(((DotButton)(e.getSource())).getColor());
            }
        } else if (e.getSource() instanceof JButton) {
            JButton clicked = (JButton)(e.getSource());

            if (clicked.getText().equals("Quit")) {
                String fileName = "savedGame.ser";
                try{
                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
                    os.writeObject(gameModel);
                    os.writeObject(undoState);
                    os.writeObject(redoState);
                    os.close();
                }catch(FileNotFoundException f){
                    System.out.println("File not found. Creating a new file");
                    f.printStackTrace();
                }catch(IOException f){
                    System.out.println("Implementation incorrect");
                    f.printStackTrace();
                }
                 System.exit(0);
                
             } else if (clicked.getText().equals("Reset")){
                reset();
            }else if(clicked.getText().equals("Undo")){
                undo();
            }else if(clicked.getText().equals("Redo")){
                redo();
             }else if(clicked.getText().equals("Settings")){
                JPanel planeOptions = new JPanel();
                planeOptions.setLayout(new GridLayout(6, 1));
                JLabel q1 = new JLabel("Play on plane or torus?");
                JLabel q2 = new JLabel("Diagonal moves?");
                JRadioButton plane = new JRadioButton("Plane");
                plane.addActionListener(this);

                JRadioButton torus = new JRadioButton("Torus");
                torus.addActionListener(this);

                if(gameModel.getMode() == MODE_PLANE){
                    plane.setSelected(true);
                }
                else if(gameModel.getMode() == MODE_TORUS){
                    torus.setSelected(true);
                }

                JRadioButton orthogonal = new JRadioButton("Orthogonal");
                orthogonal.addActionListener(this);

                JRadioButton diagonals = new JRadioButton("Diagonals");
                diagonals.addActionListener(this);

                if(gameModel.getDirection() == DIRECTION_ORTHOGONAL){
                    orthogonal.setSelected(true);
                }
                else if(gameModel.getDirection() == DIRECTION_DIAGONAL){
                    diagonals.setSelected(true);
                }

                ButtonGroup g1 = new ButtonGroup();
                ButtonGroup g2 = new ButtonGroup();
                g1.add(plane);
                g1.add(torus);
                g2.add(orthogonal);
                g2.add(diagonals);

                planeOptions.add(q1);
                planeOptions.add(plane);
                planeOptions.add(torus);
                planeOptions.add(q2);
                planeOptions.add(orthogonal);
                planeOptions.add(diagonals);

                Object[] options = {"OK"};

                JOptionPane.showOptionDialog(gameView, planeOptions, "Message", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
             }
        }
        else if(e.getSource() instanceof JRadioButton){
            if(command.equals("Plane")){
                gameModel.setMode(MODE_PLANE);
            }
            else if(command.equals("Torus")){
                gameModel.setMode(MODE_TORUS);
            }
            if(command.equals("Orthogonal")){
                gameModel.setDirection(DIRECTION_ORTHOGONAL);
            }
            else if(command.equals("Diagonals")){
                gameModel.setDirection(DIRECTION_DIAGONAL);
            }
        } 
    }

    /**
     * <b>selectColor</b> is the method called when the user selects a new color.
     * If that color is not the currently selected one, then it applies the laogic
     * of the game to capture possible locations. It then checks if the game
     * is finished, and if so, congratulates the player, showing the number of
     * moves, and gives to options: start a new game, or exit
     * @param color
     *            the newly selected color
     */
    public void selectColor(int color){
        if(color != gameModel.getCurrentSelectedColor()) {
            addStates();   
            gameModel.setCurrentSelectedColor(color);
            flood();
            gameModel.step();
           
            gameView.update(gameModel, canUndo(), canRedo());
            if(gameModel.isFinished()) {
                      Object[] options = {"Play Again",
                                "Quit"};
                        int n = JOptionPane.showOptionDialog(gameView,
                                "Congratulations, you won in " + gameModel.getNumberOfSteps() 
                                    +" steps!\n Would you like to play again?",
                                "Won",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                        if(n == 0){
                            reset();
                            undoState.clear();
                            redoState.clear();
                            gameView.update(gameModel, canUndo(), canRedo());
                        } else{
                            System.exit(0);
                        }   
                }            
            }        
    }

   /**
     * <b>flood</b> is the method that computes which new dots should be "captured" 
     * when a new color has been selected. The Model is updated accordingly
     */
     private void flood() {

        Stack<DotInfo> stack = new GenericLinkedStack<DotInfo>();
        for(int i =0; i < gameModel.getSize(); i++) {
           for(int j =0; j < gameModel.getSize(); j++) {
                if(gameModel.isCaptured(i,j)) {
                    stack.push(gameModel.get(i,j));
                }
           }
        }

        DotInfo dotInfo;
        while(!stack.isEmpty()){
            dotInfo = stack.pop();
            if(gameModel.getMode() == MODE_PLANE){ //the board is now plane (borders are limited)
                if((dotInfo.getX() > 0) && shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY())) {
                    gameModel.capture(dotInfo.getX()-1, dotInfo.getY());
                    stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()));
                }  
                if((dotInfo.getX() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY())) {
                    gameModel.capture(dotInfo.getX()+1, dotInfo.getY());
                    stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()));
                }
                if((dotInfo.getY() > 0) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()-1)) {
                    gameModel.capture(dotInfo.getX(), dotInfo.getY()-1);
                    stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()-1));
                }  
                if((dotInfo.getY() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()+1)) {
                    gameModel.capture(dotInfo.getX(), dotInfo.getY()+1);
                    stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()+1));
                }
                if(gameModel.getDirection() == DIRECTION_DIAGONAL){ //checks diagonals when radio button "Diagonals" is selected in settings
                    if((dotInfo.getX() > 0 && dotInfo.getY() > 0) && shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY()-1)) { //top left
                        gameModel.capture(dotInfo.getX()-1, dotInfo.getY()-1);
                        stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()-1));
                    }
                    if((dotInfo.getX() < gameModel.getSize()-1 && dotInfo.getY() > 0) && shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY()-1)) { //bottom left
                        gameModel.capture(dotInfo.getX()+1, dotInfo.getY()-1);
                        stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()-1));
                    }
                    if((dotInfo.getX() > 0 && dotInfo.getY() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY()+1)) { //top right
                        gameModel.capture(dotInfo.getX()-1, dotInfo.getY()+1);
                        stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()+1));
                    }
                    if((dotInfo.getX() < gameModel.getSize()-1 && dotInfo.getY() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY()+1)) { //bottom right
                        gameModel.capture(dotInfo.getX()+1, dotInfo.getY()+1);
                        stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()+1));
                    }
                }
            }
            else if(gameModel.getMode() == MODE_TORUS){ //the board is now torus (board is like a cylinder)
                if(dotInfo.getX() >= 0) {
                    if(dotInfo.getX() == 0){
                        if(shouldBeCaptured (gameModel.getSize()-1, dotInfo.getY())){
                            gameModel.capture(gameModel.getSize()-1, dotInfo.getY());
                            stack.push(gameModel.get(gameModel.getSize()-1, dotInfo.getY()));
                        }
                    }
                    else{
                        if(shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY())){
                            gameModel.capture(dotInfo.getX()-1, dotInfo.getY());
                            stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()));
                        }
                    }
                }  
                if(dotInfo.getX() < gameModel.getSize()) {
                    if(dotInfo.getX() == gameModel.getSize()-1){
                        if(shouldBeCaptured (0, dotInfo.getY())){
                            gameModel.capture(0, dotInfo.getY());
                            stack.push(gameModel.get(0, dotInfo.getY()));
                        }
                    }
                    else{
                        if(shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY())){
                            gameModel.capture(dotInfo.getX()+1, dotInfo.getY());
                            stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()));
                        }
                    }
                }
                if(dotInfo.getY() >= 0) {
                    if(dotInfo.getY() == 0){
                        if(shouldBeCaptured (dotInfo.getX(), gameModel.getSize()-1)){
                            gameModel.capture(dotInfo.getX(), gameModel.getSize()-1);
                            stack.push(gameModel.get(dotInfo.getX(), gameModel.getSize()-1));
                        }
                    }
                    else{
                        if(shouldBeCaptured (dotInfo.getX(), dotInfo.getY()-1)){
                            gameModel.capture(dotInfo.getX(), dotInfo.getY()-1);
                            stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()-1));
                        }
                    }
                }  
                if(dotInfo.getY() < gameModel.getSize()) {
                    if(dotInfo.getY() == gameModel.getSize()-1){
                        if(shouldBeCaptured (dotInfo.getX(), 0)){
                            gameModel.capture(dotInfo.getX(), 0);
                            stack.push(gameModel.get(dotInfo.getX(), 0));
                        }
                    }
                    else{
                        if(shouldBeCaptured (dotInfo.getX(), dotInfo.getY()+1)){
                            gameModel.capture(dotInfo.getX(), dotInfo.getY()+1);
                            stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()+1));
                        }
                    }
                }
                if(gameModel.getDirection() == DIRECTION_DIAGONAL){ //checks diagonals when radio button "Diagonals" is selected in settings
                    if(dotInfo.getX() >= 0 && dotInfo.getY() >= 0) { //top left
                        if(dotInfo.getX() == 0 && dotInfo.getY() == 0){
                            if(shouldBeCaptured (gameModel.getSize()-1, gameModel.getSize()-1)){
                                gameModel.capture(gameModel.getSize()-1, gameModel.getSize()-1);
                                stack.push(gameModel.get(gameModel.getSize()-1, gameModel.getSize()-1));
                            }
                        }
                        else if(dotInfo.getX() == 0){
                            if(shouldBeCaptured (dotInfo.getX() + (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1)){
                                gameModel.capture(dotInfo.getX() + (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1);
                                stack.push(gameModel.get(dotInfo.getX() + (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1));

                            }
                        }
                        else if(dotInfo.getY() == 0){
                            if(shouldBeCaptured (gameModel.getSize()-1, dotInfo.getY() + (gameModel.getSize()-1-dotInfo.getX()))){
                                gameModel.capture(gameModel.getSize()-1, dotInfo.getY() + (gameModel.getSize()-1-dotInfo.getX()));
                                stack.push(gameModel.get(gameModel.getSize()-1, dotInfo.getY() + (gameModel.getSize()-1-dotInfo.getX())));
                            }
                        }
                        else{
                            if(shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY()-1)){
                                gameModel.capture(dotInfo.getX()-1, dotInfo.getY()-1);
                                stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()-1));
                            }
                        }
                    }
                    if(dotInfo.getX() < gameModel.getSize() && dotInfo.getY() >= 0) { //bottom left
                        if(dotInfo.getX() == gameModel.getSize()-1 && dotInfo.getY() == 0){
                            if(shouldBeCaptured (0, gameModel.getSize()-1)){
                                gameModel.capture(0, gameModel.getSize()-1);
                                stack.push(gameModel.get(0, gameModel.getSize()-1));
                            }
                        }
                        else if(dotInfo.getX() == gameModel.getSize()-1){
                            if(shouldBeCaptured (dotInfo.getY(), gameModel.getSize()-1)){
                                gameModel.capture(dotInfo.getY(), gameModel.getSize()-1);
                                stack.push(gameModel.get(dotInfo.getY(), gameModel.getSize()-1));

                            }
                        }
                        else if(dotInfo.getY() == 0){
                            if(shouldBeCaptured (gameModel.getSize()-1, dotInfo.getX())){
                                gameModel.capture(gameModel.getSize()-1, dotInfo.getX());
                                stack.push(gameModel.get(gameModel.getSize()-1, dotInfo.getX()));
                            }
                        }
                        else{
                            if(shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY()-1)){
                                gameModel.capture(dotInfo.getX()+1, dotInfo.getY()-1);
                                stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()-1));
                            }
                        }
                    }
                    if(dotInfo.getX() >= 0 && dotInfo.getY() < gameModel.getSize()) { //top right
                        if(dotInfo.getX() == 0 && dotInfo.getY() == gameModel.getSize()-1){
                            if(shouldBeCaptured (gameModel.getSize()-1, 0)){
                                gameModel.capture(gameModel.getSize()-1, 0);
                                stack.push(gameModel.get(gameModel.getSize()-1, 0));
                            }
                        }
                        else if(dotInfo.getX() == 0){
                            if(shouldBeCaptured (dotInfo.getY(), gameModel.getSize()-1)){
                                gameModel.capture(dotInfo.getY(), gameModel.getSize()-1);
                                stack.push(gameModel.get(dotInfo.getY(), gameModel.getSize()-1));

                            }
                        }
                        else if(dotInfo.getY() == gameModel.getSize()-1){
                            if(shouldBeCaptured (gameModel.getSize()-1, dotInfo.getX())){
                                gameModel.capture(gameModel.getSize()-1, dotInfo.getX());
                                stack.push(gameModel.get(gameModel.getSize()-1, dotInfo.getX()));
                            }
                        }
                        else{
                            if(shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY()+1)){
                                gameModel.capture(dotInfo.getX()-1, dotInfo.getY()+1);
                                stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()+1));
                            }
                        }
                    }
                    if(dotInfo.getX() < gameModel.getSize() && dotInfo.getY() < gameModel.getSize()) { //top right
                        if(dotInfo.getX() == gameModel.getSize()-1 && dotInfo.getY() == gameModel.getSize()-1){
                            if(shouldBeCaptured (0, 0)){
                                gameModel.capture(0, 0);
                                stack.push(gameModel.get(0, 0));
                            }
                        }
                        else if(dotInfo.getX() == gameModel.getSize()-1){
                            if(shouldBeCaptured (dotInfo.getX() - (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1)){
                                gameModel.capture(dotInfo.getX() - (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1);
                                stack.push(gameModel.get(dotInfo.getX() - (gameModel.getSize()-1-dotInfo.getY()), gameModel.getSize()-1));

                            }
                        }
                        else if(dotInfo.getY() == gameModel.getSize()-1){
                            if(shouldBeCaptured (gameModel.getSize()-1, dotInfo.getY() - (gameModel.getSize()-1-dotInfo.getX()))){
                                gameModel.capture(gameModel.getSize()-1, dotInfo.getY() - (gameModel.getSize()-1-dotInfo.getX()));
                                stack.push(gameModel.get(gameModel.getSize()-1, dotInfo.getY() - (gameModel.getSize()-1-dotInfo.getX())));
                            }
                        }
                        else{
                            if(shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY()+1)){
                                gameModel.capture(dotInfo.getX()+1, dotInfo.getY()+1);
                                stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()+1));
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * <b>shouldBeCaptured</b> is a helper method that decides if the dot
     * located at position (i,j), which is next to a captured dot, should
     * itself be captured
     * @param i
     *            row of the dot
     * @param j
     *            column of the dot
     * @return true if the dot should be captured and false otherwise
     */
    
   private boolean shouldBeCaptured(int i, int j) {
        if(!gameModel.isCaptured(i, j) &&
           (gameModel.getColor(i,j) == gameModel.getCurrentSelectedColor())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * <b>addStates</b> is a helper method that pushes every GameModel into the undo stack
     */
    private void addStates(){
        try{
            undoState.push(gameModel.clone());
        }catch(CloneNotSupportedException e){
            System.out.println("Cannot be cloned");
        }catch(NullPointerException e){ // this should never happen as GameModel is always initialized
            System.out.println("GameModel is empty");
        }
    }

    /**
     * <b>canUndo</b> is a helper method that returns true if the user can undo and false otherwise.
     *
     * @return true if an undo is permitted and false otherwise
     */
    private boolean canUndo(){
        return !undoState.isEmpty();
    }

    /**
     * <b>undo</b> is a helper method that restores the game to its previous state when the user presses "Undo" button.
     */
    private void undo(){
        if(!canUndo()){
            throw new IllegalStateException("Cannot undo");
        }
        try{
            redoState.push(gameModel.clone());
            gameModel = undoState.pop();
        }catch(CloneNotSupportedException e){
            System.out.println("Cannot be cloned");
        }catch(EmptyStackException e){
            System.out.println("Empty Stack");
        }catch(NullPointerException e){// this should never happen as GameModel is always initialized
            System.out.println("GameModel is empty");
        }
        
        
        gameView.update(gameModel, canUndo(), canRedo());
    }

    /**
     * <b>canRedo</b> is a helper method that returns true if the user can redo and false otherwise.
     *
     * @return true if an redo is permitted and false otherwise
     */
    private boolean canRedo(){
        return !redoState.isEmpty();
    }

    /**
     * <b>redo</b> is a helper method that restores the game to its previous state after the game is undone when the user presses "Redo" button.
     */
    private void redo(){
        if(!canRedo()){
            throw new IllegalStateException("Cannot redo");
        }
        try{
            undoState.push(gameModel.clone());
            gameModel = redoState.pop();
        }catch(CloneNotSupportedException e){
            System.out.println("Cannot be cloned");
        }catch(EmptyStackException e){
            System.out.println("EmptyStack");
        }catch(NullPointerException e){// this should never happen as GameModel is always initialized
            System.out.println("GameModel is empty");
        }
        
        gameView.update(gameModel, canUndo(), canRedo());
    }

}
