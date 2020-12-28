package PLH512.mcts;

import PLH512.client.*;
import java.util.*;

public class MonteCarloTreeSearch {
	private static final int N = 5;
	private MCTSNode root;

	public MonteCarloTreeSearch(MCTSNode root) {
		this.root = root;
	}

	public MCTSNode BestAction() {
		MCTSNode promisingNode;
		MCTSNode nodeToEplore;
		MCTSNode tempNode;
		MCTSNode bestNode = this.getRoot();
		double bestScore = Double.MAX_VALUE;

		Random rand = new Random();
		int i = N;
		while (i != 0) {
			promisingNode = selectNode(this.getRoot());
			if (!promisingNode.getState().getBoard().checkIfWon()) {
				expandNode(promisingNode);
			}
			nodeToEplore = promisingNode;
			if (promisingNode.sizeOfChildrenList() > 0) {
				int randi = rand.nextInt(promisingNode.sizeOfChildrenList() - 1);
				nodeToEplore = promisingNode.getChildrenNodes().get(randi);
				simulate(nodeToEplore);
			}
			backPropagate(nodeToEplore);
			i--;
		}

		System.out.println(bestNode.sizeOfChildrenList() + "   " + bestNode.isLeaf());
		for (int j = 0; j < bestNode.getChildrenNodes().size(); j++) {
			tempNode = bestNode.getChildrenNodes().get(j);
			if (tempNode.getTotalWins() < bestScore && tempNode.getTotalWins() != 0) { // TODO
				bestNode = tempNode;
				bestScore = tempNode.getTotalWins();
			}
		}

		return bestNode;
	}

	public MCTSNode selectNode(MCTSNode rootNode) {
		MCTSNode curNode = rootNode;
		ArrayList<MCTSNode> curChildren = curNode.getChildrenNodes();

		double bestUCT;
		double curUCT;

		while (!curNode.isLeaf()) {
			bestUCT = -Double.MAX_VALUE;

			for (int i = 0; i < curNode.sizeOfChildrenList(); i++) {
				curUCT = curChildren.get(i).UCTValue();
				if (curUCT > bestUCT) {
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

	public void expandNode(MCTSNode leafNode) {
		ArrayList<State> possibleStates = Client.getMoves(leafNode.getState().getPlayerID(),
				leafNode.getState().getBoard());
		State state;
		MCTSNode newNode;

		for (int i = 0; i < possibleStates.size(); i++) {
			state = possibleStates.get(i);
			newNode = new MCTSNode(state, 0, 0, leafNode);
			leafNode.getChildrenNodes().add(newNode);
		}
	}

	public void simulate(MCTSNode node) {
		/*int RandomMoves = 10, randomIndex;
		ArrayList<State> possibleMoves = Client.getMoves(node.getState().getPlayerID(), node.getState().getBoard());
		Random rand = new Random();
		while (RandomMoves > 0) {
			randomIndex = rand.nextInt(1);
            

			RandomMoves--;
		}*/

	}

	public void backPropagate(MCTSNode node) {
		MCTSNode tempNode = node;
		double score = tempNode.getState().getEvaluation();

        node.setIsVisited(true);
		while (tempNode != null) {
			tempNode.setTotaSims(tempNode.getTotalSims() + 1);
			tempNode.setTotalWins(tempNode.getTotalWins() + score);
			tempNode = tempNode.getParentNode();
		}

	}

	/* GETTERS AND SETTERS */
	public MCTSNode getRoot() {
		return this.root;
	}

	public void setRoot(MCTSNode root) {
		this.root = root;
	}

}
