package ctf.agent;

import ctf.common.AgentEnvironment;
import ctf.agent.Agent;
import ctf.common.AgentAction;
import java.util.ArrayList;

//Copy of ChenAgent

public class nsh100020Agent extends Agent 
{
	boolean firstMove = true;
    boolean foundMapSize;
	int[][] constructedMap;
	int rowIndex;
	int colIndex;
	int previousMove;
	int northOrSouthMult;
	int westOrEastMult;	
    int mapGoalRow;
    int mapGoalCol;
    ArrayList<int[]> checkCycle;

	public void setClassVariables()
	{
        foundMapSize = false;
		previousMove = 0;
		rowIndex = 0;
		colIndex = 0;
		northOrSouthMult = 0;
		westOrEastMult = 0;
		mapGoalCol = -1;
		mapGoalRow = -1;

        // cycle data structure {row location, column location, move taken, cycle detected[0 = false, 1 = true], cycle useful or used to arrive at target location[0 = false, 1 = true]}
        checkCycle = new ArrayList<int[]>();
		constructedMap = new int[100][100];
	}
	
	// implements Agent.getMove() interface
	public int getMove(AgentEnvironment inEnvironment) 
	{
		//Check if this is the first move by the agent to establish the map
		if(firstMove)
		{
			firstMove = false;

			setClassVariables();

			//we are the north agent
			if(inEnvironment.isAgentSouth(inEnvironment.OUR_TEAM, false))
			{
				northOrSouthMult = 1;
			}
			else //else we are the south agent
			{
				northOrSouthMult = -1;
			}
			
			//Here we will determine if we are working with east or west agents
            if(inEnvironment.isFlagEast(inEnvironment.ENEMY_TEAM, false))
            {
            	 westOrEastMult = 1;
            }
            else //else we are the east agents
            {
            	westOrEastMult = -1;
            }

			//Initialize the map
			for(int n = 0; n < 100; n++)
			{
				for(int h = 0; h < 100; h++)
				{
					constructedMap[n][h] = 0;
				}
			}
		}

		int nextMove = AgentAction.DO_NOTHING;
		
		// booleans describing direction of goal
		// goal is either enemy flag, or our base
		boolean goalNorth;
		boolean goalSouth;
		boolean goalEast;
		boolean goalWest;

		//Information for the current flag status
		boolean currentlyHasFlag = inEnvironment.hasFlag();
		boolean enemyHasFlag = inEnvironment.hasFlag(inEnvironment.ENEMY_TEAM);
        boolean attackEnemy = !currentlyHasFlag && inEnvironment.hasFlag(inEnvironment.OUR_TEAM);
        boolean defendFlag = !currentlyHasFlag && enemyHasFlag;

        int enemyAvoidValue = 5;

		//Teammate has flag, go for opponents
		if(attackEnemy || defendFlag)
		{
			goalNorth = inEnvironment.isAgentNorth(inEnvironment.ENEMY_TEAM, false);
			goalSouth = inEnvironment.isAgentSouth(inEnvironment.ENEMY_TEAM, false);
			goalEast = inEnvironment.isAgentEast(inEnvironment.ENEMY_TEAM, false);
			goalWest = inEnvironment.isAgentWest(inEnvironment.ENEMY_TEAM, false);

			enemyAvoidValue = -50;
		}
		else if(!currentlyHasFlag) //Agent does not has flag - set enemy flag as goals
		{
			goalNorth = inEnvironment.isFlagNorth(inEnvironment.ENEMY_TEAM, false);
			goalSouth = inEnvironment.isFlagSouth(inEnvironment.ENEMY_TEAM, false);
			goalEast = inEnvironment.isFlagEast(inEnvironment.ENEMY_TEAM, false);
			goalWest = inEnvironment.isFlagWest(inEnvironment.ENEMY_TEAM, false);
		}
		else //Agent has flag - set base as goals
		{
			goalNorth = inEnvironment.isBaseNorth(inEnvironment.OUR_TEAM, false);
			goalSouth = inEnvironment.isBaseSouth(inEnvironment.OUR_TEAM, false);
			goalEast = inEnvironment.isBaseEast(inEnvironment.OUR_TEAM, false);
			goalWest = inEnvironment.isBaseWest(inEnvironment.OUR_TEAM, false);
		}

		//See if we haven't found the map size yet
		if(!foundMapSize)
        {
            foundMapSize = findMapSize(inEnvironment.isFlagSouth(inEnvironment.ENEMY_TEAM, false), inEnvironment.isFlagNorth(inEnvironment.ENEMY_TEAM, false));

            if(foundMapSize)
            {
                //set base location heref
                mapGoalRow = rowIndex;
            }
        }

        //The mapGoalCol can only be determined if the map size has been found and we know the direction of the goal, this can also change during the game so have it outside the if statement above
        if(foundMapSize)
        {
        	if(goalEast || (colIndex == mapGoalCol && !goalWest)) //Goal is east of us, or we are in the col of the goal
        	{
        		//The goal is at the half way point, but the south agent will get this wrong since it's 0 position is off by 1
        		if(northOrSouthMult == 1)
        		{
        			if(westOrEastMult == 1)
        			{
        				mapGoalCol = (mapGoalRow*2)-1;
        			}
        			else
        			{
        				mapGoalCol = 0;
        			}
        		}
        		else
        		{
        			if(westOrEastMult == 1)
        			{
        				mapGoalCol = ((mapGoalRow+1)*2)-1;
        			}
        			else
        			{
        				mapGoalCol = 0;
        			}
        		}
        	}
        	else //goalWest
        	{
        		if(northOrSouthMult == 1)
        		{
        			if(westOrEastMult == 1)
        			{
        				mapGoalCol = 0;
        			}
        			else
        			{
        				mapGoalCol = (mapGoalRow*2)-1;
        			}
        		}
        		else
        		{
        			if(westOrEastMult == 1)
        			{
        				mapGoalCol = 0;
        			}
        			else
        			{
        				mapGoalCol = ((mapGoalRow+1)*2)-1;
        			}
        		}
        	}

        	//System.out.println("The goal location is [" + mapGoalRow + ", " + mapGoalCol + "]");
        }
		
		// now we have direction boolean for our goal
		
		// check for immediate obstacles blocking our path		
		boolean obstNorth = inEnvironment.isObstacleNorthImmediate();
		boolean obstSouth = inEnvironment.isObstacleSouthImmediate();
		boolean obstEast = inEnvironment.isObstacleEastImmediate();
		boolean obstWest = inEnvironment.isObstacleWestImmediate();
		
		//Boolean to tell if an enemy agent is located next to current agent
		boolean enemyNorth = inEnvironment.isAgentNorth(inEnvironment.ENEMY_TEAM, true);
		boolean enemySouth = inEnvironment.isAgentSouth(inEnvironment.ENEMY_TEAM, true);
		boolean enemyEast = inEnvironment.isAgentEast(inEnvironment.ENEMY_TEAM, true);
		boolean enemyWest = inEnvironment.isAgentWest(inEnvironment.ENEMY_TEAM, true);
        
		
		//Boolean to tell if our teamate is located next to current agent
		boolean teamNorth = inEnvironment.isAgentNorth(inEnvironment.OUR_TEAM, true);
		boolean teamSouth = inEnvironment.isAgentSouth(inEnvironment.OUR_TEAM, true);
		boolean teamEast = inEnvironment.isAgentEast(inEnvironment.OUR_TEAM, true);
		boolean teamWest = inEnvironment.isAgentWest(inEnvironment.OUR_TEAM, true);
        boolean playerNear = false;

		//Hold the values of the spaces around the current position
		double northBlockValue;
		double southBlockValue;
		double eastBlockValue;
		double westBlockValue;

		//Keep track of how many spaces around are blocked
		int deadEndCount = 0;

		//Check to see our position was reset and the row and col needs to be reset as well
        if(northOrSouthMult == 1 && (inEnvironment.isObstacleNorthImmediate() && inEnvironment.isBaseSouth(inEnvironment.OUR_TEAM, false) && !inEnvironment.isBaseEast(inEnvironment.OUR_TEAM, false) && 
        	!inEnvironment.isBaseWest(inEnvironment.OUR_TEAM, false)) && ((inEnvironment.isObstacleWestImmediate() && westOrEastMult == 1) || (inEnvironment.isObstacleEastImmediate() && westOrEastMult == -1)))
        {
            if(checkCycle.size() > 16)
            {
                checkCycle.clear();
            }
			resetPosition();
        }
        else if(northOrSouthMult == -1 && (inEnvironment.isObstacleSouthImmediate() && inEnvironment.isBaseNorth(inEnvironment.OUR_TEAM, false) && !inEnvironment.isBaseEast(inEnvironment.OUR_TEAM, false) && 
        	!inEnvironment.isBaseWest(inEnvironment.OUR_TEAM, false)) && ((inEnvironment.isObstacleWestImmediate() && westOrEastMult == 1) || (inEnvironment.isObstacleEastImmediate() && westOrEastMult == -1)))
        {
            if(checkCycle.size() > 16)
            {
                checkCycle.clear();
            }
        	resetPosition();
        }

        //System.out.println("My current position is [" + rowIndex + ", " + colIndex + "]");
        
		northBlockValue = getValueNorth(northOrSouthMult);
		southBlockValue = getValueSouth(northOrSouthMult);
		eastBlockValue = getValueEast(westOrEastMult);
		westBlockValue = getValueWest(westOrEastMult);
        
        int northBlockValueFromMap = getValueNorth(northOrSouthMult);
		int southBlockValueFromMap = getValueSouth(northOrSouthMult);
		int eastBlockValueFromMap = getValueEast(westOrEastMult);
		int westBlockValueFromMap = getValueWest(westOrEastMult);

		//North
		if((rowIndex != 0 && northOrSouthMult == 1) || (rowIndex != 100 && northOrSouthMult == -1)) //Position won't cause out-of-bounds
		{
			//Check for any bad things and the value to avoid them
			if(enemyNorth)
			{
				northBlockValue += enemyAvoidValue;
			}
			else if(obstNorth) //Obstacle in the way
			{
				northBlockValue = 200;
				deadEndCount++;

				//Try traversing around the obstacle by making the east and west values better
				if(goalEast)
					eastBlockValue--;
				if(goalWest)
					westBlockValue--;
			}
			else if(northBlockValueFromMap == 99)//Deadend from map
			{
				deadEndCount++;
			}
			else if(teamNorth && !currentlyHasFlag) //Teammate is next to me and I don't have the flag, move out of their way
			{
				northBlockValue += 10;
			}

			//Check if the map size has been found, if it has, then we can calculate the manhattan distance
			if(foundMapSize)
			{
				northBlockValue += Math.sqrt(Math.pow((rowIndex-northOrSouthMult)-mapGoalRow, 2)+Math.pow(colIndex-mapGoalCol, 2));
			}

			//Avoid immedietly going to the prevoius position
			if(previousMove == AgentAction.MOVE_SOUTH)
			{
				northBlockValue += 2;
			}

		}
		else //Map edge
		{
			northBlockValue = 200;
			deadEndCount++;
		}
        
		//South
		if((rowIndex != 100 && northOrSouthMult == 1) || (rowIndex != 0 && northOrSouthMult == -1))
		{
			if(enemySouth)
			{
				southBlockValue += enemyAvoidValue;
			}
			else if(obstSouth)
			{
				southBlockValue = 200;
				deadEndCount++;

				if(goalEast)
					eastBlockValue--;
				if(goalWest)
					westBlockValue--;
			}
			else if(southBlockValueFromMap == 99)
			{
				deadEndCount++;
			}
			else if(teamSouth && !currentlyHasFlag)
			{
				southBlockValue += 10;
			}

			if(foundMapSize)
			{
				southBlockValue += Math.sqrt(Math.pow((rowIndex+northOrSouthMult)-mapGoalRow, 2)+Math.pow(colIndex-mapGoalCol, 2));
			}

			if(previousMove == AgentAction.MOVE_NORTH)
			{
				southBlockValue += 2;
			}

		}
		else
		{
			southBlockValue = 200;
			deadEndCount++;
		}

		//East
		if((colIndex != 100 && westOrEastMult == 1) || (colIndex != 0 && westOrEastMult == -1))
		{
			if(enemyEast)
			{
				eastBlockValue += enemyAvoidValue;
			}
			else if(obstEast)
			{
				eastBlockValue = 200;
				deadEndCount++;

				if(goalNorth)
					northBlockValue--;
				if(goalSouth)
					southBlockValue--;
			}
			else if(eastBlockValueFromMap == 99)
			{
				deadEndCount++;
			}
			else if(teamEast && !currentlyHasFlag)
			{
				eastBlockValue += 10;
			}

			if(foundMapSize)
			{
				eastBlockValue += Math.sqrt(Math.pow(rowIndex-mapGoalRow, 2)+Math.pow((colIndex+westOrEastMult)-mapGoalCol, 2));
			}

			//Bias towards east and west
			if(goalEast)
			{
				eastBlockValue -= 1;
			}

			if(previousMove == AgentAction.MOVE_WEST)
			{
				eastBlockValue += 2;
			}

		}
		else
		{
			eastBlockValue = 200;
			deadEndCount++;
		}

		//West
		if((colIndex != 0 && westOrEastMult == 1) || (colIndex != 100 && westOrEastMult == -1))
		{
			if(enemyWest)
			{
				westBlockValue += enemyAvoidValue;
			}
			else if(obstWest)
			{
				westBlockValue = 200;
				deadEndCount++;

				if(goalNorth)
					northBlockValue--;
				if(goalSouth)
					southBlockValue--;
			}
			else if(westBlockValueFromMap == 99)
			{
				deadEndCount++;
			}
			else if(teamEast && !currentlyHasFlag)
			{
				eastBlockValue += 10;
			}

			if(foundMapSize)
			{
				westBlockValue += Math.sqrt(Math.pow(rowIndex-mapGoalRow, 2)+Math.pow((colIndex-westOrEastMult)-mapGoalCol, 2));
			}
			
			if(goalWest)
			{
				westBlockValue -= 1;
			}

			if(previousMove == AgentAction.MOVE_EAST)
			{
				westBlockValue += 2;
			}

		}
		else
		{
			westBlockValue = 200;
			deadEndCount++;
		}

        // Start of the cycle detection
        int[] temp = new int[]{rowIndex, colIndex, nextMove, 0, 0};
        checkCycle.add(temp);
        int indexList = checkCycle.size();
        temp = checkCycle.get(indexList - 1);
        ArrayList<Integer> pastOccurances = new ArrayList<Integer>();
        boolean beenHereBefore = false;
        
        //Check if we have been at this location before
        if(indexList > 3)
        {
            for(int y = 0; y < indexList - 1; y++)
            {
                temp = checkCycle.get(y);

                if(temp[0] == rowIndex && temp[1] == colIndex)
                {
                    //System.out.println("Been Here Before");
                    beenHereBefore = true;
                    pastOccurances.add(y);
                }
            }
        
            boolean isACycle = true;

            // If we have been to this location before then check for a cycle
            if(beenHereBefore == true)
            {
                int pastOccurancesIndex = pastOccurances.get(pastOccurances.size() - 1);
                int cycleSize = indexList - pastOccurancesIndex - 1;
                
                // this fixes some runtime errors where the pastoccuranceindex was smaller than the index size
                if(cycleSize > pastOccurancesIndex)
                {
                    cycleSize = pastOccurancesIndex - 1;
                }
                for(int x = 0; x < cycleSize; x++)
                {
                    int[] compareA = new int[5];
                    int[] compareB = new int[5];

                    compareA = checkCycle.get(pastOccurancesIndex - x);
                    compareB = checkCycle.get(indexList - x - 1);
                    
                    if(compareA[0] != compareB[0] || compareA[1] != compareB[1])
                    {
                        // if one of the previous locations in the cycle test doesn't match then exit the loop and set cycle to false
                        isACycle = false;
                        break;
                    }
                }
                
                // if we are arriving at the same location too many times during a cycle then add a slight bias against moving here again
                if((pastOccurancesIndex >= 3) && (isACycle == true))
                    {
                       constructedMap[rowIndex][colIndex] = 1; 
                    }

                int[] getCycleMove = new int[5];
                getCycleMove = checkCycle.get(pastOccurancesIndex);

                // when a cycle is detected then avoid the direction then that of the previous attempt at navigation
                if(isACycle == true)
                {
                    if(getCycleMove[2] == 0)
                    {
                        northBlockValue += 50;
                    }
                    else if(getCycleMove[2] == 1)
                    {
                        southBlockValue += 50;
                    }
                    else if(getCycleMove[2] == 2)
                    {
                        eastBlockValue += 50;
                    }
                    else if(getCycleMove[2] == 3)
                    {
                        westBlockValue += 50;
                    }
                    // If this is the second time there is the same cycle then we will try another option
                    if(getCycleMove[3] == 0)
                    {
                        northBlockValue += 50;
                    }
                    else if(getCycleMove[3] == 1)
                    {
                        southBlockValue += 50;
                    }
                    else if(getCycleMove[3] == 2)
                    {
                        eastBlockValue += 50;
                    }
                    else if(getCycleMove[3] == 3)
                    {
                        westBlockValue += 50;
                    }
                }
                //When cycle detected then direction of the previous move taken will be set to unfavourable 
            }
        }
        
		nextMove = successorFunction(northBlockValue, southBlockValue, eastBlockValue, westBlockValue, goalEast, goalWest);

		//Save all the current moves to the previous storage
		previousMove = nextMove;
        
        //update checkCycle with the chosen next move
        int[] tempNext;
        checkCycle.remove(checkCycle.size() - 1);
        if((northBlockValue >= 50) && (northBlockValue <= 55))
        {
            tempNext = new int[]{rowIndex, colIndex, nextMove, 0, 0};
        }
        else if((southBlockValue >= 50) && (southBlockValue <= 55))
        {
            tempNext = new int[]{rowIndex, colIndex, nextMove, 1, 0};
        }
        else if((eastBlockValue >= 50) && (eastBlockValue <= 55))
        {
            tempNext = new int[]{rowIndex, colIndex, nextMove, 2, 0};
        }
        else if((westBlockValue >= 50) && (westBlockValue <= 55))
        {
            tempNext = new int[]{rowIndex, colIndex, nextMove, 3, 0};
        }
        else
        {
            tempNext = new int[]{rowIndex, colIndex, nextMove, -1, 0};
        }

        checkCycle.add(tempNext);

		//Check to see if we were in a dead end
		if(deadEndCount == 3)
		{
			constructedMap[rowIndex][colIndex] = 99;
		}

		//System.out.println("My current position is [" + rowIndex + ", " + colIndex + "]");

		switch(nextMove)
		{
			case AgentAction.MOVE_NORTH:
				//I have the flag but my teammate is in my way, wait for them to leave
				if(teamNorth && currentlyHasFlag)
				{
					nextMove = AgentAction.DO_NOTHING;
				}
				else //Position is fine
				{
					//System.out.println("N");
					rowIndex -= northOrSouthMult;
				}

				break;
			case AgentAction.MOVE_SOUTH:
				if(teamSouth && currentlyHasFlag)
				{
					nextMove = AgentAction.DO_NOTHING;
				}
				else
				{
					//System.out.println("S");
					rowIndex += northOrSouthMult;
				}
				
				break;
			case AgentAction.MOVE_WEST:
				if(teamWest && currentlyHasFlag)
				{
					nextMove = AgentAction.DO_NOTHING;
				}
				else
				{
					//System.out.println("W");
					colIndex -= westOrEastMult;
				}

				break;
			case AgentAction.MOVE_EAST:
				if(teamEast && currentlyHasFlag)
				{
					nextMove = AgentAction.DO_NOTHING;
				}
				else
				{
					//System.out.println("E");
					colIndex += westOrEastMult;
				}				

				break;
			case AgentAction.DO_NOTHING:
				//Do nothing
				break;
		}

		//printMap();

		return nextMove;
	}
	
	public int successorFunction(double northBlockValue, double southBlockValue, double eastBlockValue, double westBlockValue, boolean goalEast, boolean goalWest)
	{
		double[] successors = new double[4];
		
		//Row, Col of current, look at all cardinal directions, get their value from the constructed map
		successors[AgentAction.MOVE_NORTH] = northBlockValue;
		successors[AgentAction.MOVE_SOUTH] = southBlockValue;
		successors[AgentAction.MOVE_EAST] = eastBlockValue;
		successors[AgentAction.MOVE_WEST] = westBlockValue;
		
        /*
		System.out.println("North: " + successors[AgentAction.MOVE_NORTH]);
		System.out.println("South: " + successors[AgentAction.MOVE_SOUTH]);
		System.out.println("East: " + successors[AgentAction.MOVE_EAST]);
		System.out.println("West: " + successors[AgentAction.MOVE_WEST]);
        */
        
		double min = 200;
		int lowestPositions = -1;
		
		for(int n = 0; n < 4; n++)
		{
			if(successors[n] < min)
			{
				min = successors[n];
				lowestPositions = n;
				//System.out.println("New min " + min + " at " + n);
			}
		}

		//System.out.println("Position: " + lowestPositions);
		return lowestPositions;
	}

	public int getValueNorth(int nOrS)
	{
		int t;

		if((rowIndex != 0 && nOrS == 1) || (rowIndex != 100 && nOrS == -1)) //The value of the space north
		{
			int n = rowIndex - nOrS;
			t = constructedMap[n][colIndex];
		}
		else //Map edge
			t = 100;
		
		return t;
	}

	public void updateValueNorth(int value, int nOrS)
	{
		if((rowIndex != 0 && nOrS == 1) || (rowIndex != 100 && nOrS == -1)) //The value of the space north
		{
			int n = rowIndex - nOrS;
			constructedMap[n][colIndex] = value;
		}
	}

	public int getValueSouth(int nOrS)
	{
		int t;
		
		if((rowIndex != 100 && nOrS == 1) || (rowIndex != 0 && nOrS == -1))
		{
			int n = rowIndex + nOrS;
			t = constructedMap[n][colIndex];
		}
		else //Map edge
			t = 100;
		
		return t;
	}

	public void updateValueSouth(int value, int nOrS)
	{
		if((rowIndex != 100 && nOrS == 1) || (rowIndex != 0 && nOrS == -1))
		{
			int n = rowIndex + nOrS;
			constructedMap[n][colIndex] = value;
		}
	}

	public int getValueEast(int eOrW)
	{
		int t;
		
		if((colIndex != 100 && eOrW == 1) || (colIndex != 0 && eOrW == -1))
		{
			int n = colIndex + eOrW;
			t = constructedMap[rowIndex][n];
		}
		else //Map edge
			t = 100;
		
		return t;
	}

	public void updateValueEast(int value, int eOrW)
	{
		if((colIndex != 100 && eOrW == 1) || (colIndex != 0 && eOrW == -1))
		{
			int n = colIndex + eOrW;
			constructedMap[rowIndex][n] = value;
		}
	}

	public int getValueWest(int eOrW)
	{
		int t;
		
		if((colIndex != 0 && eOrW == 1) || (colIndex != 100 && eOrW == -1))
		{
			int n = colIndex - eOrW;
			t = constructedMap[rowIndex][n];
		}
		else //Map edge
			t = 100;
		
		return t;
	}

	public void updateValueWest(int value, int eOrW)
	{
		if((colIndex != 0 && eOrW == 1) || (colIndex != 100 && eOrW == -1))
		{
			int n = colIndex - eOrW;
			constructedMap[rowIndex][n] = value;
		}
	}

    public boolean findMapSize(boolean isNorth, boolean isSouth)
    {
        if(!isNorth && !isSouth)
        {
            return true;    
        }

        return false;
    }
    
	public void printMap()
	{
		System.out.print("==========================================================================\n");

		for(int n = 0; n < 10; n++)
		{
			for(int h = 0; h < 10; h++)
			{
				System.out.print(constructedMap[n][h] + "\t");
			}
			
			System.out.print("\n");
		}

		System.out.print("==========================================================================\n");
	}

	public void resetPosition()
	{
        rowIndex = 0;
        colIndex = 0;
	}
}