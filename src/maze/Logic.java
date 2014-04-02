package maze;

import java.util.HashMap;
import java.util.Random;

public class Logic extends Maze {

	private int dragonStrategy;

	// Standard Maze
	public Logic() {
		super();
		this.dragonStrategy = 1;
	}

	// Random Maze
	public Logic(int mazeSize, int dragonCounter, int dragonStrategy) {
		super(mazeSize, dragonCounter);
		this.dragonStrategy = dragonStrategy;
	}

	/**
	 * Processes the moves related to the game according to the input and game
	 * initial configuration variables
	 * 
	 * @param userInput
	 *            the String previously entered by the player used to attempt to
	 *            move the Hero.
	 * @return message String used to display relevant game events
	 */
	public String[] playGame(String userInput) {

		// [0] General messages ; [1] Hero messages ; [2] Dragon messages ; [3]
		// Eagle messages
		String message[] = new String[] { null, null, null, null };
		int dragonState;
		Random random = new Random();

		HashMap<Integer, Boolean> validHeroMoves = getValidHeroMoves();

		if (userInput.equals("q"))
			message[0] = "Abort";

		if (!hero.getDead())
			message[1] = moveHero(userInput, validHeroMoves);

		// Dragon(s) processing
		for (int i = 0; i < dragons.size(); i++) {
			Dragon dragon = dragons.get(i);
			dragonState = random.nextInt(4);

			if (dragonState == 0 && dragonStrategy == 2)
				dragon.setAsleep(true);
			else
				dragon.setAsleep(false);
			// Move dragon
			if (!dragon.getDead()) {
				if (dragon.getAsleep()) {
					getMazePiece(dragon.getPosX(), dragon.getPosY()).setSymbol(
							dragon.showDragon());
				} else if (dragonStrategy != 0)
					if (message[2] == null)
						message[2] = moveDragon(dragon);
					else
						moveDragon(dragon);
			}
		}
		if (!eagle.getDead())
			message[3] = moveEagle();

		// Check game status
		State state = checkGame();
		if (state.equals(State.HERO_WON))
			message[0] = "Hero won :)";
		else if (state.equals(State.HERO_DEAD))
			message[0] = "Hero died!";
		else if (state.equals(State.DRAGON_DEAD))
			message[0] = "Hero killed a dragon";

		return message;
	}

	/**
	 * Processes a valid Hero move and deals with possible Hero related game
	 * events
	 * 
	 * @param userInput
	 *            the String previously entered by the player used to attempt to
	 *            move the Hero.
	 * @param moves
	 *            HashMap previously generated only containing valid Hero moves
	 * @return message String used to display relevant Hero events
	 */
	public String moveHero(String userInput, HashMap<Integer, Boolean> moves) {

		String message = null;
		// Release eagle
		if (userInput.equals("e")) {
			if (eagle.getDead())
				return "Eagle is dead!";
			else if (hero.getArmed())
				return "Hero already armed!";
			else if (!eagle.getState().equals(State.EAGLE_FOLLOWING))
				return "Eagle isn't following the hero!";
			else {
				eagle.setState(State.EAGLE_PURSUING);
				hero.setEagle(false);
				eagle.setHeroX(hero.getPosX());
				eagle.setHeroY(hero.getPosY());
				return "Hero released the eagle";
			}
		}
		// Mover player Up
		if (userInput.equals("w")) {
			// Check if HashMap contains the key with value 0)
			if (moves.containsKey(Movement.MOVE_UP.getDirection())) {
				// Make current piece free
				message = swapHero(Movement.MOVE_UP.getDirection());
			} else
				return "Hero can't move up!";
		} else if (userInput.equals("s")) {
			// Check if HashMap contains the key with value 1)
			if (moves.containsKey(Movement.MOVE_DOWN.getDirection())) {
				message = swapHero(Movement.MOVE_DOWN.getDirection());
			} else
				return "Hero can't move down!";
		} else if (userInput.equals("d")) {
			// Check if HashMap contains the key with value 2)
			if (moves.containsKey(Movement.MOVE_RIGHT.getDirection())) {
				message = swapHero(Movement.MOVE_RIGHT.getDirection());
			} else
				return "Hero can't move right!";
		} else if (userInput.equals("a")) {
			// Check if HashMap contains the key with value 3)
			if (moves.containsKey(Movement.MOVE_LEFT.getDirection())) {
				message = swapHero(Movement.MOVE_LEFT.getDirection());
			} else
				return "Hero can't move left!";
		}
		return message;
	}

	/**
	 * Processes a valid Dragon move and deals with possible Dragon related game
	 * events
	 * 
	 * @param dragon
	 *            one object Dragon of the ArrayList<Dragon> dragons
	 * @return message String used to display relevant Dragon events
	 */
	public String moveDragon(Dragon dragon) {

		Random random = new Random();
		int previousX = dragon.getPosX(), previousY = dragon.getPosY();
		int nextX = 0, nextY = 0;
		int direction = random.nextInt(4);
		String nearSymbol, message = null;

		switch (direction) {
		case (0):
			// Move Up
			nextX = dragon.getPosX();
			nextY = dragon.getPosY() - 1;
			break;

		case (1):
			// Move Down
			nextX = dragon.getPosX();
			nextY = dragon.getPosY() + 1;
			break;

		case (2):
			// Move Right
			nextX = dragon.getPosX() + 1;
			nextY = dragon.getPosY();
			break;

		case (3):
			// Move Left
			nextX = dragon.getPosX() - 1;
			nextY = dragon.getPosY();
			break;

		default:
			break;
		}

		nearSymbol = getMazePieceSymbol(nextX, nextY);
		// Reject invalid Dragon moves
		if (!nearSymbol.equals(PieceType.FREE.asString())
				&& !nearSymbol.equals(PieceType.GROUND_EAGLE.asString())
				&& !nearSymbol.equals(PieceType.SWORD.asString())) {
			nextX = 0;
			nextY = 0;
		}

		if (nextX != 0 && nextY != 0) {
			// Clear left piece symbol
			getMazePiece(previousX, previousY).setSymbol(
					PieceType.FREE.asString());

			if (dragon.getGuarding()
					&& (nextX != previousX || nextY != previousY)) {
				getMazePiece(previousX, previousY).setSymbol(
						PieceType.SWORD.asString());
				dragon.setGuarding(false);
			} else if ((getSword().getPosX() == nextX)
					&& (getSword().getPosY() == nextY) && !hero.getArmed()
					&& !dragon.getGuarding()) {
				if (eagle.getState().equals(State.EAGLE_GROUND)
						&& !eagle.getDead()) {
					eagle.setDead(true);
					dragon.setGuarding(true);
					message = "Dragon killed the eagle!\n * Dragon is now guarding the sword";
				} else if (eagle.getState().equals(State.EAGLE_RETURNING))
					dragon.setGuarding(false);
				else {
					dragon.setGuarding(true);
					message = "Dragon is now guarding the sword";
				}
			}
			// Set entry piece symbol
			getMazePiece(nextX, nextY).setSymbol(dragon.showDragon());
			// Update dragon position
			dragon.setPosition(nextX, nextY);
		}
		return message;
	}

	/**
	 * Processes a valid Eagle move and deals with possible Eagle related game
	 * events
	 * 
	 * @return message String used to display relevant Eagle events
	 */
	public String moveEagle() {
		String previousPiece;
		String nextPiece;
		int previousX = eagle.getPosX();
		int previousY = eagle.getPosY();
		int nextX = 0, nextY = 0;

		// Update eagle position to follow hero
		if (eagle.getState().equals(State.EAGLE_FOLLOWING) || (hero.getArmed())) {
			eagle.setPosition(hero.getPosX(), hero.getPosY());
		}
		// Calculate distance between the eagle and the sword
		else if (eagle.getState().equals(State.EAGLE_PURSUING)) {
			if (!hero.getDead()) {
				int deltaX = getSword().getPosX() - eagle.getPosX();
				int deltaY = getSword().getPosY() - eagle.getPosY();
				// Descend at arrival to the sword position
				if (deltaX == 0 && deltaY == 0) {
					// Dragon guarding sword at arrival position
					if (getMazePiece(eagle.getPosX(), eagle.getPosY())
							.getSymbol().contains("F")) {
						eagle.setDead(true);
						return "Eagle died!";
					}
					// Free sword
					else {
						getMazePiece(eagle.getPosX(), eagle.getPosY())
								.setSymbol("E G");
						eagle.setState(State.EAGLE_RETURNING);
					}
				}
				// Eagle movement in pursuit of the sword
				else {
					if (Math.abs(deltaX) > Math.abs(deltaY)) {
						if (deltaX < 0) {
							nextX = eagle.getPosX() - 1;
							nextY = eagle.getPosY();
						} else {
							nextX = eagle.getPosX() + 1;
							nextY = eagle.getPosY();
						}
					} else if (deltaY < 0) {
						nextX = eagle.getPosX();
						nextY = eagle.getPosY() - 1;
					} else {
						nextX = eagle.getPosX();
						nextY = eagle.getPosY() + 1;
					}
					// Generate custom maze symbols
					previousPiece = getMazePiece(previousX, previousY)
							.getSymbol().substring(0, 2) + " ";
					nextPiece = getMazePiece(nextX, nextY).getSymbol()
							.substring(0, 2) + eagle.showEagle();

					// Set or restore maze symbols
					getMazePiece(previousX, previousY).setSymbol(previousPiece);
					getMazePiece(nextX, nextY).setSymbol(nextPiece);

					// Update eagle position
					eagle.setPosition(nextX, nextY);
				}
			}
		}
		// Calculate distance between the eagle and the hero
		else if (eagle.getState().equals(State.EAGLE_RETURNING)) {
			if (!hero.getDead()) {
				int deltaX = eagle.getHeroX() - eagle.getPosX();
				int deltaY = eagle.getHeroY() - eagle.getPosY();

				// Descend at arrival to the hero position
				if (deltaX == 0 && deltaY == 0) {
					if (eagle.getHeroX() != hero.getPosX()
							|| eagle.getHeroY() != hero.getPosY()) {
						eagle.setState(State.EAGLE_GROUND);
						return "Hero was not on the same spot - Eagle is in the ground!";
					} else {
						hero.setEagle(true);
						hero.setArmed(true);
						eagle.setState(State.EAGLE_PURSUING);
						getMazePiece(hero.getPosX(), hero.getPosY()).setSymbol(
								hero.showHero());
						return "Eagle returned successfuly and hero is now armed";

					}
				}
				// Eagle movement in pursuit of the hero
				else {
					if (Math.abs(deltaX) > Math.abs(deltaY)) {

						if (deltaX < 0) {
							nextX = eagle.getPosX() - 1;
							nextY = eagle.getPosY();
						} else {
							nextX = eagle.getPosX() + 1;
							nextY = eagle.getPosY();
						}
					} else if (deltaY < 0) {
						nextX = eagle.getPosX();
						nextY = eagle.getPosY() - 1;
					} else {
						nextX = eagle.getPosX();
						nextY = eagle.getPosY() + 1;
					}
					// Generate custom maze symbols
					previousPiece = " "
							+ getMazePiece(previousX, previousY).getSymbol()
									.substring(1, 2) + " ";
					nextPiece = "E"
							+ getMazePiece(nextX, nextY).getSymbol().substring(
									1, 2) + eagle.showEagle();

					// Set or restore maze symbols
					getMazePiece(eagle.getPosX(), eagle.getPosY()).setSymbol(
							previousPiece);
					getMazePiece(nextX, nextY).setSymbol(nextPiece);
					// Update eagle and sword positions
					eagle.setPosition(nextX, nextY);
					getSword().setPosition(nextX, nextY);
				}
			}
		}
		return null;
	}

	/**
	 * Processes the possible game consequences after a round of all Character
	 * movements
	 * 
	 * @return State of the game after every Character movement is taken into
	 *         account
	 */
	public State checkGame() {
		State state = State.GAME_CONTINUE;
		if ((hero.getPosX() == getExitX()) && (hero.getPosY() == getExitY())
				&& hero.getArmed()) {
			return State.HERO_WON;
		}
		Dragon dragon;
		for (int i = 0; i < dragons.size(); i++) {
			dragon = nearDragon();
			if (dragon != null) {
				if (!hero.getArmed()) {
					if (!dragon.getDead() && !dragon.getAsleep()) {
						// If unarmed, hero dies and the game ends
						return State.HERO_DEAD;
					}
				}
				// Armed hero
				else if (!dragon.getDead()) {
					// Set dragon state as dead
					dragon.setDead(true);
					getMazePiece(dragon.getPosX(), dragon.getPosY()).setSymbol(
							dragon.showDragon());
					dragons.remove(dragon);
					state = State.DRAGON_DEAD;
				}
			}
		}
		return state;
	}

	/**
	 * Checks each of the 4 positions around hero for availability and if adds
	 * it to HasMap validMoves
	 * 
	 * @return HashMap with possible positions for hero to move into
	 */
	public HashMap<Integer, Boolean> getValidHeroMoves() {
		HashMap<Integer, Boolean> validMoves = new HashMap<Integer, Boolean>();
		boolean heroArmado = hero.getArmed();

		// Check if hero can move up
		if (hero.getPosY() - 1 >= 0) {
			if (maze.get(hero.getPosY() - 1).get(hero.getPosX()).getSymbol()
					.equals(PieceType.FREE.asString())
					|| maze.get(hero.getPosY() - 1).get(hero.getPosX())
							.getSymbol()
							.equals(PieceType.GROUND_EAGLE.asString())
					|| maze.get(hero.getPosY() - 1).get(hero.getPosX())
							.getSymbol()
							.equals(PieceType.PURSUING_EAGLE.asString())
					|| maze.get(hero.getPosY() - 1).get(hero.getPosX())
							.getSymbol().equals(PieceType.SWORD.asString()))
				validMoves.put(0, true);
			// Confirms if hero is armed at exit
			else if ((maze.get(hero.getPosY() - 1).get(hero.getPosX())
					.getSymbol().equals(PieceType.EXIT.asString()))
					&& heroArmado)
				validMoves.put(0, true);
		}
		// Check if hero can move down
		if (hero.getPosY() + 1 < maze.size()) {
			if (maze.get(hero.getPosY() + 1).get(hero.getPosX()).getSymbol()
					.equals(PieceType.FREE.asString())
					|| maze.get(hero.getPosY() + 1).get(hero.getPosX())
							.getSymbol()
							.equals(PieceType.GROUND_EAGLE.asString())
					|| maze.get(hero.getPosY() + 1).get(hero.getPosX())
							.getSymbol()
							.equals(PieceType.PURSUING_EAGLE.asString())
					|| maze.get(hero.getPosY() + 1).get(hero.getPosX())
							.getSymbol().equals(PieceType.SWORD.asString()))
				validMoves.put(1, true);
			// Confirms if hero is armed at exit
			else if ((maze.get(hero.getPosY() + 1).get(hero.getPosX())
					.getSymbol().equals(PieceType.EXIT.asString()))
					&& heroArmado)
				validMoves.put(1, true);
		}
		// Check if hero can move to the right
		if (hero.getPosX() + 1 < maze.size()) {
			if (maze.get(hero.getPosY()).get(hero.getPosX() + 1).getSymbol()
					.equals(PieceType.FREE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() + 1)
							.getSymbol()
							.equals(PieceType.GROUND_EAGLE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() + 1)
							.getSymbol()
							.equals(PieceType.PURSUING_EAGLE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() + 1)
							.getSymbol().equals(PieceType.SWORD.asString()))
				validMoves.put(2, true);
			// Confirms if hero is armed at exit
			else if ((maze.get(hero.getPosY()).get(hero.getPosX() + 1)
					.getSymbol().equals(PieceType.EXIT.asString()))
					&& heroArmado)
				validMoves.put(2, true);
		}
		// Check if hero can move to the left
		if (hero.getPosX() - 1 >= 0) {
			if (maze.get(hero.getPosY()).get(hero.getPosX() - 1).getSymbol()
					.equals(PieceType.FREE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() - 1)
							.getSymbol()
							.equals(PieceType.GROUND_EAGLE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() - 1)
							.getSymbol()
							.equals(PieceType.PURSUING_EAGLE.asString())
					|| maze.get(hero.getPosY()).get(hero.getPosX() - 1)
							.getSymbol().equals(PieceType.SWORD.asString()))
				validMoves.put(3, true);
			// Confirms if hero is armed at exit
			else if ((maze.get(hero.getPosY()).get(hero.getPosX() - 1)
					.getSymbol().equals(PieceType.EXIT.asString()))
					&& heroArmado)
				validMoves.put(3, true);
		}
		return validMoves;
	}

	/**
	 * Checks for Dragons near the Hero
	 * 
	 * @return Dragon near the Hero
	 */
	public Dragon nearDragon() {
		// Generic position returning function
		int heroX = hero.getPosX();
		int heroY = hero.getPosY();

		for (int i = 0; i < dragons.size(); i++) {
			Dragon dragon = dragons.get(i);
			int dragonX = dragon.getPosX();
			int dragonY = dragon.getPosY();

			if (heroX + 1 == dragonX && heroY == dragonY)
				return dragon;
			else if (heroX - 1 == dragonX && heroY == dragonY)
				return dragon;
			else if (heroX == dragonX && heroY - 1 == dragonY)
				return dragon;
			else if (heroX == dragonX && heroY + 1 == dragonY)
				return dragon;
		}
		return null;
	}

	/**
	 * Checks if given Dragon is at the sword position
	 * 
	 * @param dragon
	 *            one object Dragon of the ArrayList<Dragon> dragons
	 * @return boolean with the validity of the function checks
	 */
	public boolean dragonAtSword(Dragon dragon) {
		return (dragon.getPosX() == getSword().getPosX())
				&& (dragon.getPosY() == getSword().getPosY());
	}
}
