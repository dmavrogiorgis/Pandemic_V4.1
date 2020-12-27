package PLH512.mcts;

import java.util.ArrayList;

public class MCTSNode {
    private State state;
    private int totalSims;  //total number of simulations
    private int totalWins;  //number of simulations that resulted in a win
    
    private MCTSNode parentNode;
    private ArrayList<MCTSNode> childrenNodes;

    public MCTSNode(State state, int totalSims, int totalWins, MCTSNode parentNode){
        this.parentNode = parentNode;
        this.state = state;
        this.totalSims = totalSims;
        this.totalWins = totalWins;
        this.childrenNodes = new ArrayList<MCTSNode>();
    }

    public double UCTValue() {
        if (this.totalSims == 0) {
            return Integer.MAX_VALUE;
        }
        return (this.getTotalWins()/this.getTotalSims()) 
                    + Math.sqrt(2)*Math.sqrt(Math.log(this.getParentNode().getTotalSims())/this.getTotalSims());
    }

    /* IF THE SIZE OF THE CHILDREN LIST IS 0 WE HAVE A LEAF NODE */
    public boolean isLeaf(){
        return (this.getChildrenNodes().size()==0); 
    }

    public boolean isRoot() {
        return (this.getParentNode() == null);
    }

    public int sizeOfChildrenList(){
        return this.getChildrenNodes().size();
    }
    
    /* GETTERS AND SETTERS */
    public State getState(){
        return state;
    }
    
    public void setState(State state){
        this.state = state;
    }

    public int getTotalSims(){
        return totalSims;
    }

    public void setTotaSims(int totalSims){
        this.totalSims = totalSims;
    }

    public int getTotalWins(){
        return totalWins;
    }
    
    public void setTotalWins(int totalWins){
        this.totalWins = totalWins;
    }
    
    public MCTSNode getParentNode(){
        return parentNode;
    }
    
    public void setParentNode(MCTSNode parentNode){
        this.parentNode = parentNode;
    }

    public ArrayList<MCTSNode> getChildrenNodes(){
        return childrenNodes;
    }
    
    public void setChildrenNodes(ArrayList<MCTSNode> childrenNodes){
        this.childrenNodes = childrenNodes;
    }
}
