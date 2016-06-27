# blackjack
Implementation of a blackjack distributed game coded in scala


Steps to run the Blackjack game.

1.	Add the PlayerClient project to the BlackJackGameServer project.
2.	Start the game server by launching the StartCasino application in EyesInTheSkyUI package in Blackjack game server.
3.	 Select Start Casino option from the options menu. This starts the casino and its tables. These tables are displayed on the screen.
4.	Start the PlayerClient by running the PlayerPlaceBets application. 
5.	Enter the details of the player like name, bankroll and bet amount appropriately and click on Bet button to enter the casino and start the game.
6.	PlayerBJScreen pops up on successful entry into the casino.
7.	The Player can play the blackjack along with 2 more bot players spawned in the server. 
8.	The player can formulate his strategy at his turn by using Hit, Surrender, Stay, Double Down options.
9.	He can repeatedly play as many games as he likes till his bankroll reaches below the minimum.
10.	The bot players play their game in accordance to the Basic Strategy rules available in the GameServer    
