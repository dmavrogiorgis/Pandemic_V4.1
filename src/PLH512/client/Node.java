package PLH512.client;
import PLH512.server.*;

public class Node {
    private double evaluation;
    private Board board;

    public Node(double evaluation, Board board){
        this.evaluation = evaluation;
        this.board = board;
    }

    public Board getBoard(){
        return board;
    }
    
    public void setBoard(Board br){
        this.board = br;
    }

    public double getEvaluation(){
        return evaluation;
    }
    public void setEvaluation(double eva){
        this.evaluation = eva;
    }
}
