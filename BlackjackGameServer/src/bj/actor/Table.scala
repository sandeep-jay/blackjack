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
package bj.table
import scala.actors.Actor
import Actor._
import scala.actors.remote.RemoteActor.{ alive, register }
import bj.actor.Player
import bj.actor.Dealer
import scala.collection.mutable.HashMap
import scala.actors.Actor
import scala.actors.OutputChannel
import bj.hkeeping.NotOk
import bj.hkeeping.Ok
import bj.hkeeping.Reply
import bj.actor.Go
import bj.actor.Arrive
import bj.util.Logs
import bj.actor.Outcome
import bj.actor.Win
import bj.actor.Loose
import bj.actor.Push
import bj.actor.GameStart
import bj.actor.Bet
import bj.actor.GameOver
import bj.actor.GameServerStarter
import message.GameComplete
import bj.actor.House



/** This class implements the table static members */
object Table {
  val MIN_PLAYERS: Int = 1
  val MAX_PLAYERS: Int = 3

  var id: Int = -1
}

/**
 * This class implement table instances.
 * @param minBet Minimum bet for the table.
 */
class Table(val minBet: Double, var tableBankroll: Double, var tableStatus: String) extends Actor with Logs {
  /** Table's id */
  Table.id += 1
  val tid = Table.id

  /** Dealer for this table */
  var dealer = new Dealer

  /** True if this table is involved in a game */
  var trucking: Boolean = false

  var tableEarnings = 0.0

  var tableLosses = 0.0

  /** Bet amounts by player id */
  var bets = HashMap[Int, Double]()

  /** Mailboxes by player id */
  var players = HashMap[Int, OutputChannel[Any]]()

  /** House mail box */
  var house: OutputChannel[Any] = null

  //var humanArriveCount = 0
  /** Starts the table */
  start

  /** Gives a string version of the table */
  override def toString: String = "table(" + tid + ", " + minBet + ")"

  /** This method receives messages */
  def act {
    loop {
      react {
        // Receives arrival of a player: mailbox is the player's
        case Arrive(mailbox: OutputChannel[Any], pid: Int, betAmt: Double) =>{
          debug(this + " received ARRIVE from " + mailbox + " amt = " + betAmt)
                   
       /*   if(pid == 1){
        	  GameServerStarter.gameServer ! HumanArrive(humanArriveCount)
          }
          humanArriveCount += 1
          
          if(humanArriveCount == 2){
            humanArriveCount = 0
          }*/
          
          arrive(mailbox, pid, betAmt)
        }
        // Receive game over signal from the dealer
        case GameOver(pays: HashMap[Int, Outcome]) =>
          debug(this + " received game over for " + pays.size + " players")
          gameOver(pays)
          
          
          

        // Receives game start signal from the house
        case Go =>
          debug(this + " received Go for " + players.size + " players")

          go

      }
    }
  }

  /**
   * Processes a player arrival
   * @param source Player's mailbox
   * @param pid Player's id
   * @param betAmt Player's bet amount
   */
  def arrive(source: OutputChannel[Any], pid: Int, betAmt: Double) {
    val reply = placed(source, Bet(pid, betAmt))

    debug(this + " bet = " + reply)

    source ! reply
  }

  /** Handles game start */
  def go {
    val bettors = players.foldLeft(List[OutputChannel[Any]]())((xs, x) => xs ::: List(x._2))

    if (bettors.size != 0) {

      debug(this + " dealing " + bettors.size + " bettors")

      dealer ! GameStart(bettors)
    }
  }

  /**
   * Places the bet after validation.
   * @param mailbox Player's mailbox
   * @param bet Bet parameters
   */
  def placed(mailbox: OutputChannel[Any], bet: Bet): Reply = {
    debug("table: placing bet amt = " + bet.amt + " num bets = " + bets.size)
    if (bet.amt <= 0 || bets.size >= Table.MAX_PLAYERS)
      return NotOk

    players.get(bet.player) match {
      case None =>
        debug("table: adding new player id = " + bet.player)
        players += bet.player -> mailbox

        bets += bet.player -> bet.amt

      case Some(player) =>
        bets.get(bet.player) match {
          case Some(oldAmt) =>
            debug("table: updating bet for player id = " + bet.player)
            bets(bet.player) = (oldAmt + bet.amt)

          case None =>
            debug("table: got bad bet")

            return NotOk
        }
    }

    Ok
  }

  /** Handles game over */
  def gameOver(pays: HashMap[Int, Outcome]) {
    
    pays.foreach(p => pay(p))
    
    Thread.sleep(10000)
    
   

     /** Mailboxes by player id */
  	
    //Player.stop(players)
  	Player.id = -1
  	
  	
  	players.clear()
  	bets.clear()
  	house = null
  	trucking = false
  	//humanArriveCount = 0
  	House ! GameComplete("End")
    GameServerStarter.gameServer ! GameComplete("Flush all Data ")
    
  }

  /**
   * Sends payment to player.
   * @param pid Player id
   * @parm outcome Game outcome for player pid
   */
  def pay(figure: (Int, Outcome)): Unit = {
    val (pid, outcome) = figure

    outcome match {
      case Win(gain) =>
        debug("player(" + pid + ") won " + gain)
        players(pid) ! outcome

      case Loose(gain) =>
        debug("player(" + pid + ") lost " + gain)
        players(pid) ! outcome

      case Push(gain) =>
        debug("player(" + pid + ") push " + gain)
        players(pid) ! outcome
    }

  }

  /** Clears all the bets -- NOT USED */
  def clear: Unit = bets.clear
}