package message

import collection.mutable.HashMap
import card.Hand


case class GameProgress(gamePlayData : GamePlayData)

case class GamePlayData {

  var tid = 0
  var humanPlayer = new player
  var ronBot = new player
  var sandBot = new player
  var dealer = new dealer
  
}

case class player{
  
  var pid = -1
  var bankroll = 0.0
  var betAmount = 0.0
  var handValue = 0
  var cards = Array(" "," "," "," "," ")
  var result = " "
  var amountWon = 0.0 
  var newBankroll = 0.0  
  
}


case class dealer{
  
  var handValue = 0
  var cards = Array(" "," "," "," "," ")
  
  
}