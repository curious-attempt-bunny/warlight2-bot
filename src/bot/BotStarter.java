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
        double bestScore = 0;

        for(Region region : state.getPickableStartingRegions()) {
            double score = region.getSuperRegion().score(state);
//            System.err.println("Region "+region.getId()+" belongs to super region "+region.getSuperRegion().getId()+" of size "+region.getSuperRegion().getSubRegions().size());
            if (bestRegionID == null || score > bestScore) {
                bestScore = score;
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
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(final BotState state, Long timeOut)
	{
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armiesLeft = state.getStartingArmies();
        int armies = armiesLeft;
        LinkedList<Region> visibleRegions = borderRegions(state);

        Collections.sort(visibleRegions, new Comparator<Region>() {
            private HashMap<Integer, Double> superRegionScores = new HashMap<Integer, Double>();

            private double score(Region r) {
                if (!superRegionScores.containsKey(r.getSuperRegion().getId())) {
                    superRegionScores.put(r.getSuperRegion().getId(), r.getSuperRegion().score(state));
//                    System.err.println("Region "+r.getId()+" is in super region that scores "+r.getSuperRegion().score(state));
                }
                double score = 0;
                if (!state.getMyPlayerName().equals(r.getSuperRegion().ownedByPlayer())) {
                    score += 100*superRegionScores.get(r.getSuperRegion().getId());
                }
                if (r.isBorderTo(state.getOpponentPlayerName())) {
                    score += 10;
                }
                if (r.isBorder()) {
                    score += 1;
                }

                return score;
            }

            @Override
            public int compare(Region o1, Region o2) {
                return Double.compare(score(o2), score(o1));
            }
        });

		while(armiesLeft > 0) {
			Region region = visibleRegions.remove(0);
			if(region.ownedByPlayer(myName)) {
//                System.err.println("Region "+region.getId()+" is in super region that scores "+region.getSuperRegion().score(state));
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

        rebuildRegionLayers(state);

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
//                    System.err.println("Considering "+toRegion.getId()+" which has countNotByOwner of "+toRegion.getSuperRegion().countNotByOwner(myName));

					if(!toRegion.getPlayerName().equals(myName) && shouldAttack(fromRegion, toRegion)) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, howManyToAttackWith(state, fromRegion, toRegion)));
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1
								&& transfers < maxTransfers && toRegion.getLayerNumber() < fromRegion.getLayerNumber()) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, howManyToMoveWith(state, fromRegion, toRegion)));
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

    private int howManyToMoveWith(BotState state, Region fromRegion, Region toRegion) {
        if (fromRegion.getLayerNumber() > 1 || fromRegion.countByOwner(state.getOpponentPlayerName()) <= 1) {
            return fromRegion.getArmies()-1;
        }
        return ((fromRegion.getArmies()+toRegion.getArmies())/2)-fromRegion.getArmies();
    }

    private int howManyToAttackWith(BotState state, Region fromRegion, Region toRegion) {
        if (fromRegion.countByOwner(state.getOpponentPlayerName()) <= 1) {
            return fromRegion.getArmies()-1;
        }
        return (int) Math.min(fromRegion.getArmies() - 1, Math.max(4, toRegion.getArmies() * 2));
    }

    private boolean shouldAttack(Region fromRegion, Region toRegion) {
        return toRegion.getArmies() < 0.8*(fromRegion.getArmies()-1);
    }

    private void rebuildRegionLayers(BotState state) {
        LinkedList<Region> layer = new LinkedList<Region>();

        for(Region region : state.getVisibleMap().getRegionsOwnedBy(state.getMyPlayerName())) {
            if (region.isBorder()) {
                region.setLayerNumber(1);
                layer.add(region);
            } else {
                region.setLayerNumber(Integer.MAX_VALUE);
            }
        }

        while(!layer.isEmpty()) {
            LinkedList<Region> nextLayer = new LinkedList<Region>();
            for(Region region : layer) {
                for(Region neighbour : region.getNeighbors()) {
                    if (neighbour.getPlayerName().equals(state.getMyPlayerName()) && region.getLayerNumber() < neighbour.getLayerNumber()) {
                        neighbour.setLayerNumber(region.getLayerNumber()+1);
                        nextLayer.add(neighbour);
                    }
                }
            }
            layer = nextLayer;
        }
    }

    public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
