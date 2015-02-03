/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package map;
import bot.BotState;

import java.util.LinkedList;

public class SuperRegion {
	
	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	
	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}
	
	public void addSubRegion(Region subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for(Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}

    public int countNotByOwner(String playerName) {
        int countNotOwned = 0;

        for(Region region : subRegions) {
            if (!playerName.equals(region.getPlayerName())) {
                countNotOwned++;
            }
        }

        return countNotOwned;
    }

    public double score(BotState state) {
        int armies = 0;
        for(Region region2 : subRegions) {
            Region region = (state.getVisibleMap() == null ? null : state.getVisibleMap().getRegion(region2.getId()));
            if (region == null) {
                boolean isWasteland = false;
                for(Region wasteland : state.getWasteLands()) {
                    if (wasteland.getId() == region2.getId()) {
                        isWasteland = true;
                        break;
                    }
                }
                if (isWasteland) {
                    armies += 10;
                } else {
                    armies += 2;
                }
            } else if (!region2.getPlayerName().equals(state.getMyPlayerName())) {
//                System.err.println("Region "+region2.getId()+" has armies "+region2.getArmies());
                armies += region2.getArmies();
            }
        }

        double score = armies == 0 ? armiesReward : (double)armiesReward / armies;

//        System.err.println("Super region "+id+" has total armies "+armies+" bonus "+armiesReward+" so score is "+score);

        return score;
    }
}
