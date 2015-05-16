package ctf.agent;

import ctf.common.AgentEnvironment;
import ctf.agent.Agent;
import ctf.common.AgentAction;
import java.util.ArrayList;

//Copy of ChenAgent

public class testAgent extends Agent 
{
	boolean firstMove = true;
	boolean previousEnemyNorth;
	boolean previousEnemySouth;
	boolean previousEnemyEast;
	boolean previousEnemyWest;
	boolean previousFlag;

	public void setClassVariables()
	{
		previousEnemyNorth = false;
		previousEnemySouth = false;
		previousEnemyEast = false;
		previousEnemyWest = false;
		previousFlag = false;
		previousMove = 0;
	}
	
	// implements Agent.getMove() interface
	public int getMove(AgentEnvironment inEnvironment) 
	{
		//Check if this is the first move by the agent to establish the map
		if(firstMove)
		{
			setClassVariables();
			
			firstMove = false;
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
        
		//Agent does not has flag - set enemy flag asgoals
		if(!currentlyHasFlag) 
		{
			goalNorth = inEnvironment.isFlagNorth(inEnvironment.ENEMY_TEAM, false);
			goalSouth = inEnvironment.isFlagSouth(inEnvironment.ENEMY_TEAM, false);
			goalEast = inEnvironment.isFlagEast(inEnvironment.ENEMY_TEAM, false);
			goalWest = inEnvironment.isFlagWest(inEnvironment.ENEMY_TEAM, false);
		}
		else if(inEnvironment.hasFlag(inEnvironment.OUR_TEAM) && !currentlyHasFlag) //Teammate has flag, go for opponents
		{
			goalNorth = inEnvironment.isAgentNorth(inEnvironment.ENEMY_TEAM, false);
			goalSouth = inEnvironment.isAgentSouth(inEnvironment.ENEMY_TEAM, false);
			goalEast = inEnvironment.isAgentEast(inEnvironment.ENEMY_TEAM, false);
			goalWest = inEnvironment.isAgentWest(inEnvironment.ENEMY_TEAM, false);
		}
		else //Agent has flag - set base as goals
		{
			goalNorth = inEnvironment.isBaseNorth(inEnvironment.OUR_TEAM, false);
			goalSouth = inEnvironment.isBaseSouth(inEnvironment.OUR_TEAM, false);
			goalEast = inEnvironment.isBaseEast(inEnvironment.OUR_TEAM, false);
			goalWest = inEnvironment.isBaseWest(inEnvironment.OUR_TEAM, false);
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
		
		//Hold the values of the spaces around the current position
		int northBlockValue;
		int southBlockValue;
		int eastBlockValue;
		int westBlockValue;

		//Keep track of how many spaces around are blocked
		int deadEndCount = 0;
        
		northBlockValue = getValueNorth(northOrSouthMult);
		System.out.println("North " + northBlockValue);
		southBlockValue = getValueSouth(northOrSouthMult);
		System.out.println("South " + southBlockValue);
		eastBlockValue = getValueEast(westOrEastMult);
		System.out.println("East " + eastBlockValue);
		westBlockValue = getValueWest(westOrEastMult);
		System.out.println("West " + westBlockValue);

        int enemyAvoidValue = 1;

        if(enemyHasFlag && !(inEnvironment.hasFlag())) //The enemy has the flag and I don't, take them out!
			enemyAvoidValue = -50;

		//Check for any bad things and update the map to avoid them
		//North
		if(enemyNorth)
		{
			northBlockValue += enemyAvoidValue;
		}
		else if(obstNorth)
		{
			northBlockValue = 100;
			deadEndCount++;
		}
		else //position looks promising
		{
			northBlockValue += (int)Math.sqrt(Math.pow((rowIndex-northOrSouthMult)-mapGoalRow, 2)+Math.pow(colIndex-mapGoalCol, 2));

			if(previousMove == AgentAction.MOVE_SOUTH)
			{
				northBlockValue++;
			}
		}
        
		//South
		if((obstSouth || enemySouth || southBlockValue == 99) && ((rowIndex != 100 && northOrSouthMult == 1) || (rowIndex != 0 && northOrSouthMult == -1)))
		{
			//System.out.println("Avoid south");

			if(enemySouth)
			{
				southBlockValue += enemyAvoidValue;
			}
			else if(obstSouth)
			{
				southBlockValue = 100;
				updateValueSouth(southBlockValue, northOrSouthMult);
				deadEndCount++;
			}
			else
			{
				deadEndCount++;
			}
		}
		else
		{
			if((rowIndex != 100 && northOrSouthMult == 1) || (rowIndex != 0 && northOrSouthMult == -1))
			{
				if(foundMapSize)
				{
					southBlockValue += (int)Math.sqrt(Math.pow((rowIndex+northOrSouthMult)-mapGoalRow, 2)+Math.pow(colIndex-mapGoalCol, 2));
				}

				if(previousMove == AgentAction.MOVE_NORTH)
				{
					southBlockValue++;
				}

				/*if(goalSouth)
				{
					if(southBlockValue > 0)
					{
						southBlockValue = 0;
						updateValueSouth(southBlockValue, northOrSouthMult);
					}
					else
						southBlockValue--;
				}*/
			}
			else
			{
				southBlockValue = 100;
				deadEndCount++;
			}
		}

		//East
		if((obstEast || enemyEast || eastBlockValue == 99) && ((colIndex != 100 && westOrEastMult == 1) || (colIndex != 0 && westOrEastMult == -1)))
		{
			//System.out.println("Avoid east");

			if(enemyEast)
			{
				eastBlockValue += enemyAvoidValue;
			}
			else if(obstEast)
			{
				eastBlockValue = 100;
				updateValueEast(eastBlockValue, westOrEastMult);
				deadEndCount++;
			}
			else
			{
				deadEndCount++;
			}
		}
		else
		{
			if((colIndex != 100 && westOrEastMult == 1) || (colIndex != 0 && westOrEastMult == -1))
			{
				if(foundMapSize)
				{
					eastBlockValue += (int)Math.sqrt(Math.pow(rowIndex-mapGoalRow, 2)+Math.pow((colIndex+westOrEastMult)-mapGoalCol, 2));
				}
				else if(goalEast)
				{
					eastBlockValue-=2;
				}

				if(previousMove == AgentAction.MOVE_WEST)
				{
					eastBlockValue++;
				}

				/*if(goalEast)
				{
					if(eastBlockValue > 0)
					{
						eastBlockValue = 0;
						updateValueEast(eastBlockValue, westOrEastMult);
					}
					else
						eastBlockValue -= 2;
				}*/
			}
			else
			{
				eastBlockValue = 100;
				deadEndCount++;
			}
		}

		//West
		if((obstWest || enemyWest || westBlockValue == 99) && ((colIndex != 0 && westOrEastMult == 1) || (colIndex != 100 && westOrEastMult == -1)))
		{
			//System.out.println("Avoid west");

			if(enemyWest)
			{
				westBlockValue += enemyAvoidValue;
			}
			else if(obstWest)
			{
				westBlockValue = 100;
				updateValueWest(westBlockValue, westOrEastMult);
				deadEndCount++;
			}
			else
			{
				deadEndCount++;
			}
		}
		else
		{
			if((colIndex != 0 && westOrEastMult == 1) || (colIndex != 100 && westOrEastMult == -1))
			{
				if(foundMapSize)
				{
					westBlockValue += (int)Math.sqrt(Math.pow(rowIndex-mapGoalRow, 2)+Math.pow((colIndex-westOrEastMult)-mapGoalCol, 2));
				}
				else if(goalWest)
				{
					westBlockValue-=2;
				}

				if(previousMove == AgentAction.MOVE_EAST)
				{
					westBlockValue++;
				}

				/*if(goalWest)
				{
					if(westBlockValue > 0)
					{
						westBlockValue = 0;
						updateValueWest(westBlockValue, westOrEastMult);
					}
					else
						westBlockValue -= 2;
				}*/
			}
			else
			{
				westBlockValue = 100;
				deadEndCount++;
			}
		}
        
		nextMove = successorFunction(northBlockValue, southBlockValue, eastBlockValue, westBlockValue, goalEast, goalWest, northOrSouthMult);

		previousEnemyNorth = enemyNorth;
		previousEnemySouth = enemySouth;
		previousEnemyEast = enemyEast;
		previousEnemyWest = enemyWest;

		previousFlag = currentlyHasFlag;

		previousMove = nextMove;

		previousRow = rowIndex;
		previousCol = colIndex;

		//Check to see if we were in a dead end
		if(deadEndCount == 3)
		{
			constructedMap[rowIndex][colIndex] = 99;
		}
		else if(deadEndCount == 4)
		{
			//Somehow, we're stuck and cannot do anything
			nextMove = AgentAction.DO_NOTHING;
		}

		//System.out.println("My current position is [" + rowIndex + ", " + colIndex + "]");

		switch(nextMove)
		{
			case AgentAction.MOVE_NORTH:
				System.out.println("I selected to move North");

				rowIndex -= northOrSouthMult;

				break;
			case AgentAction.MOVE_SOUTH:
				System.out.println("I selected to move South");

				rowIndex += northOrSouthMult;

				break;
			case AgentAction.MOVE_WEST:
				System.out.println("I selected to move West");

				colIndex -= westOrEastMult;

				break;
			case AgentAction.MOVE_EAST:
				System.out.println("I selected to move East");

				colIndex += westOrEastMult;

				break;
			case AgentAction.DO_NOTHING:
				System.out.println("I selected to do nothing");
				//Do nothing
				break;
		}

		printMap();

		return nextMove;
	}
	
	public int successorFunction(int northBlockValue, int southBlockValue, int eastBlockValue, int westBlockValue, boolean goalEast, boolean goalWest, int nOrSMult)
	{
		int[] successors = new int[4];
		
		//Row, Col of current, look at all cardinal directions, get their value from the constructed map
		successors[AgentAction.MOVE_NORTH] = northBlockValue;
		successors[AgentAction.MOVE_SOUTH] = southBlockValue;
		successors[AgentAction.MOVE_EAST] = eastBlockValue;
		successors[AgentAction.MOVE_WEST] = westBlockValue;
		
		System.out.println("North: " + successors[AgentAction.MOVE_NORTH]);
		System.out.println("South: " + successors[AgentAction.MOVE_SOUTH]);
		System.out.println("East: " + successors[AgentAction.MOVE_EAST]);
		System.out.println("West: " + successors[AgentAction.MOVE_WEST]);

		int min = 200;
		int lowestPositions = -1;
		
		for(int n = 0; n < 4; n++)
		{
			//If you're south agent, have a bias for going north, vice versa 
			if(successors[n] < min)
			{
				min = successors[n];
				lowestPositions = n;
				System.out.println("New min " + min + " at " + n);
			}
			else if(successors[n] == min && nOrSMult == 1)
			{
				min = successors[n];
				lowestPositions = n;
				System.out.println("New min " + min + " at " + n);
			}
		}

		System.out.println("Position: " + lowestPositions);
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
}