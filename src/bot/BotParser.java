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

import java.util.ArrayList;
import java.util.Scanner;

import map.Region;
import move.PlaceArmiesMove;
import move.AttackTransferMove;

public class BotParser {
	
	final Scanner scan;
	
	final Bot bot;
	
	BotState currentState;
    private ArrayList<PlaceArmiesMove> placeArmiesMoves;

    public BotParser(Bot bot)
	{
		this.scan = new Scanner(System.in);
		this.bot = bot;
		this.currentState = new BotState();
	}
	
	public void run()
	{
		while(scan.hasNextLine())
		{
			String line = scan.nextLine().trim();
			if(line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_region")) //pick which regions you want to start with
			{
				currentState.setPickableStartingRegions(parts);
				Region startingRegion = bot.getStartingRegion(currentState, Long.valueOf(parts[1]));
				
				System.out.println(startingRegion.getId());
			}
			else if(parts.length == 3 && parts[0].equals("go")) 
			{
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					//place armies
					placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
					for(PlaceArmiesMove move : placeArmiesMoves)
						output = output.concat(move.getString() + ",");

                    // we are updating the state, but that should be fine because it will be overwritten in the next round
                    for(PlaceArmiesMove move : placeArmiesMoves) {
//                        if (move.getRegion().getId() == 10) System.err.println("Placing at "+move.getRegion().getId()+": "+move.getRegion().getArmies()+" -> "+(move.getRegion().getArmies()+move.getArmies()));
                        move.getRegion().setArmies(move.getRegion().getArmies() + move.getArmies());
//                        if (move.getRegion().getId() == 10) System.err.println("Updated region "+move.getRegion().getId()+" armies = "+move.getRegion().getArmies());
                    }
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					//attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
					for(AttackTransferMove move : attackTransferMoves)
						output = output.concat(move.getString() + ",");
				}
				if(output.length() > 0)
					System.out.println(output);
				else
					System.out.println("No moves");
            } else if (parts[0].equals("Output")) {
                String placement = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
                parts = placement.split(" ");
                System.err.println(placement);
                if (parts.length > 1 && parts[1].equals("place_armies")) {
//                  Output from your bot: "player1 place_armies 7 2,player1 place_armies 7 2,player1 place_armies 9 2,"

                    for(PlaceArmiesMove move : placeArmiesMoves) {
//                        if (move.getRegion().getId() == 10) System.err.println("Replacing at "+move.getRegion()+": "+move.getRegion().getArmies()+" -> "+(move.getRegion().getArmies()-move.getArmies()));

                        move.getRegion().setArmies(move.getRegion().getArmies() - move.getArmies());
//                        if (move.getRegion().getId() == 10) System.err.println("Updated region "+move.getRegion().getId()+" armies = "+move.getRegion().getArmies());
                    }

                    for(int i=2; i<parts.length; i+=3) {
                        int region = Integer.parseInt(parts[i]);
                        int armies = Integer.parseInt(parts[i+1].split(",")[0]);
//                        System.err.println("Place "+armies+" in "+region);

                        Region region1 = currentState.getVisibleMap().getRegion(region);

//                        if (region == 10) System.err.println("Correcting placing at "+region1+": "+region1.getArmies()+" -> "+(region1.getArmies()+armies));

                        region1.setArmies(region1.getArmies()+armies);

//                        if (region == 10) System.err.println("Updated region "+region1.getId()+" armies = "+region1.getArmies());
                    }
                }
			} else if(parts[0].equals("settings")) {
				//update settings
				currentState.updateSettings(parts[1], parts);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				currentState.setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				currentState.updateMap(parts);
			} else if(parts[0].equals("opponent_moves")) {
				//all visible opponent moves are given
				currentState.readOpponentMoves(parts);
			} else {
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}

}
