package bj.actor

import scala.actors.Actor
import Actor._
import scala.actors.remote.RemoteActor.{ alive, register }
import scala.actors.remote.RemoteActor
import scala.actors.remote.Node
import scala.actors.AbstractActor
import bj.util.Logs
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.log4j.PropertyConfigurator
import EyesInTheSkyUI.StartCasino
import message.PlayerLogin
import remoteActor.PlayerClient
import remoteActor.PlayerClientStarter
import message.TableData
import message.PlayerData
import message.RequestStratergy
import bj.table.Table
import bj.card.Hand
import message.GamePlayData
import message.GameProgress
import message.GameComplete
import message.GameEnded
import scala.actors.OutputChannel

case class Done
case class Launch 

// gets the output channel of the human player.
case class HumanPlayerActor
//case class HumanArrive(humanArriveCount: Int)

//Game Server actor.
class GameServer(myName: Symbol, myAddr: String, myPort: Int) extends Actor with Logs {

  var count = 1
  /** My port */
  val MY_PORT = 2551

  /** My service name */
  val MY_NAME = 'gameServer

  val playerClient = GameServerStarter.playerClient
  var playerCount = 0
  var availableTables = List[Table]()
  var newPlayerData = new PlayerData
  var currentStratergy = new String
  var gamePlayData = new GamePlayData

  var tableDealer = House.tables(newPlayerData.tableID).dealer

  var randBetAmountRonBot = 0
  var randBetAmountSandBot = 0
  var humanBettingCount = 0

  var casinoPlayers = List[Player]()
  private var humanPlayerActor = List[OutputChannel[Any]]()

  
  PropertyConfigurator.configure("log4j.properties")
  /** Arrives here on start */
  def act {
    // Declare the port I'm listening on
    alive(MY_PORT)

    // Register me
    register(MY_NAME, self)

    startHouse()
    //RemoteActor.classLoader = getClass().getClassLoader()
    println(">>> started " + MY_NAME + " @ localhost:" + MY_PORT)

    loop {
      
      react {

        // Receives new player information from the client.
        case PlayerLogin(playerData) =>

          newPlayerData = playerData
          //gamePlayData = newGamePlayData

          debug("starting players")

          
          // Bot player bet amount generation.. not working as of now.
          
          /*if (playerData.playerBetAmount >= 5 && playerData.playerBetAmount < 25) {
            //val rnd = new GenRandInt(5, 24)
            val rnd = new scala.util.Random
            val range = 100 to 200

            randBetAmountRonBot = rnd.nextInt(range length)
            randBetAmountSandBot = rnd.nextInt(range length)
          } else if (playerData.playerBetAmount >= 25 && playerData.playerBetAmount < 100) {

            val rnd = new scala.util.Random
            val range = 25 to 99

            randBetAmountRonBot = rnd.nextInt(range length)
            randBetAmountSandBot = rnd.nextInt(range length)

          } else if (playerData.playerBetAmount >= 100) {

            val rnd = new scala.util.Random
            val range = 100 to 200

            randBetAmountRonBot = rnd.nextInt(range length)
            randBetAmountSandBot = rnd.nextInt(range length)

          }

          debug("Ron Bot bet amount :" + randBetAmountRonBot)
          debug("Sand Bot bet amount :" + randBetAmountSandBot)*/
          
          

          //          val players = List[Player](new Player(playerData.playerName, playerData.playerBankroll, playerData.playerBetAmount, true)
          //          							, new Player("SandBot", 1000, 21, false),new Player("RonBot", 1000, 20, false) )

          //          val players = List[Player](new Player("SandBot", 1000, 21, false)
          //          							,new Player(playerData.playerName, playerData.playerBankroll, playerData.playerBetAmount, true)
          //          							 )	

          // Creating list of players who have entered the casino
          val players = List[Player](new Player("RonBot", 1000, 40, false), new Player(playerData.playerName, playerData.playerBankroll, playerData.playerBetAmount, true), new Player("SandBot", 1000, 50, false))

          Player.start(players)
          casinoPlayers = players

          Thread.sleep(100)
          
        // Forwards table allcated daa to the client  
        case TableData(tid: Int) =>
          println("Player one table selected. Number = " + tid)

          var playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)
          playerClient ! TableData(tid)

        // Receives the human player strategy  
        case RequestStratergy(stratergy, gameProgressData) =>
          debug("Current stratergy is " + stratergy)
          currentStratergy = stratergy
          val request = humanStratergy()
          var pid = newPlayerData.playerID


          //Gets the player position in the table 
          for (i <- 0 to 2) {

            if (House.tables(newPlayerData.tableID).dealer.players(i) == humanPlayerActor(0)) {
              humanBettingCount = i
            }
          }

          var humanPlayerOutputChannel = House.tables(newPlayerData.tableID).dealer.players(humanBettingCount) //.get(humanBettingCount).exists(p => true)

          // Forwards the strategy from the human player to Player actor.
          humanPlayerOutputChannel ! StratergyReceived(request, newPlayerData.tableID)

        // Forwards the observed Up card of the dealer.
        case Up(card, handValue) =>
          debug("Up card received from player && dealer")
          debug("Up card is : " + card)
          gamePlayData.dealer.cards(0) = card.toString
          gamePlayData.dealer.handValue = handValue
          debug("Game play data dealer upCard : " + gamePlayData.dealer.cards(0))

          //messages Player client
          callPlayerClientActor()

        // Updates game progress data as and when cards are dealt  
        case CardDealt(card, pid, handSize, handValue, bankroll, betAmount) =>

          if (pid == 0) {

            gamePlayData.ronBot.cards(handSize - 1) = card.toString
            gamePlayData.ronBot.handValue = handValue
            gamePlayData.ronBot.bankroll = bankroll
            gamePlayData.ronBot.betAmount = betAmount
            gamePlayData.ronBot.pid = pid

            debug("RonBot card: " + gamePlayData.ronBot.cards(handSize - 1))
            debug("RonBot hand value " + gamePlayData.ronBot.handValue)
          } else if (pid == 1) {

            gamePlayData.humanPlayer.cards(handSize - 1) = card.toString
            gamePlayData.humanPlayer.handValue = handValue
            gamePlayData.humanPlayer.bankroll = bankroll
            gamePlayData.humanPlayer.betAmount = betAmount
            gamePlayData.humanPlayer.pid = pid

            debug("Human card: " + gamePlayData.humanPlayer.cards(handSize - 1))
            debug("Human hand value " + gamePlayData.humanPlayer.handValue)
          } else if (pid == 2) {

            gamePlayData.sandBot.cards(handSize - 1) = card.toString
            gamePlayData.sandBot.handValue = handValue
            gamePlayData.sandBot.bankroll = bankroll
            gamePlayData.sandBot.betAmount = betAmount
            gamePlayData.sandBot.pid = pid

            debug("SandBot card: " + gamePlayData.sandBot.cards(handSize - 1))
            debug("SandBot hand value " + gamePlayData.sandBot.handValue)
          }

          
          callPlayerClientActor()

        // Observes dealer cards only once.   
        case Observe(card, player, handSize, handValue) =>

          if (player == gamePlayData.humanPlayer.pid) {
            gamePlayData.dealer.cards(handSize) = card.toString
            gamePlayData.dealer.handValue = handValue

          }

          callPlayerClientActor()

        // Updates the results section of the game play data  
        case ResultsData(pid, result, wonAmount, newBankroll) =>

          if (pid == 0) {

            gamePlayData.ronBot.result = result
            gamePlayData.ronBot.amountWon = wonAmount
            gamePlayData.ronBot.bankroll = newBankroll

          } else if (pid == 1) {

            gamePlayData.humanPlayer.result = result
            gamePlayData.humanPlayer.amountWon = wonAmount
            gamePlayData.humanPlayer.bankroll = newBankroll

          } else if (pid == 2) {

            gamePlayData.sandBot.result = result
            gamePlayData.sandBot.amountWon = wonAmount
            gamePlayData.sandBot.bankroll = newBankroll
          }

         
          callPlayerClientActor()

         // Receives human player actor channel when Table is assigned. 
        case HumanPlayerActor =>
          var syncSender = List[OutputChannel[Any]](sender)
          humanPlayerActor = humanPlayerActor ::: syncSender

        // Updates game play data. Flushes the House. Clears tables , players.  
        case GameComplete(flush: String) =>

          Thread.sleep(1000)
          var playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)
          playerClient ! GameEnded

          flushHouse()

      }
    }

  }

  // Clears House , game server and instantiates new objects for next game
  def flushHouse() {

    Thread.sleep(10000)

    casinoPlayers.foreach(player => casinoPlayers.remove(player => true))

    humanBettingCount = 0
    playerCount = 0
    Player.stop(casinoPlayers)
    newPlayerData = new PlayerData
    currentStratergy = new String
    gamePlayData = new GamePlayData
    casinoPlayers.drop(casinoPlayers.size)


  }

  // Player client actor communication information
  def callPlayerClientActor() {

    var playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)
    playerClient ! GameProgress(gamePlayData)

  }

  // Starts the house(tables)
  def startHouse() {
    debug("starting the house")
    House.start
    Thread.sleep(1000)
    availableTables = House.tables
   
    Thread.sleep(1000)
  }

  // processes human strategy received
  def humanStratergy(): Request = {

    //humanStratergy(stratergy)
    var request = new Request
    if (currentStratergy == "Hit") {

      debug("Human player stratergy Hit")
      request = Hit(newPlayerData.playerID)


    } else if (currentStratergy == "DoubleDown") {
      debug("Human player stratergy DoubleDown")
      request = DoubleDown(newPlayerData.playerID)
    } else if (currentStratergy == "Surrender") {
      debug("Human player stratergy Surrender")
      request = Surrender(newPlayerData.playerID)
    } else {
      debug("Human player stratergy Stays")
      request = Stay(newPlayerData.playerID)
    }

    return request

  }


  // communicates with player client and notifies that it is human players turn to play.
  def requestStratergy(pid: Int, tid: Int) {

    //getDealerHandCopy(tid)

    debug("GamePlayData " + gamePlayData.humanPlayer.cards(0).toString())
    debug("Request stratergy from human player ")
    var playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)
    var stratergy = ""
    playerClient ! RequestStratergy(stratergy, gamePlayData)
  }

  // updates table information for the players in casino. Gives a go ahead to the tables.
  def receivedTable(tid: Int, pid: Int) {

    debug("Received table " + tid)
    var playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)

    if (pid == 1) {

      //humanBettingCount = playerCount
      newPlayerData.playerID = pid
      newPlayerData.tableID = tid
      playerClient ! PlayerLogin(newPlayerData)
    }

    playerCount += 1
    if (playerCount == 3) { //House.tables(tid).players.size){
      debug("telling house go")
      House ! Go

      //, gamePlayData)  

      //playerCount = 0
    }

    debug("Player count received table :" + playerCount)
  }

  // Noties that the game is over.
  def gameOver() {

    for (i: Int <- 0 to House.tables.size)
      (House.tables.drop(i))

  }

}
object GameServerStarter {
  /** My port */
  val MY_PORT = 2551

  /** My service name */
  val MY_NAME = 'gameServer

  val myAddr = "localhost"

  RemoteActor.classLoader = getClass().getClassLoader()
  val gameServer = new GameServer(MY_NAME, myAddr, MY_PORT)
  val playerClient = RemoteActor.select(Node(myAddr, 2552), 'playerClient)
  /** Runs the main thread */
  def main(args: Array[String]): Unit = {

    gameServer.start()

  }

}
