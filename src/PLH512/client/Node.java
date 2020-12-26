package PLH512.client;
import PLH512.server.*;

public class Node {
    private double evaluation;
    private Board board;
    private Node parentNode;

    public Node(double evaluation, Board board, Node parentNode){
        this.evaluation = evaluation;
        this.board = board;
        this.parentNode = parentNode;
    }

    /* GETTERS AND SETTERS */
    public Board getBoard(){
        return board;
    }
    
    public void setBoard(Board board){
        this.board = board;
    }

    public double getEvaluation(){
        return evaluation;
    }
    
    public void setEvaluation(double evaluation){
        this.evaluation = evaluation;
    }

    public Node getParentNode(){
        return parentNode;
    }
    
    public void setBoard(Node parentNode){
        this.parentNode = parentNode;
    }
}
