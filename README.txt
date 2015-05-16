By: Nicolas Herrera & Charles Farris

*************************************************************

To run in its current state move to the directory containing 
the ctf folder. Then execute the command

java -cp . ctf.environment.TestPlaySurface

*************************************************************

To compile a new agent or alter a current agent then execute 
the following command where AGENTFILE is replaced with which
agent you are compiling. This must also be done in the directory
which contains the ctf folder.

javac -cp . ctf/agent/AGENTFILE.java

*************************************************************

When inside the test environment you may choose which agents
to test against each other, the speed at which the agents move,
and which field to play on. There is also an option for a large
or regular size random field.