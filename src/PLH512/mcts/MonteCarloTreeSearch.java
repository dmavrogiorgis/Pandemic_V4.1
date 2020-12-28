package PLH512.mcts;
import PLH512.client.*;
import java.util.*;

public class MonteCarloTreeSearch {
    private static final int N = 4;
    private MCTSNode root;

    public MonteCarloTreeSearch(MCTSNode root) {
        this.root = root;
    }

    public ArrayList<MCTSNode> BestAction(){
        MCTSNode promisingNode;
        MCTSNode nodeToEplore;
        MCTSNode tempNode;
        MCTSNode bestNode= this.getRoot();
        ArrayList<MCTSNode> bestNodes = new ArrayList<MCTSNode>();
        double bestScore = Double.MAX_VALUE;

        Random rand = new Random();
        int i = N;
        while(i!=0){
            promisingNode = selectNode(this.getRoot());
            if(!promisingNode.getState().getBoard().checkIfWon()){
                expandNode(promisingNode);
            }
            nodeToEplore = promisingNode;
            if(promisingNode.sizeOfChildrenList() > 0){
                int randi = rand.nextInt(promisingNode.sizeOfChildrenList()-1);
                nodeToEplore = promisingNode.getChildrenNodes().get(randi);
                simulate(nodeToEplore);
            }
            backPropagate(nodeToEplore);
            i--;
        }

        for(int j=0; j<bestNode.getChildrenNodes().size(); j++){
            tempNode = bestNode.getChildrenNodes().get(j);
            if(tempNode.getTotalWins() < bestScore){  //TODO
                bestNode = tempNode;
                bestScore = tempNode.getTotalWins();
            }
        }
        bestNodes.add(bestNode);

        for(int j=0; j<3; j++){
            double prevBestScore=bestScore;
            for(int k=0; k<bestNode.sizeOfChildrenList(); k++){
                
                tempNode = bestNode.getChildrenNodes().get(k);
                if(tempNode.getTotalWins() < bestScore){
                    bestNode = tempNode;
                    bestScore = tempNode.getTotalWins();
                }   
            }
            if(prevBestScore==bestScore)
                break;
            bestNodes.add(bestNode);
        }
        return bestNodes;
    }

    public MCTSNode selectNode(MCTSNode rootNode){
        MCTSNode curNode = rootNode;
        ArrayList<MCTSNode> curChildren = curNode.getChildrenNodes();

        double bestUCT;
        double curUCT;

        while (!curNode.isLeaf()){
            bestUCT = -Double.MAX_VALUE;
            
            for(int i=0; i<curChildren.size(); i++){
                curUCT = curChildren.get(i).UCTValue();
                if(curUCT > bestUCT){
                    bestUCT = curUCT;
                    curNode = curChildren.get(i);
                }
            }
            curChildren = curNode.getChildrenNodes();
        }
        System.out.println("Hand:  ");
        curNode.getState().getBoard().printHandOf(curNode.getState().getPlayerID());
        return curNode;
    }

    public void expandNode(MCTSNode leafNode){
        ArrayList<State> possibleStates = Client.getMoves(leafNode.getState().getPlayerID(), leafNode.getState().getBoard());
        State state;
        MCTSNode newNode;

        for(int i=0; i< possibleStates.size(); i++ ){
            state = possibleStates.get(i);       
            newNode = new MCTSNode(state, 0, 0, leafNode);
            leafNode.getChildrenNodes().add(newNode);
        } 
    }
    
    public void simulate(MCTSNode node) {
            int RandomMoves=10;
            while(RandomMoves>0){

                RandomMoves--;
            }

    }

    public void backPropagate(MCTSNode node) {
        MCTSNode tempNode = node;
        double score = tempNode.getState().getEvaluation();
        
        while (tempNode != null) {
            tempNode.setTotaSims(tempNode.getTotalSims()+1);
            tempNode.setTotalWins(tempNode.getTotalWins() + score);
            tempNode = tempNode.getParentNode();
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
