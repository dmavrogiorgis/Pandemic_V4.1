package PLH512.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import PLH512.server.Board;
import PLH512.server.City;

public class Client {
	final static int ServerPort = 64240;
	final static String username = "myName";

	public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException {
		int numberOfPlayers;
		int myPlayerID;
		String myUsername;
		String myRole;

		// Getting localhost ip
		InetAddress ip = InetAddress.getByName("localhost");

		// Establish the connection
		Socket s = new Socket(ip, ServerPort);
		System.out.println("\nConnected to server!");

		// Obtaining input and out streams
		ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

		// Receiving the playerID from the Server
		myPlayerID = (int) dis.readObject();
		myUsername = "User_" + myPlayerID;
		System.out.println("\nHey! My username is " + myUsername);

		// Receiving number of players to initialize the board
		numberOfPlayers = (int) dis.readObject();

		// Receiving my role for this game
		myRole = (String) dis.readObject();
		System.out.println("\nHey! My role is " + myRole);

		// Sending the username to the Server
		dos.reset();
		dos.writeObject(myUsername);

		// Setting up the board
		Board[] currentBoard = { new Board(numberOfPlayers) };

		// Creating sendMessage thread
		Thread sendMessage = new Thread(new Runnable() {
			@Override
			public void run() {

				boolean timeToTalk = false;

				// MPOREI NA GINEI WHILE TRUE ME BREAK GIA SINTHIKI??
				while (currentBoard[0].getGameEnded() == false) {
					timeToTalk = ((currentBoard[0].getWhoIsTalking() == myPlayerID)
							&& !currentBoard[0].getTalkedForThisTurn(myPlayerID));

					try {
						TimeUnit.MILLISECONDS.sleep(15);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					try {
						// Executing this part of the code once per round
						if (timeToTalk) {

							// Initializing variables for current round

							Board myBoard = currentBoard[0];

							String myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
							City myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

							ArrayList<String> myHand = myBoard.getHandOf(myPlayerID);

							int[] myColorCount = { 0, 0, 0, 0 };

							for (int i = 0; i < 4; i++)
								myColorCount[i] = cardsCounterOfColor(myBoard, myPlayerID, myBoard.getColors(i));

							ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
							distanceMap = buildDistanceMap(myBoard, myCurrentCity, distanceMap);

							String myAction = "";
							// String mySuggestion = "";

							int myActionCounter = 0;

							// Printing out my current hand

							System.out.println("\nMy current hand...");
							printHand(myHand);

							// Printing out current color count
							System.out.println("\nMy hand's color count...");
							for (int i = 0; i < 4; i++)
								System.out.println(myBoard.getColors(i) + " cards count: " + myColorCount[i]);

							// Printing out distance map from current city
							// System.out.println("\nDistance map from " + myCurrentCity);
							// printDistanceMap(distanceMap);

							// ADD YOUR CODE FROM HERE AND ON!!
							Agent ag = new Agent(myPlayerID, myBoard);

							boolean tryToCure = false;
							String colorToCure = null;

							boolean tryToTreatHere = false;
							String colorToTreat = null;

							boolean tryToTreatClose = false;
							String destinationClose = null;

							boolean tryToTreatMedium = false;
							String destinationMedium = null;

							double adam_eva = ag.Evaluation();
							System.out.println("AGENT EVALUATION: " + adam_eva);
							ArrayList<String> al = getMoves(myPlayerID,  myBoard);
							
							System.err.println("ARRAY LIST SIZE: " + al.size());
							
							myBoard.printCitiesAndCubes();
							// String destinationRandom = null;

							if (myColorCount[0] > 4 || myColorCount[1] > 4 || myColorCount[2] > 4
									|| myColorCount[3] > 4) {
								if (myActionCounter < 4)
									tryToCure = true;

								if (myColorCount[0] > 4)
									colorToCure = "Black";
								else if (myColorCount[1] > 4)
									colorToCure = "Yellow";
								else if (myColorCount[2] > 4)
									colorToCure = "Blue";
								else if (myColorCount[3] > 4)
									colorToCure = "Red";
							}

							if (tryToCure) {
								System.out.println("I want to try and cure the " + colorToCure + " disease!");
								myAction = myAction + toTextCureDisease(myPlayerID, colorToCure);
								myBoard.cureDisease(myPlayerID, colorToCure);
								myActionCounter++;

							}

							if (myCurrentCityObj.getBlackCubes() != 0 || myCurrentCityObj.getYellowCubes() != 0
									|| myCurrentCityObj.getBlueCubes() != 0 || myCurrentCityObj.getRedCubes() != 0) {
								if (myActionCounter < 4)
									tryToTreatHere = true;

								if (myCurrentCityObj.getBlackCubes() > 0)
									colorToTreat = "Black";
								else if (myCurrentCityObj.getYellowCubes() > 0)
									colorToTreat = "Yellow";
								else if (myCurrentCityObj.getBlueCubes() > 0)
									colorToTreat = "Blue";
								else if (myCurrentCityObj.getRedCubes() > 0)
									colorToTreat = "Red";
							}

							if (tryToTreatHere) {
								while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4) {
									colorToTreat = myCurrentCityObj.getMaxCubeColor();

									System.out.println("I want to try and treat one " + colorToTreat + " cube from "
											+ myCurrentCity + "!");

									myAction = myAction + toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat);
									myActionCounter++;

									myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
								}
							}

							if (myActionCounter < 4) {
								destinationClose = getMostInfectedInRadius(1, distanceMap, myBoard);

								if (!destinationClose.equals(myCurrentCity))
									tryToTreatClose = true;
							}

							if (tryToTreatClose) {
								System.out.println("Hhhmmmmmm I could go and try to treat " + destinationClose);

								myAction = myAction + toTextDriveTo(myPlayerID, destinationClose);
								myActionCounter++;

								myBoard.driveTo(myPlayerID, destinationClose);

								myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
								myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

								while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4) {
									colorToTreat = myCurrentCityObj.getMaxCubeColor();

									System.out.println("I want to try and treat one " + colorToTreat + " cube from "
											+ myCurrentCity + "!");

									myAction = myAction + toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat);
									myActionCounter++;

									myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
								}
							}

							if (myActionCounter < 4) {
								destinationMedium = getMostInfectedInRadius(2, distanceMap, myBoard);

								if (!destinationMedium.equals(myCurrentCity))
									tryToTreatMedium = true;
							}

							if (tryToTreatMedium) {
								System.out.println("Hhhmmmmmm I could go and try to treat " + destinationMedium);

								String driveFirstTo = getDirectionToMove(myCurrentCity, destinationMedium, distanceMap,
										myBoard);

								myAction = myAction + toTextDriveTo(myPlayerID, driveFirstTo);
								myActionCounter++;
								myAction = myAction + toTextDriveTo(myPlayerID, destinationMedium);
								myActionCounter++;

								myBoard.driveTo(myPlayerID, driveFirstTo);

								myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
								myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

								myBoard.driveTo(myPlayerID, destinationMedium);

								myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
								myCurrentCityObj = myBoard.searchForCity(myCurrentCity);

								while (myCurrentCityObj.getMaxCube() != 0 && myActionCounter < 4) {
									colorToTreat = myCurrentCityObj.getMaxCubeColor();

									System.out.println("I want to try and treat one " + colorToTreat + " cube from "
											+ myCurrentCity + "!");

									myAction = myAction + toTextTreatDisease(myPlayerID, myCurrentCity, colorToTreat);
									myActionCounter++;

									myBoard.treatDisease(myPlayerID, myCurrentCity, colorToTreat);
								}
							}

							Random rand = new Random();

							while (myActionCounter < 4) {
								int upperBound;
								int randomNumber;
								String randomCityToGo;

								upperBound = myCurrentCityObj.getNeighboursNumber();
								randomNumber = rand.nextInt(upperBound);
								randomCityToGo = myCurrentCityObj.getNeighbour(randomNumber);

								System.out.println("Moving randomly to " + randomCityToGo);

								myAction = myAction + toTextDriveTo(myPlayerID, randomCityToGo);
								myActionCounter++;

								myBoard.driveTo(myPlayerID, randomCityToGo);

								myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
								myCurrentCityObj = myBoard.searchForCity(myCurrentCity);
							}

							// UP TO HERE!! DON'T FORGET TO EDIT THE "msgToSend"

							// Message type
							// toTextShuttleFlight(0,Atlanta)+"#"+etc
							String msgToSend;
							if (myBoard.getWhoIsPlaying() == myPlayerID)
								msgToSend = myAction;

							// msgToSend =
							// "AP,"+myPlayerID+"#AP,"+myPlayerID+"#AP,"+myPlayerID+"#C,"+myPlayerID+",This
							// was my action#AP,"+myPlayerID+"#C,"+myPlayerID+",This should not be
							// printed..";//"Action";
							else
								msgToSend = "#C," + myPlayerID + ",This was my recommendation"; // "Recommendation"

							// NO EDIT FROM HERE AND ON (EXEPT FUNCTIONS OUTSIDE OF MAIN() OF COURSE)

							// Writing to Server
							dos.flush();
							dos.reset();
							if (msgToSend != "")
								msgToSend = msgToSend.substring(1); // Removing the initial delimeter
							dos.writeObject(msgToSend);
							System.out.println(myUsername + " : I've just sent my " + msgToSend);
							currentBoard[0].setTalkedForThisTurn(true, myPlayerID);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		// Creating readMessage thread
		Thread readMessage = new Thread(new Runnable() {
			@Override
			public void run() {

				while (currentBoard[0].getGameEnded() == false) {
					try {

						// Reading the current board
						// System.out.println("READING!!!");
						currentBoard[0] = (Board) dis.readObject();
						// System.out.println("READ!!!");

						// Read and print Message to all clients
						String prtToScreen = currentBoard[0].getMessageToAllClients();
						if (!prtToScreen.equalsIgnoreCase(""))
							System.out.println(prtToScreen);

						// Read and print Message this client
						prtToScreen = currentBoard[0].getMessageToClient(myPlayerID);
						if (!prtToScreen.equalsIgnoreCase(""))
							System.out.println(prtToScreen);

					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		});

		// Starting the threads
		readMessage.start();
		sendMessage.start();

		// Checking if the game has ended
		while (true) {
			if (currentBoard[0].getGameEnded() == true) {
				System.out.println("\nGame has finished. Closing resources.. \n");
				// scn.close();
				s.close();
				System.out.println("Recources closed succesfully. Goodbye!");
				System.exit(0);
				break;
			}

		}
	}

	
	/* GET ALL POSSIBLE MOVES */ 
	public static ArrayList<String> getMoves(int playerID, Board board) {
		ArrayList<String> myMovesList = new ArrayList<String>();
		ArrayList<String> myHand = board.getHandOf(playerID);

		String curCityName = board.getPawnsLocations(playerID);

		City currentCity = board.searchForCity(board.getPawnsLocations(playerID));

		String maxDisease = currentCity.getMaxCubeColor();

		String[] colors = board.getAllColors();
		String myMove = "";

		for (int j = 0; j < currentCity.getNeighboursNumber(); j++) {
			if (isLegalDriveTo(playerID, currentCity.getNeighbour(j), board)) {
				myMove = "#DT:" + currentCity.getNeighbour(j); // TODO
				System.out.println(myMove);
				myMovesList.add(myMove);
			}
		}

		for (int i = 0; i < myHand.size(); i++) {
			if (isLegalDirectFlight(playerID, myHand.get(i), board)) {
				myMove = "#DF:" + myHand.get(i); // TODO
				System.out.println(myMove);
				myMovesList.add(myMove);

			} else if (isLegalCharterFlight(playerID, myHand.get(i), board)) {
				myMove = "#CF:" + myHand.get(i); // TODO
				System.out.println(myMove);
				myMovesList.add(myMove);

			} else if (isLegalShuttleFlight(playerID, myHand.get(i), board)) {
				myMove = "#SF:" + myHand.get(i); // TODO
				System.out.println(myMove);
				myMovesList.add(myMove);

			} else if (isLegalBuildRS(playerID, myHand.get(i), board)) {
				myMove = "#RS:" + myHand.get(i); // TODO
				System.out.println(myMove);
				myMovesList.add(myMove);
			}
		}

		if (isLegalTreatDisease(playerID, curCityName, maxDisease, board)) {
			myMove = "#TD:" + curCityName + ":" + maxDisease; // TODO
			myMovesList.add(myMove);
		}

		for (int i = 0; i < colors.length; i++) {
			if (isLegalCureDisease(playerID, colors[i], board)) {
				myMove = "#CD1:" + curCityName + ":" + maxDisease; // TODO
				myMovesList.add(myMove);
			}
		}
		return myMovesList;
	}
	
	/* THESE ARE USED TO CHECK IF A PLAYER MOVE IS LEGAL */
	/* IS LEGAL TO USE DRIVE TO A CITY */ 
	public static boolean isLegalDriveTo(int playerID, String destination, Board board) {
		City currentCity = board.searchForCity(board.getPawnsLocations(playerID));
		boolean isLegal = false;

		for (int i = 0; i < currentCity.getNeighboursNumber(); i++)
			if (currentCity.getNeighbour(i).equals(destination))
				isLegal = true;
		return isLegal;
	}

	/* IS LEGAL TO USE DIRECT FLIGHT TO A CITY */ 
	public static boolean isLegalDirectFlight(int playerID, String destination, Board board) {
		boolean isLegal = false;

		if (board.getHandOf(playerID).contains(destination))
			isLegal = true;
		return isLegal;
	}

	/* IS LEGAL TO USE CHARTER FLIGHT TO A CITY */ 
	public static boolean isLegalCharterFlight(int playerID, String destination, Board board) {
		boolean isLegal = false;

		if (board.getHandOf(playerID).contains(board.getPawnsLocations(playerID)))
			isLegal = true;
		return isLegal;
	}

	/* IS LEGAL TO USE SHUTTLE FLIGHT TO A CITY */ 
	public static boolean isLegalShuttleFlight(int playerID, String destination, Board board) {
		boolean isLegal = false;

		if (board.getRSLocations().contains(board.getPawnsLocations(playerID))
				&& board.getRSLocations().contains(destination))
			isLegal = true;

		return isLegal;
	}

	/* IS LEGAL TO USE BUILD RS IN A CITY */ 
	public static boolean isLegalBuildRS(int playerID, String cityToBuild, Board board) {
		boolean isOperationsExpert = board.getRoleOf(playerID).equals("Operations Expert");
		boolean isLegal = false;

		if (board.getPawnsLocations(playerID).equals(cityToBuild) && isOperationsExpert)
			isLegal = true;
		else if (board.getPawnsLocations(playerID).equals(cityToBuild)
				&& board.getHandOf(playerID).contains(cityToBuild))
			isLegal = true;
		else
			isLegal = false;
		return isLegal;
	}

	/* IS LEGAL TO USE TREAT DIDEASE IN A CITY */ 
	public static boolean isLegalTreatDisease(int playerID, String cityToTreat, String color, Board board) {
		City currentCity = board.searchForCity(board.getPawnsLocations(playerID));
		boolean isLegal = false;

		if (board.getPawnsLocations(playerID).equals(cityToTreat)) {
			if (color.equals("Black") && currentCity.getBlackCubes() > 0)
				isLegal = true;
			else if (color.equals("Yellow") && currentCity.getYellowCubes() > 0)
				isLegal = true;
			else if (color.equals("Blue") && currentCity.getBlueCubes() > 0)
				isLegal = true;
			else if (color.equals("Red") && currentCity.getRedCubes() > 0)
				isLegal = true;
		}
		return isLegal;
	}

	/* IS LEGAL TO USE CURE DISEASE IN A CITY */ 
	public static boolean isLegalCureDisease(int playerID, String colorToCure, Board board) {
		boolean isScientist = board.getRoleOf(playerID).equals("Scientist");
		boolean isLegal = false;
		int cardsColorCount = 0;

		if (board.getRSLocations().contains(board.getPawnsLocations(playerID))) {
			for (int i = 0; i < board.getHandOf(playerID).size(); i++) {
				City cityToCheck = board.searchForCity(board.getHandOf(playerID).get(i));

				if (cityToCheck.getColour().equals(colorToCure))
					cardsColorCount++;
			}

			if (cardsColorCount >= (board.getCardsNeededForCure() - 1) && isScientist)
				isLegal = true;
			else if (cardsColorCount >= board.getCardsNeededForCure())
				isLegal = true;
			else
				isLegal = false;
		}
		return isLegal;
	}

	// --> Useful functions <--
	public static Board copyBoard(Board boardToCopy) {
		Board copyOfBoard;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
			outputStrm.writeObject(boardToCopy);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
			copyOfBoard = (Board) objInputStream.readObject();
			return copyOfBoard;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getDirectionToMove(String startingCity, String goalCity,
			ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard) {
		City startingCityObj = myBoard.searchForCity(startingCity);

		int minDistance = distanceFrom(goalCity, distanceMap);
		int testDistance = 999;

		String directionToDrive = null;
		String testCity = null;

		for (int i = 0; i < startingCityObj.getNeighboursNumber(); i++) {
			ArrayList<citiesWithDistancesObj> testDistanceMap = new ArrayList<citiesWithDistancesObj>();
			testDistanceMap.clear();

			testCity = startingCityObj.getNeighbour(i);
			testDistanceMap = buildDistanceMap(myBoard, testCity, testDistanceMap);
			testDistance = distanceFrom(goalCity, testDistanceMap);

			if (testDistance < minDistance) {
				minDistance = testDistance;
				directionToDrive = testCity;
			}
		}
		return directionToDrive;
	}

	public static String getMostInfectedInRadius(int radius, ArrayList<citiesWithDistancesObj> distanceMap,
			Board myBoard) {
		int maxCubes = -1;
		String mostInfected = null;

		for (int i = 0; i < distanceMap.size(); i++) {
			if (distanceMap.get(i).getDistance() <= radius) {
				City cityToCheck = myBoard.searchForCity(distanceMap.get(i).getName());

				if (cityToCheck.getMaxCube() > maxCubes) {
					mostInfected = cityToCheck.getName();
					maxCubes = cityToCheck.getMaxCube();
				}
			}
		}

		return mostInfected;
	}

	// Count how many card of the color X player X has
	public static int cardsCounterOfColor(Board board, int playerID, String color) {
		int cardsCounter = 0;

		for (int i = 0; i < board.getHandOf(playerID).size(); i++)
			if (board.searchForCity(board.getHandOf(playerID).get(i)).getColour().equals(color))
				cardsCounter++;

		return cardsCounter;
	}

	public static void printHand(ArrayList<String> handToPrint) {
		for (int i = 0; i < handToPrint.size(); i++)
			System.out.println(handToPrint.get(i));
	}

	public static boolean alredyInDistanceMap(ArrayList<citiesWithDistancesObj> currentMap, String cityName) {
		for (int i = 0; i < currentMap.size(); i++)
			if (currentMap.get(i).getName().equals(cityName))
				return true;

		return false;
	}

	public static boolean isInDistanceMap(ArrayList<citiesWithDistancesObj> currentMap, String cityName) {
		for (int i = 0; i < currentMap.size(); i++) {
			if (currentMap.get(i).getName().equals(cityName))
				return true;
		}
		return false;
	}

	public static void printDistanceMap(ArrayList<citiesWithDistancesObj> currentMap) {
		for (int i = 0; i < currentMap.size(); i++)
			System.out.println("Distance from " + currentMap.get(i).getName() + ": " + currentMap.get(i).getDistance());
	}

	public static int distanceFrom(String cityToFind, ArrayList<citiesWithDistancesObj> currentDistanceMap) {
		int result = -1;

		for (int i = 0; i < currentDistanceMap.size(); i++)
			if (currentDistanceMap.get(i).getName().equals(cityToFind))
				result = currentDistanceMap.get(i).getDistance();

		return result;
	}

	public static int numberOfCitiesWithDistance(int distance, ArrayList<citiesWithDistancesObj> currentDistanceMap) {
		int count = 0;

		for (int i = 0; i < currentDistanceMap.size(); i++)
			if (currentDistanceMap.get(i).getDistance() == distance)
				count++;

		return count;
	}

	public static ArrayList<citiesWithDistancesObj> buildDistanceMap(Board myBoard, String currentCityName,
			ArrayList<citiesWithDistancesObj> currentMap) {
		currentMap.clear();
		currentMap.add(new citiesWithDistancesObj(currentCityName, myBoard.searchForCity(currentCityName), 0));

		for (int n = 0; n < 15; n++) {
			for (int i = 0; i < currentMap.size(); i++) {
				if (currentMap.get(i).getDistance() == (n - 1)) {
					for (int j = 0; j < currentMap.get(i).getCityObj().getNeighboursNumber(); j++) {
						String nameOfNeighbor = currentMap.get(i).getCityObj().getNeighbour(j);

						if (!(alredyInDistanceMap(currentMap, nameOfNeighbor)))
							currentMap.add(new citiesWithDistancesObj(nameOfNeighbor,
									myBoard.searchForCity(nameOfNeighbor), n));
					}
				}
			}
		}

		return currentMap;
	}

	// --> Actions <--

	// --> Coding functions <--

	public static String toTextDriveTo(int playerID, String destination) {
		return "#DT," + playerID + "," + destination;
	}

	public static String toTextDirectFlight(int playerID, String destination) {
		return "#DF," + playerID + "," + destination;
	}

	public static String toTextCharterFlight(int playerID, String destination) {
		return "#CF," + playerID + "," + destination;
	}

	public static String toTextShuttleFlight(int playerID, String destination) {
		return "#SF," + playerID + "," + destination;
	}

	public static String toTextBuildRS(int playerID, String destination) {
		return "#BRS," + playerID + "," + destination;
	}

	public static String toTextRemoveRS(int playerID, String destination) {
		return "#RRS," + playerID + "," + destination;
	}

	public static String toTextTreatDisease(int playerID, String destination, String color) {
		return "#TD," + playerID + "," + destination + "," + color;
	}

	public static String toTextCureDisease(int playerID, String color) {
		return "#CD1," + playerID + "," + color;
	}

	public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3,
			String card4) {
		return "#CD2," + playerID + "," + color + "," + card1 + "," + card2 + "," + card3 + "," + card4;
	}

	public static String toTextActionPass(int playerID) {
		return "#AP," + playerID;
	}

	public static String toTextChatMessage(int playerID, String messageToSend) {
		return "#C," + playerID + "," + messageToSend;
	}

	public static String toTextPlayGG(int playerID, String cityToBuild) {
		return "#PGG," + playerID + "," + cityToBuild;
	}

	public static String toTextPlayQN(int playerID) {
		return "#PQN," + playerID;
	}

	public static String toTextPlayA(int playerID, int playerToMove, String cityToMoveTo) {
		return "#PA," + playerID + "," + playerToMove + "," + cityToMoveTo;
	}

	public static String toTextPlayF(int playerID) {
		return "#PF," + playerID;
	}

	public static String toTextPlayRP(int playerID, String cityCardToRemove) {
		return "#PRP," + playerID + "," + cityCardToRemove;
	}

	public static String toTextOpExpTravel(int playerID, String destination, String colorToThrow) {
		return "#OET," + playerID + "," + destination + "," + colorToThrow;
	}

}