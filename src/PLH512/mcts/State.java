package PLH512.mcts;
import PLH512.server.*;

public class State {
    private Board board;
    private int playerID;
    private String action;
    private double evaluation;

    public State(Board board, int playerID, String action, double evaluation){
        this.board = board;
        this.playerID = playerID;
        this.action = action;
        this.evaluation = evaluation;
    }

    public Board getBoard(){
        return board;
    }
    
    public void setBoard(Board board){
        this.board = board;
    }

    public int getPlayerID(){
        return playerID;
    }

    public void setPlayerID(int playerID){
        this.playerID = playerID;
    }

    public String getAction(){
        return action;
    }

    public void setAction(String action){
        this.action = action;
    }

    public double getEvaluation(){
        return evaluation;
    }

    public void setEvaluation(double evaluation){
        this.evaluation = evaluation;
    }
}
