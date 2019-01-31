# FloodIt
Welcome to my FloodIt game project.

A simple FloodIt game designed with Model-View-Controller approach.

The game now has two modes: Plane and Torus, as well as two directions: Orthogonal and Diagonal. The player can select game modes 
and/or direction when he/she presses "Settings" button. A JOptionPane will pop up with all choices.

The player can now undo and redo his/her moves. The game saves itself automatically after pressing "Quit" button. The current
GameModel and all undo and redo states (including resets) will be saved. When the player wins the game, GameModel, undo and redo
states will be reset if the player presses "Play Again" button in the JOptionPane. The game quits if the player presses "Quit" button
in the JOptionPane.

Main method is in FloodIt.java

To run, please install latest java development toolkit, make sure correct variables are include in Path enviroment variables. Then use command prompt to run the program by entering the following command (make sure you are in the right directory)
```bash
javac FloodIt
java FloodIt.java
```
