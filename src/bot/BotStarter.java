/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.*;

import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
    private static Random random = new Random(1234);

	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut)
	{
		Integer bestRegionID = null;
        int bestSize = 0;

        for(Region region : state.getPickableStartingRegions()) {
            int size = region.getSuperRegion().getSubRegions().size();
            System.err.println("Region "+region.getId()+" belongs to super region "+region.getSuperRegion().getId()+" of size "+region.getSuperRegion().getSubRegions().size());
            if (bestRegionID == null || size < bestSize) {
                bestSize = size;
                bestRegionID = region.getId();
            }
        }

		Region startingRegion = state.getFullMap().getRegion(bestRegionID);
		
		return startingRegion;
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = borderRegions(state);
		
		while(armiesLeft > 0)
		{
			double rand = random.nextDouble();
			int r = (int) (rand*visibleRegions.size());
			Region region = visibleRegions.get(r);
			
			if(region.ownedByPlayer(myName))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
			}
		}
		
		return placeArmiesMoves;
	}

    private LinkedList<Region> borderRegions(BotState state) {
        return state.getVisibleMap().getBorderRegions();
    }

    @Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		final String myName = state.getMyPlayerName();
		int armies = 5;
		int maxTransfers = 10;
		int transfers = 0;

		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());

                Collections.sort(possibleToRegions, new Comparator<Region>() {
                    private int score(Region region) {
                        return (region.getPlayerName().equals(myName) ? 100 : 0) +
                            region.getSuperRegion().countNotByOwner(myName);
                    }

                    @Override
                    public int compare(Region o1, Region o2) {
                        return score(o1) - score(o2);
                    }
                });

                System.err.println("From region "+fromRegion.getId()+" has "+fromRegion.getArmies()+" armies");
				while(!possibleToRegions.isEmpty())
				{
                    Region toRegion = possibleToRegions.get(0);

                    if (fromRegion.getPlayerName().equals(myName) && toRegion.getPlayerName().equals(myName) && fromRegion.isBorder() && !toRegion.isBorder()) {
                        possibleToRegions.remove(toRegion);
                        continue;
                    }
                    System.err.println("Considering "+toRegion.getId()+" which has countNotByOwner of "+toRegion.getSuperRegion().countNotByOwner(myName));

					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 5) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1
								&& transfers < maxTransfers) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						transfers++;
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
