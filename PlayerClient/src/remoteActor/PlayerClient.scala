package remoteActor

import scala.actors.Actor
import Actor._
import scala.actors.remote.RemoteActor.{ alive, register }
import scala.actors.remote.RemoteActor
import scala.actors.remote.Node
import scala.actors.AbstractActor
import message.PlayerLogin
import message.PlayerData
import message.TableData
import message.RequestStratergy
import message.GamePlayData
import message.GameProgress
import message.PlayerLogin
import ui.PlayerPlaceBets
import message.GameEnded

/** This message sent to start the pong actor */
case class Start(actor: AbstractActor)

/** This message sent to stop the ping actor */
case class Stop

// This message forwards the refresh screen request to the UI
case class RefreshScreenRequest

// acknowledges screen refresh
case class Refreshed

// Player client actor
class PlayerClient(myName: Symbol, myAddr: String, myPort: Int) extends Actor {

  // contains player login data. It is frequently updated.
  var playerData = new PlayerData
  // contains the game progress information
  var gameProgressData = new GamePlayData
  var count = 0
  var gameStarted = false
  var isStratergyRequested = false
  var stratergyRequestCount = 0
  //val bjScreen = ui.PlayerBJScreen

  private var listener: Actor = null

  // Player blackjack screen actor
  def listener(listener: Actor) {
    this.listener = listener
  }

  /** Arrives here on start */

  def act {
    // Declare the port I'm listening on
    alive(myPort)

    // Register me
    register(myName, self)

    println(">>> started " + myName + " @ " + myAddr + ":" + myPort)

    loop {
      react {
        // Receives updated Login information like pid and tid
        case PlayerLogin(playerData) =>

          println("Back with message")
          //PlayerClientStarter.bjScreen.main(null)

          //refreshes the screen
          listener ! RefreshScreenRequest
          Thread.sleep(100)
          ui.PlayerBJScreen.main(null)

        // Receives allocated table id  
        case TableData(tid) =>
          println("PlayerClient: Received Table Number = " + tid)
          gameProgressData.tid = tid

        //listener ! RefreshScreenRequest
        //ui.PlayerBJScreen.main(null)

        //Game server request strategy from the player
        case RequestStratergy(stratergy, gamePlayData) =>

          stratergyRequestCount += 1
          isStratergyRequested = true
          gameProgressData = gamePlayData

          listener ! RefreshScreenRequest
          Thread.sleep(100)
          ui.PlayerBJScreen.main(null)

        // updates game progress data everytime its changed.  
        case GameProgress(gamePlayData) =>

          gameStarted = true

          gameProgressData = gamePlayData
          listener ! RefreshScreenRequest
          Thread.sleep(100)
          ui.PlayerBJScreen.main(null)

        //listener ! RefreshScreenRequest
        //ui.PlayerBJScreen.main(null)


        case Refreshed => println("Refreshed the screen ... Repaint is done.")

        //Starts the communication with the game server by forwarding Player login info.
        case Start(gameServer) =>


          //playerData = new PlayerData
          //clears all the stale data
          gameProgressData.humanPlayer.cards.drop(gameProgressData.humanPlayer.cards.size) //= new GamePlayData
          gameProgressData.ronBot.cards.drop(gameProgressData.ronBot.cards.size)
          gameProgressData.sandBot.cards.drop(gameProgressData.sandBot.cards.size)
          gameProgressData.dealer.cards.drop(gameProgressData.dealer.cards.size)
          gameProgressData.humanPlayer.handValue = 0
          gameProgressData.humanPlayer.result = " "

          gameProgressData.ronBot.handValue = 0
          gameProgressData.ronBot.result = " "

          gameProgressData.sandBot.handValue = 0
          gameProgressData.sandBot.result = " "

          count = 0
          gameStarted = false
          
          //forwards player login info to game server
          gameServer ! PlayerLogin(playerData)

        // Receives notification when game has ended. Clears all the stale data.  
        case GameEnded =>

          println("Game Over")
          listener ! GameEnded
          playerData.playerBankroll = gameProgressData.humanPlayer.bankroll
          //ui.PlayerBJScreen.main(null)
          Thread.sleep(20000)
          //playerData = new PlayerData

          gameProgressData = new GamePlayData
          stratergyRequestCount = 0
          isStratergyRequested = false

      }
    }

  }

  // Forwards player game strategy to Game server to further process the request.
  def responseStratergy(stratergy: String, pid: Int) {

    val gameServer = RemoteActor.select(Node(myAddr, 2551), 'gameServer)
    gameServer ! RequestStratergy(stratergy, gameProgressData)
    isStratergyRequested = false

  }

}

object PlayerClientStarter {
  /** My port */
  val MY_PORT = 2552

  /** My service name */
  val MY_NAME = 'playerClient

  /** The other port */
  val OTHER_PORT = 2551

  /** The other service name */
  val OTHER_NAME = 'gameServer

  val myAddr = "localhost"

  val gameserverAddr = "localhost"

  // Get reference to the ping actor
  val gameServer = RemoteActor.select(Node(gameserverAddr, OTHER_PORT), OTHER_NAME)


  val playerClient = new PlayerClient(MY_NAME, myAddr, MY_PORT)
  val bjScreen = ui.PlayerBJScreen
  
  // used to de-serialize objects.
  RemoteActor.classLoader = getClass().getClassLoader()



  /** Runs the main thread */
  def main(args: Array[String]) {

    playerClient.start()
    playerClient ! Start(gameServer)

  }

}