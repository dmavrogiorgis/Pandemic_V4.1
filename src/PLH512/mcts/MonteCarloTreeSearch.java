package PLH512.mcts;
import PLH512.client.*;
import java.util.*;

public class MonteCarloTreeSearch {
    private static final int N = 100;
    private MCTSNode root;

    public MonteCarloTreeSearch(MCTSNode root) {
        this.root = root;
    }

    public MCTSNode selectNode(MCTSNode rootNode){
        MCTSNode curNode = rootNode;
        ArrayList<MCTSNode> curChildren = curNode.getChildrenNodes();

        double bestUCT;
        double curUCT;

        while (!curNode.isLeaf()) {
            bestUCT = Integer.MIN_VALUE;
            
            for(int i=0; i<curChildren.size(); i++){
                curUCT = curChildren.get(i).UCTValue();
                if(curUCT > bestUCT){
                    bestUCT = curUCT;
                    curNode = curChildren.get(i);
                }
            }
        }
        return curNode;
    }

    public void expandNode(MCTSNode leafNode){
        ArrayList<State> moves = Client.getMoves(leafNode.getState().getPlayerID(), leafNode.getState().getBoard());
        for(int i=0; i< moves.size(); i++ ){
            //State state = new State(Client., Agent.Evaluation(), );
            
           // MCTSNode node = new MCTSNode(state, 0, 0, leafNode);
        } 

    }
    /* GETTERS AND SETTERS */
    public MCTSNode getRoot(){
        return this.root;
    }
    
    public void setRoot(MCTSNode root){
        this.root = root;
    }


}
