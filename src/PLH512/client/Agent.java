package PLH512.client;

import PLH512.server.Board;
import PLH512.server.City;
import java.util.*;

public class Agent {
	// BOARD CLASS GETTER AND SETTERS -> numOfPlayers, cityList
	private int agentID;
	private String myAction;
	private String mySuggestions;
	private Board myBoard;

	public Agent(int agentID, Board br) {
		this.agentID = agentID;
		this.myAction = "";
		this.mySuggestions = "";
		this.myBoard = Client.copyBoard(br);
	}

	/* 1) NORMALIZATION OF DISTANCE TO ALL THE CITIES USING THE NUMBER OF CITY CUBES DIVIED BY TOTAL STATE CUBES*/
	public double heuristicSurvive(Board board) {
		double evaluateState = 0;

		for (int i = 0; i < board.getNumberOfPlayers(); i++) {
			evaluateState += distanceOfCities(board, i) / heuristicInfection(board);
		}

		return evaluateState;
	}

	/* 2) DISTANCE TO THE CLOSEST CITY WITH RS IN IT */
	public double heuristicCure(Board board) {
		double evaluateState = 0;

		for (int i = 0; i < board.getNumberOfPlayers(); i++) {
			evaluateState += CloserCityWithRS(BuildPlayerDistanceMap(board, i));
		}
		return evaluateState;
	}

	/* 3) MIN NUMBER OF CARDS MISSING TO DISCOVER A CURE FOR EACH DISEASE COLOR AMONG PLAYERS' HANDS */
	public int heuristicCards(Board board) {
		String[] colors = board.getAllColors();
		int minColorCounter = Integer.MAX_VALUE;
		int totalValue = 0;
		int ColorCounter = 0;
		int actives = 0;
		int R = 0;

		actives = heuristicCures(board);
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < board.getNumberOfPlayers(); j++) {
				if (board.getRoleOf(j).equals("Scientist")) {
					R = 4;
				} else {
					R = 5;
				}
				ColorCounter = R - Client.cardsCounterOfColor(board, j, colors[i]);
				if (minColorCounter > ColorCounter)
					minColorCounter = ColorCounter;
			}
			totalValue += minColorCounter * actives;
		}
		return totalValue;
	}

	/* 4) NUMBER OF DISCARDED CARDS FOR EACH OF THE ACTIVE DISEASES STILL MISSING A CURE */
	public double heuristicDiscard(Board board) {
		String[] colors = board.getAllColors();
		int totalValue = 0;
		int actives = 0;

		actives = heuristicCures(board);

		for (int i = 0; i < colors.length; i++) {
			totalValue += actives * getDiscardedPlayerDeck(board, colors[i]);
		}
		return totalValue;
	}

	/* 5) TOTAL NUMBER OF INFECTIONS-CUBES IN CURRENT STATE */
	public int heuristicInfection(Board board) {
		Vector<City> list = board.getCityList();
		Iterator<City> value = list.iterator();

		int totalCubes = 0;
		while (value.hasNext()) {
			City city = (City) value.next();
			totalCubes += totalCubes(city);
		}
		return totalCubes;
	}

	/* 6) AVERAGE DISTANCE REQUIRED TO MOVE FROM EACH CITY TO ANOTHER ASSOSIATED WITH THE NUMBER OF CARDS PLAYED */
	public double heuristicDistance(Board board) {
		Vector<City> Cities = board.getCityList(); 

		int cardsPlayedSoFar = board.getPlayersDeck().size();
		int epidemicCards = board.getNumberOfEpidemicCards();
		int totalCityCards = board.getCitiesCount();
		int constant = 2256;

		double dist = 0;
		for (int i = 0; i < board.getCitiesCount(); i++) {
            ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
            distanceMap= Client.buildDistanceMap(board, Cities.get(i).getName(), distanceMap);
            
			for (int j = 0; j < board.getCitiesCount(); j++) {
                    dist += (Client.distanceFrom(Cities.get(j).getName(),distanceMap)/constant)*(cardsPlayedSoFar/(totalCityCards + epidemicCards));
			}
		}
		return dist;
	}

	/* 7) COUNTS THE NUMBER OF ACTIVE DISEASES WHICH ARE STILL LACKING A CURE */
	public int heuristicCures(Board board) {
		String[] colors = board.getAllColors();

		int totalActives = 0;
		for (int i = 0; i < colors.length; i++) {
			if (!board.getCured(colors[i])) {
				totalActives++;
			}
		}
		return totalActives;
    }
	
	/* 8) TOTAL EVALUATION */
    public double Evaluation(Board board){
        double hstate = 0;
        double hsurv = heuristicSurvive(board); 
        double hcure = heuristicCure(board);
        double hcards = heuristicCards(board); 
        double hdisc = heuristicDiscard(board);
        double hinf = heuristicInfection(board);
        double hdist = heuristicDistance(board);
        double hcures = heuristicCures(board);

        hstate = 0.5*hsurv + 0.5*hcure + 1*hcards + 0.5*hdisc + 0.6*hinf + 0.6*hdist + 24*hcures;
        
        return hstate;
    }

	public int getDiscardedPlayerDeck(Board board, String Color) {
		ArrayList<String> PlayerDeck = board.getPlayersDeck();
		int ColorCounter = 12;
		String cityColor = null;

		for (int i = 0; i < PlayerDeck.size(); i++) {
			if (PlayerDeck.get(i).equals("Epidemic"))
				continue;
            cityColor = board.colorOf(PlayerDeck.get(i)); 
			if (cityColor.equals(Color)) {
				ColorCounter--;
			}
		}
		return ColorCounter;
	}
	
	public int distanceOfCities(Board board, int playerID) {
		int distance = 0;

		ArrayList<citiesWithDistancesObj> distanceMap = BuildPlayerDistanceMap(board, playerID);
		for (int i = 0; i < distanceMap.size(); i++) {
			String CityName = distanceMap.get(i).getName();
			distance += Client.distanceFrom(CityName, distanceMap) * totalCubes(board.searchForCity(CityName));
		}

		return distance;
	}

	public ArrayList<citiesWithDistancesObj> BuildPlayerDistanceMap(Board board, int playerID) {

		String playerCurrentCity = board.getPawnsLocations(playerID);
		ArrayList<citiesWithDistancesObj> dM = new ArrayList<citiesWithDistancesObj>();
		dM = Client.buildDistanceMap(board, playerCurrentCity, dM);
		return dM;
	}

	public int CloserCityWithRS(ArrayList<citiesWithDistancesObj> distanceMap) {
		Iterator<citiesWithDistancesObj> value = distanceMap.iterator();

		int minDistance = Integer.MAX_VALUE;
		while (value.hasNext()) {
			City curCity = (City) value.next().getCityObj();
			int dist = Client.distanceFrom(curCity.getName(), distanceMap);

			if (curCity.getHasReseachStation() && dist < minDistance) {
				minDistance = dist;
			}
		}
		return minDistance;
	}

	/* GET THE TOTAL CUBES IN A CITY */
	public int totalCubes(City city) {
		return (city.getCubes("Red") + city.getCubes("Black") + city.getCubes("Yellow") + city.getCubes("Blue"));
	}

	/* GETTERS AND SETTERS */
	public int getAgentID() {
		return agentID;
	}

	public void setAgentID(int agentID) {
		this.agentID = agentID;
	}

	public String getMyAction() {
		return myAction;
	}

	public void setMyAction(String myAction) {
		this.myAction = myAction;
	}

	public String getMySuggestions() {
		return mySuggestions;
	}

	public void setMySuggestions(String mySuggestions) {
		this.mySuggestions = mySuggestions;
	}

	public Board getMyBoard() {
		return myBoard;
	}

	public void setMyBoard(Board myBoard) {
		this.myBoard = myBoard;
	}

}
