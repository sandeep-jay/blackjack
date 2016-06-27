//Copyright (C) 2011 Ron Coleman. Contact: ronncoleman@gmail.com
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either
//version 3 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

package bj.actor
import scala.actors.Actor
import Actor._
import scala.actors.remote.RemoteActor.{ alive, register }
import scala.actors.OutputChannel
import bj.card.Hand
import bj.hkeeping.Ok
import bj.card.Card
import bj.util.Logs
import bj.hkeeping.Broke
import scala.collection.mutable.HashMap

case class StratergyReceived(request: Request, tid: Int)
case class ResultsData(pid: Int, result: String, wonAmount: Double, newBankroll: Double)
/** This object represents the player's class variables */
object Player {
  /** Unique id counter for players */
  var id: Int = -1

  /** Creates, starts, and send the players on their way.  */
  def start(players: List[Player]) {
    players.foreach { p =>
      p.start

      p ! Go
    }
  
  }
  def stop(players: List[Player]){
  	
    players.drop(players.size)
    
      
    
  }
  
  
}

/**
 * This class implements the functionality of a player.
 * @param name Name of the player
 * @param bankroll Bankroll of the player to start
 * @param betAmt Minimum amount player will bet
 */
class Player(name: String, var bankroll: Double, var betAmt: Double, var realPlayerflag: Boolean) extends Actor with Hand with Logs {
  /**
   * Get the player's unique id
   * Note: this assumes players are constructed serially!
   */
  Player.id += 1
  var pid = Player.id

  /** Dealer's up-card */
  var upcard: Card = _

  /** Table id I've been assigned */
  var tableId: Int = -1

  //var isDoubleDown = false

  /** Pretty-prints the player reference */
  override def toString: String = "(" + name + ", " + pid + ")"

  /** This method receives messages */
  def act {
    loop {
      react {
        // Receives message to tell player to place its bet
        case Go =>
          debug(this + " received Go placing bet = " + betAmt + " from bankroll = " + bankroll)
          bet

        // Receives the dealer's up-card which is player's cue to play
        case Up(card, handValue) =>
          debug(this + " received dealer's up card = " + card)
          GameServerStarter.gameServer ! Up(card, handValue)

          
          Thread.sleep(1000)
          play(card)

        // Receives a card from the dealer
        case card: Card =>
          hitMe(card)

        case DoubleDownStratergy =>

          //isDoubleDown = true
          betAmt = betAmt + betAmt
          //isDoubleDown = false
        // Receives broke message
          
        //case SurrenderStrategy =>
          
        case Broke =>
          debug(this + " received BROKE")

        // Receives message about dealt card
        case Observe(card, player, handSize, handValue) =>

          GameServerStarter.gameServer ! Observe(card, player, handSize, handValue)

          Thread.sleep(1000)

          debug(this + " observed: " + card)
        //observe(card,player,shoeSize)

        // Receives the table number I've been assigned to
        case TableNumber(tid: Int) =>
          debug(this + " received table assignment tid = " + tid)
          assign(tid)
          if(this.realPlayerflag){
            GameServerStarter.gameServer ! HumanPlayerActor
          }
          
        case Win(gain) =>
          val won = betAmt * gain
          bankroll += won

          GameServerStarter.gameServer ! ResultsData(pid, "Won", won, bankroll)
          Thread.sleep(1000)
          debug(this + " received WIN " + won + " new bankroll = " + bankroll)
          clear

        case Loose(gain) =>
          val lost = betAmt * gain

          bankroll += lost
          GameServerStarter.gameServer ! ResultsData(pid, "Lost", lost, bankroll)
          Thread.sleep(1000)
          debug(this + " received LOOSE " + lost + " new bankroll = " + bankroll)
          clear
        case Push(gain) =>

          GameServerStarter.gameServer ! ResultsData(pid, "Push", 0, bankroll)
          Thread.sleep(1000)
          debug(this + " received PUSH bankroll = " + bankroll)
          clear
        // Receives an ACK
        case Ok =>
          debug(this + " received Ok")

        case StratergyReceived(request, tid) =>
          debug("Received human stratergy. Forwarding it to dealer")
          receivedHumanRequest(request, tid)

        // Receives something completely from left field
        case dontKnow =>
          // Got something we REALLY didn't expect
          debug(this + " received unexpected: " + dontKnow)
      }
    }

  }

  /**
   * Processes table numbers.
   * @param tid Table id
   */
  def assign(tid: Int) {

    //GameServerStarter.gameServer ! TableData(tid)
    //println("In assign")
    GameServerStarter.gameServer.receivedTable(tid, this.pid)

  }

  def receivedHumanRequest(request: Request, tid: Int) {

    House.tables(tid).dealer ! request

  }

  /**
   * Observes a card being dealt.
   * Note: This method needs to be overridden if counting cards.
   * @param card Card the player received
   * @param player Player id receiving this card
   * @param size Shoe size
   */
  def observe(card: Card, player: Int, size: Int) {

  }

  /**
   * Processes hit request.
   * @param dealer Dealer's mailbox
   * @param upcard Dealer's up-card
   */
  def hitMe(card: Card) {
    // Hit my hand with this card
    this.hit(card)

    GameServerStarter.gameServer ! CardDealt(card, pid, cards.size, value, bankroll, betAmt)
    Thread.sleep(4000)
    

    debug(this + " received card " + card + " hand sz = " + cards.size + " value = " + value)

    // If I've received more than two cards, the extras must be in
    // response to my requests
    if (cards.size > 2)
      play(this.upcard)
  }

  /** Places a bet with the house */
  def bet {
    if (bankroll < betAmt)
      return
    House ! Bet(pid, betAmt)
  }

  /**
   * Processes the dealer's upcard.
   * @param dealer Dealer's mailbox
   * @param upcard Dealer's up-card
   */
  def play(upcard: Card) {
    this.upcard = upcard
    var request = new Request()
    // get human player strategy
    if (this.realPlayerflag) {
      debug("Go get human statergy")
      GameServerStarter.gameServer.requestStratergy(this.pid, this.tableId)
      GameServerStarter.gameServer ! HumanPlayerActor

    } else {
      // Compute my play strategy
      request = analyze(upcard)
      
      debug(this + " request = " + request)
      // Don't send a request if we break since
      // deal will have moved on
      if (!this.broke)
        sender ! request
    }

  }

 // Clears player objects
    def clear: Unit = {
    clearHand
    upcard = null
    tableId = -1
    //isDoubleDown = false
    Player.id += -1
    pid = -1
    }
    
   // Hash Map containing Hard total strategy 
     val hardTotalStrategy = HashMap[(String, String), String](
    		 //("8", "2") -> "Hit"          , ("8", "3") -> "Hit"         , ("8", "4") -> "Hit"         , ("8", "5") -> "Hit"          , ("8", "6") -> "Hit"         , ("8", "7") -> "Hit"         , ("8", "8") -> "Hit"          , ("8", "9") -> "Hit"          , ("8", "10") -> "Hit"          , ("8", "A") -> "Hit",
    		 ("9", "2") -> "Hit"          , ("9", "3") -> "DoubleDown"  , ("9", "4") -> "DoubleDown"  , ("9", "5") -> "DoubleDown"   , ("9", "6") -> "DoubleDown"  , ("9", "7") -> "Hit"         , ("9", "8") -> "Hit"          , ("9", "9") -> "Hit"          , ("9", "10") -> "Hit"          , ("9", "A") -> "Hit",
    		 ("10", "2") -> "DoubleDown"  , ("10", "3") -> "DoubleDown" , ("10", "4") -> "DoubleDown" , ("10", "5") -> "DoubleDown"  , ("10", "6") -> "DoubleDown" , ("10", "7") -> "DoubleDown" , ("10", "8") -> "DoubleDown"  , ("10", "9") -> "DoubleDown"  , ("10", "10") -> "Hit"         , ("10", "A") -> "Hit",
    		 ("11", "2") -> "DoubleDown"  , ("11", "3") -> "DoubleDown" , ("11", "4") -> "DoubleDown" , ("11", "5") -> "DoubleDown"  , ("11", "6") -> "DoubleDown" , ("11", "7") -> "DoubleDown" , ("11", "8") -> "DoubleDown"  , ("11", "9") -> "DoubleDown"  , ("11", "10") -> "DoubleDown"  , ("11", "A") -> "Hit",
    		 ("12", "2") -> "Hit"         , ("12", "3") -> "Hit"        , ("12", "4") -> "Stay"       , ("12", "5") -> "Stay"        , ("12", "6") -> "Stay"       , ("12", "7") -> "Hit"        , ("12", "8") -> "Hit"         , ("12", "9") -> "Hit"         , ("12", "10") -> "Hit"         , ("12", "A") -> "Hit",
    		 ("13", "2") -> "Stay"        , ("13", "3") -> "Stay"       , ("13", "4") -> "Stay"       , ("13", "5") -> "Stay"        , ("13", "6") -> "Stay"       , ("13", "7") -> "Hit"        , ("13", "8") -> "Hit"         , ("13", "9") -> "Hit"         , ("13", "10") -> "Hit"         , ("13", "A") -> "Hit",
    		 ("14", "2") -> "Stay"        , ("14", "3") -> "Stay"       , ("14", "4") -> "Stay"       , ("14", "5") -> "Stay"        , ("14", "6") -> "Stay"       , ("14", "7") -> "Hit"        , ("14", "8") -> "Hit"         , ("14", "9") -> "Hit"         , ("14", "10") -> "Hit"         , ("14", "A") -> "Hit",
    		 ("15", "2") -> "Stay"        , ("15", "3") -> "Stay"       , ("15", "4") -> "Stay"       , ("15", "5") -> "Stay"        , ("15", "6") -> "Stay"       , ("15", "7") -> "Hit"        , ("15", "8") -> "Hit"         , ("15", "9") -> "Hit"         , ("15", "10") -> "Surrender"   , ("15", "A") -> "Hit",
    		 ("16", "2") -> "Stay"        , ("16", "3") -> "Stay"       , ("16", "4") -> "Stay"       , ("16", "5") -> "Stay"        , ("16", "6") -> "Stay"       , ("16", "7") -> "Hit"        , ("16", "8") -> "Hit"         , ("16", "9") -> "Hit"         , ("16", "10") -> "Surrender"   , ("16", "A") -> "Surrender",
    		 ("17", "2") -> "Stay"        , ("17", "3") -> "Stay"       , ("17", "4") -> "Stay"       , ("17", "5") -> "Stay"        , ("17", "6") -> "Stay"       , ("17", "7") -> "Stay"       , ("17", "8") -> "Stay"        , ("17", "9") -> "Stay"        , ("17", "10") -> "Stay"        , ("17", "A") -> "Stay")
    
    // Hash map containing Soft Total strategy
    val softTotalStrategy = HashMap[(String, String), String](
     
             ("A,2", "2") -> "Hit"        , ("A,2", "3") -> "Hit"		 , ("A,2", "4") -> "Hit"	    , ("A,2", "5") -> "DoubleDown"   , ("A,2", "6") -> "DoubleDown"		, ("A,2", "7") -> "Hit"		, ("A,2", "8") -> "Hit"		, ("A,2", "9") -> "Hit"		, ("A,2", "10") -> "Hit"		, ("A,2", "A") -> "Hit",
             ("A,3", "2") -> "Hit"		  , ("A,3", "3") -> "Hit"		 , ("A,3", "4") -> "Hit"		, ("A,3", "5") -> "DoubleDown"	 , ("A,3", "6") -> "DoubleDown"		, ("A,3", "7") -> "Hit"		, ("A,3", "8") -> "Hit"		, ("A,3", "9") -> "Hit"		, ("A,3", "10") -> "Hit"		, ("A,3", "A") -> "Hit",
             ("A,4", "2") -> "Hit"		  , ("A,4", "3") -> "Hit"		 , ("A,4", "4") -> "DoubleDown" , ("A,4", "5") -> "DoubleDown"	 , ("A,4", "6") -> "DoubleDown"		, ("A,4", "7") -> "Hit"		, ("A,4", "8") -> "Hit"		, ("A,4", "9") -> "Hit"		, ("A,4", "10") -> "Hit"		, ("A,4", "A") -> "Hit",
             ("A,5", "2") -> "Hit"		  , ("A,5", "3") -> "Hit"		 , ("A,5", "4") -> "DoubleDown" , ("A,5", "5") -> "DoubleDown"	 , ("A,5", "6") -> "DoubleDown"		, ("A,5", "7") -> "Hit"		, ("A,5", "8") -> "Hit"		, ("A,5", "9") -> "Hit"		, ("A,5", "10") -> "Hit"		, ("A,5", "A") -> "Hit",
             ("A,6", "2") -> "Hit"		  , ("A,6", "3") -> "DoubleDown" , ("A,6", "4") -> "DoubleDown" , ("A,6", "5") -> "DoubleDown"	 , ("A,6", "6") -> "DoubleDown"		, ("A,6", "7") -> "Hit"		, ("A,6", "8") -> "Hit"		, ("A,6", "9") -> "Hit"		, ("A,6", "10") -> "Hit"		, ("A,6", "A") -> "Hit",
             ("A,7", "2") -> "Stay"		  , ("A,7", "3") -> "DoubleDown" , ("A,7", "4") -> "DoubleDown" , ("A,7", "5") -> "DoubleDown"	 , ("A,7", "6") -> "DoubleDown"		, ("A,7", "7") -> "Stay"	, ("A,7", "8") -> "Stay"	, ("A,7", "9") -> "Hit"		, ("A,7", "10") -> "Hit"		, ("A,7", "A") -> "Hit",
             ("A,8", "2") -> "Stay"		  , ("A,8", "3") -> "Stay"		 , ("A,8", "4") -> "Stay"		, ("A,8", "5") -> "Stay"	     , ("A,8", "6") -> "Stay"			, ("A,8", "7") -> "Stay"	, ("A,8", "8") -> "Stay"	, ("A,8", "9") -> "Stay"	, ("A,8", "10") -> "Stay"		, ("A,8", "A") -> "Stay",
             ("A,9", "2") -> "Stay"		  , ("A,9", "3") -> "Stay"		 , ("A,9", "4") -> "Stay"		, ("A,9", "5") -> "Stay"		 , ("A,9", "6") -> "Stay"			, ("A,9", "7") -> "Stay"	, ("A,9", "8") -> "Stay"	, ("A,9", "9") -> "Stay"	, ("A,9", "10") -> "Stay"		, ("A,9", "A") -> "Stay")  
    
  /** Analyzes my best play using a condensed form of Basic Strategy. */
  def analyze(upcard: Card): Request = {
       
       var botStratergy = "Hit"
       var searchkey: (String, String) = null
       var request: Request = null
       
       // Checks for aces and if only 2 cards are dealt. Creates search key accordingly to retrieve strategy.
       if (cards.size == 2 && (cards(0).ace == true || cards(1).ace == true || cards(0).value == cards(1).value)) {
    	   if (cards(0).ace == true)
    		   searchkey = (  "A," + cards(1).value.toString(), upcard.value.toString())
    	   else if (cards(1).ace == true)
    		   searchkey = ( "A," + cards(0).value.toString(), upcard.value.toString())
    		   
    		   debug(this + " generated " + searchkey)
    	   if (softTotalStrategy.keySet.contains(searchkey))
    		   botStratergy = softTotalStrategy(searchkey)
    }
    // Gets strategy using hard total irrespective of the no of cards in the hand
    // Takes only hand value and dealer upcard to search key.   
    else {
      if (value <= 8)
        botStratergy = "Hit"
      else if (value < 18) {
        searchkey = (value.toString, upcard.value.toString())
        debug(this + " generated " + searchkey)
        if (hardTotalStrategy.keySet.contains(searchkey))
          botStratergy = hardTotalStrategy(searchkey)
      } else
        botStratergy = "Stay"
    }
    debug(this + " got bot stratergy " + botStratergy)
    // creates request according to the bot strategy retrieved from the Hash maps.
    botStratergy match {
      case "Hit" =>
        request = Hit(pid)
      case "Stay" =>
        request = Stay(pid)
      case "DoubleDown" =>
        if (cards.size == 2)
          request = DoubleDown(pid)
        else
          request = Hit(pid)
      case "Surrender" =>
        if (cards.size == 2)
          request = Surrender(pid)
        else
          request = Hit(pid)
      case dontKnow =>
        debug("got" + dontKnow)
    }
    request
  }
         
   
}