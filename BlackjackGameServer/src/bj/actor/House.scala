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
import bj.hkeeping.Ok
import bj.table.Table
import scala.collection.mutable.HashMap
import bj.hkeeping.NotOk
import scala.actors.OutputChannel
import bj.util.Logs
import message.GameComplete

case class Bet(player: Int, amt: Double)
case class GameStart(players : List[OutputChannel[Any]])
case class GameOver(payouts : HashMap[Int,Outcome])
case class Arrive(mailbox : OutputChannel[Any], player : Int, betAmt : Double)
case class TableNumber(tid : Int)
case class Go

/** This object represents the house as a container of tables. */
object House extends Actor with Logs {
  var nextId = 0
  /** My port */
  val MY_PORT = 2551
  
  /** My service name */
  val MY_NAME = 'House
  
  var myaddr = null
  var tables = List[Table](new Table(100,100000.0,"Started"), new Table(25,10000.0,"Started"), new Table(5,5000.0,"Stopped"))
  
  override def toString = "house("+nextId+")"
   
  /** This method receives messages. */
  def act {
        
    loop {
      react {
        // Receives a bet from a player and matches it
        // to a table
        case Bet(pid : Int, bet : Double) =>
          debug("house: received bet amt = "+bet)
          
          tables.find(t => t.bets.size < Table.MAX_PLAYERS && t.minBet <= bet) match {
            case None =>
              sender ! NotOk
              
            case Some(table) =>
              debug("house: sending table id = "+table.tid+" sender = "+sender)
              table ! Arrive(sender, pid, bet)
              
              sender ! TableNumber(table.tid)
              
             
          }
          
        // Receives a message to tell the tables to go
        case Go =>
          debug("house: receive Go for "+tables.size+" tables")
          tables.foreach(t => t ! Go)
          
        case GameComplete(string) => tables.foreach(t => t.clear) 
          
        // Receives something completely from left field
        case dontKnow =>
          // Got something we REALLY didn't expect
          debug(this+" got "+dontKnow)          
        }
    }
  }   
  
}