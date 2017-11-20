import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * The class <b>GameView</b> provides the current view of the entire Game. It extends
 * <b>JFrame</b> and lays out an instance of  <b>BoardView</b> (the actual game) and 
 * two instances of JButton. The action listener for the buttons is the controller.
 *
 * @author Guy-Vincent Jourdan, University of Ottawa
 */

public class GameView extends JFrame {


    /**
     * The board is a two dimensionnal array of DotButtons instances
     */
    private DotButton[][] board;

 
    /**
     * Reference to the model of the game
     */
    private GameModel  gameModel;
    /**
     * Reference to the controller of the game
     */
    private GameController gameController;
    /**
     * Reference to the JLabel which stores the score of game
     */
    private JLabel scoreLabel;
    /**
     * Reference to JButton "Undo"
     */
    private JButton undo;
    /**
     * Reference to JButton "Redo"
     */
    private JButton redo;
    /**
     * Constructor used for initializing the Frame
     * 
     * @param model
     *            the model of the game (already initialized)
     * @param gameController
     *            the controller
     */

    public GameView(GameModel model, GameController gameController) {
        super("Flood it -- the ITI 1121 version");

        this.gameModel = model;
        this.gameController = gameController;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setBackground(Color.WHITE);

        JPanel selectPanel = new JPanel();
        selectPanel.setBackground(Color.WHITE);
        
        undo = new JButton("Undo");
        redo = new JButton("Redo");
        JButton settings = new JButton("Settings");

        undo.addActionListener(gameController);
        redo.addActionListener(gameController);
        settings.addActionListener(gameController);

        selectPanel.add(undo);
        selectPanel.add(redo);
        selectPanel.add(settings);

        add(selectPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridLayout(gameModel.getSize(), gameModel.getSize()));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        board = new DotButton[gameModel.getSize()][gameModel.getSize()];

        for (int row = 0; row < gameModel.getSize(); row++) {
            for (int column = 0; column < gameModel.getSize(); column++) {
                board[row][column] = new DotButton(row, column, gameModel.getColor(row,column), 
                    (gameModel.getSize() < 26 ? DotButton.MEDIUM_SIZE : DotButton.SMALL_SIZE));
                board[row][column].addActionListener(gameController);
                panel.add(board[row][column]);
            }
        }
    	add(panel, BorderLayout.CENTER);

        JButton buttonReset = new JButton("Reset");
        buttonReset.setFocusPainted(false);
        buttonReset.addActionListener(gameController);

        JButton buttonExit = new JButton("Quit");
        buttonExit.setFocusPainted(false);
        buttonExit.addActionListener(gameController);

        JPanel control = new JPanel();
        control.setBackground(Color.WHITE);
        scoreLabel = new JLabel();
        scoreLabel.setText("Select initial dot");
        control.add(scoreLabel);
        control.add(buttonReset);
        control.add(buttonExit);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2,1));
        
        southPanel.add(control);
        southPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        southPanel.setBackground(Color.WHITE);
        add(southPanel, BorderLayout.SOUTH);


    	pack();
    	//setResizable(false);
    	setVisible(true);

    }

    /**
     * update the status of the board's DotButton instances based on the current game model
     *
     * @param newGameModel
     *              the new GameModel after undoing/redoing
     * @param undoable
     *              the game can be undone if it is true and false otherwise
     * @param redoable
     *              the game can be redone if it is true and false otherwise
     */

    public void update(GameModel newGameModel, boolean undoable, boolean redoable){
        gameModel = newGameModel;
        for(int i = 0; i < gameModel.getSize(); i++){
            for(int j = 0; j < gameModel.getSize(); j++){
                board[i][j].setColor(gameModel.getColor(i,j));
            }
        }
        if(undoable){
            undo.setEnabled(true);
        }
        else{
            undo.setEnabled(false);
        }

        if(redoable){
            redo.setEnabled(true);
        }
        else{
            redo.setEnabled(false);
        }

        if(gameModel.allDotsNotCaptured()){
            scoreLabel.setText("Select initial dot");
            repaint();
        }
        else{
            scoreLabel.setText("Number of steps: " + gameModel.getNumberOfSteps());
            repaint();
        }
        
    }

}
