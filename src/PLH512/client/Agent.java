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

	/* 1) */
	public double heuristicSurvive() {
		double evaluateState = 0;

		for (int i = 0; i < getMyBoard().getNumberOfPlayers(); i++) {
			evaluateState += distanceOfCities(i) / heuristicInfection();
		}

		return evaluateState;
	}

	/* 2) */
	public double heuristicCure() {
		double evaluateState = 0;

		for (int i = 0; i < getMyBoard().getNumberOfPlayers(); i++) {
			evaluateState += CloserCityWithRS(BuildPlayerDistanceMap(i));
		}
		return evaluateState;
	}

	/* 3) */
	public int heuristicCards() {
		String[] colors = getMyBoard().getAllColors();
		int minColorCounter = Integer.MAX_VALUE;
		int totalValue = 0;
		int ColorCounter = 0;
		int actives = 0;
		int R = 0;

		actives = heuristicCures();
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < getMyBoard().getNumberOfPlayers(); j++) {
				if (getMyBoard().getRoleOf(j).equals("Scientist")) {
					R = 4;
				} else {
					R = 5;
				}
				ColorCounter = R - Client.cardsCounterOfColor(getMyBoard(), j, colors[i]);
				if (minColorCounter > ColorCounter)
					minColorCounter = ColorCounter;
			}
			totalValue += minColorCounter * actives;
		}
		return totalValue;
	}

	/* 4) */
	public double heuristicDiscard() {
		String[] colors = getMyBoard().getAllColors();
		int totalValue = 0;
		int actives = 0;

		actives = heuristicCures();

		for (int i = 0; i < colors.length; i++) {
			totalValue += actives * getDiscardedPlayerDeck(colors[i]);
		}
		return totalValue;
	}

	/* 5) GET THE TOTAL CUBES IN CURRENT STATE OF THE GAME */
	public int heuristicInfection() {
		Vector<City> list = getMyBoard().getCityList();
		Iterator<City> value = list.iterator();

		int totalCubes = 0;
		while (value.hasNext()) {
			City city = (City) value.next();
			totalCubes += totalCubes(city);
		}
		return totalCubes;
	}

	/* 6) */
	public double heuristicDistance() {
		Vector<City> Cities = getMyBoard().getCityList(); 

		int cardsPlayedSoFar = getMyBoard().getPlayersDeck().size();
		int epidemicCards = getMyBoard().getNumberOfEpidemicCards();
		int totalCityCards = getMyBoard().getCitiesCount();
		int constant = 2256;

		double dist = 0;
		for (int i = 0; i < getMyBoard().getCitiesCount(); i++) {
            ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
            distanceMap= Client.buildDistanceMap(getMyBoard(), Cities.get(i).getName(), distanceMap);
            
			for (int j = 0; j < getMyBoard().getCitiesCount(); j++) {
                    dist += (Client.distanceFrom(Cities.get(j).getName(),distanceMap)/constant)*(cardsPlayedSoFar/(totalCityCards + epidemicCards));
			}
		}
		return dist;
	}

	/* 7) */
	public int heuristicCures() {
		Board board = getMyBoard();
		String[] colors = getMyBoard().getAllColors();

		int totalActives = 0;
		for (int i = 0; i < colors.length; i++) {
			if (!board.getCured(colors[i])) {
				totalActives++;
			}
		}
		return totalActives;
    }
    
    public double Evaluation(){
        double hstate = 0;
        double hsurv = heuristicSurvive(); 
        double hcure = heuristicCure();
        double hcards = heuristicCards(); 
        double hdisc = heuristicDiscard();
        double hinf = heuristicInfection();
        double hdist = heuristicDistance();
        double hcures = heuristicCures();

        hstate = 0.5*hsurv + 0.5*hcure + 1*hcards + 0.5*hdisc + 0.6*hinf + 0.6*hdist + 24*hcures;
        
        return hstate;
    }

	public int getDiscardedPlayerDeck(String Color) {
		ArrayList<String> PlayerDeck = getMyBoard().getPlayersDeck();
		int ColorCounter = 12;
		String cityColor = null;

		for (int i = 0; i < PlayerDeck.size(); i++) {
			if (PlayerDeck.get(i).equals("Epidemic"))
				continue;
            cityColor = getMyBoard().colorOf(PlayerDeck.get(i)); 
			if (cityColor.equals(Color)) {
				ColorCounter--;
			}
		}
		return ColorCounter;
	}
	
	public int distanceOfCities(int playerID) {
		int distance = 0;

		ArrayList<citiesWithDistancesObj> distanceMap = BuildPlayerDistanceMap(playerID);
		for (int i = 0; i < distanceMap.size(); i++) {
			String CityName = distanceMap.get(i).getName();
			distance += Client.distanceFrom(CityName, distanceMap) * totalCubes(getMyBoard().searchForCity(CityName));
		}

		return distance;
	}

	public ArrayList<citiesWithDistancesObj> BuildPlayerDistanceMap(int playerID) {

		String playerCurrentCity = getMyBoard().getPawnsLocations(playerID);
		ArrayList<citiesWithDistancesObj> dM = new ArrayList<citiesWithDistancesObj>();
		dM = Client.buildDistanceMap(getMyBoard(), playerCurrentCity, dM);
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
