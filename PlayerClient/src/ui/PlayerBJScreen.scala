package ui

import swing._
import scala.swing._
import scala.swing.BorderPanel.Position.North
import scala.swing.event.ButtonClicked
import scala.swing.Dimension
import scala.swing.Alignment
import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Orientation
import scala.swing.SimpleSwingApplication
import scala.swing.Swing
import scala.swing.TextField
import javax.swing.ImageIcon
import scala.swing.FlowPanel
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import remoteActor.PlayerClientStarter
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ Timer => SwingTimer, AbstractAction }
import javax.swing.Timer
import javax.swing.JButton
import javax.swing._
import java.awt.event._
import scala.actors.Actor
import Actor._
import remoteActor.RefreshScreenRequest
import scala.actors.SchedulerAdapter
import remoteActor.Refreshed
import message.GameEnded

object PlayerBJScreen extends SimpleSwingApplication {

  private var bufferedImage: BufferedImage = null
  val playerClient = PlayerClientStarter.playerClient

  def top = new MainFrame {
    title = "Blackjack Table"
    peer.setLocation(500, 100)
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())
    peer.setResizable(false)

    val refreshButton = new Button {
      visible = false

    }

    val gameOverButton = new Button {
      visible = false

    }

    val enablingActor = new Actor {
      override val scheduler = new SchedulerAdapter {
        def execute(fun: => Unit) { Swing.onEDT(fun) }
      }

      def act() {

        loop {
          react {
            case RefreshScreenRequest => {
              val syncSender = sender
              SwingUtilities.invokeLater(new Runnable() {
                def run() {

                  refreshButton.doClick()

                }
              })
            }
            case GameEnded => {
              val syncSender = sender
              SwingUtilities.invokeLater(new Runnable() {
                def run() {
                  gameOverButton.doClick()

                }
              })
            }
          }
        }

      }
    }

    enablingActor.start()
    playerClient.listener(enablingActor)



    val label = new Label {

      icon = new ImageIcon("BJT.jpg")

      preferredSize = new java.awt.Dimension(600, 600)
    }

    val playerBankrollLabel = new Label {

      text = "<html><b>Bankroll :</b></html>"
      horizontalTextPosition = Alignment.Right
    }

    val playerBankrollTextField = new TextField {

      if (playerClient.gameProgressData.humanPlayer.newBankroll == 0.0)
        text = playerClient.gameProgressData.humanPlayer.bankroll.toString() //"5,000"
      else
        text = playerClient.gameProgressData.humanPlayer.bankroll.toString()

      editable = false

    }

    val currentBetLabel = new Label {

      text = "<html><b>Current Bet :</b></html>"
      horizontalTextPosition = Alignment.Right
    }

    val currentBetTextField = new TextField {

      text = playerClient.gameProgressData.humanPlayer.betAmount.toString()
      editable = false

    }

    val surrenderButton = new Button {
      text = "<html><b>Surrender</b></html>"
      enabled = false
    }

    val splitButton = new Button {
      text = "<html><b>Split</b></html>"
      enabled = false
    }

    val hitButton = new Button {
      text = "<html><b>Hit</b></html>"
      enabled = false
    }

    val standButton = new Button {
      text = "<html><b>Stand</b></html>"
      enabled = false
    }

    val insuranceButton = new Button {
      text = "<html><b>Insurance</b></html>"
      enabled = false
    }

    val doubleDownButton = new Button {
      text = "<html><b>Double down</b></html>"
      enabled = false
    }

    contents = new BoxPanel(Orientation.Vertical) {
      maximumSize = new java.awt.Dimension(600, 500)
      // preferredSize = maximumSize
      border = Swing.EmptyBorder(0, 0, 0, 0)

      val formPanel = new BoxPanel(Orientation.Vertical) {

        val size1 = new Dimension(600, 450)

        val dealerLabel1 = new Label {

          text = "<html><b>Dealer : Rob </b></html>"
          horizontalTextPosition = Alignment.Right
          opaque = false

        }

        val dealerLabel2 = new Label {

          icon = new ImageIcon("clubs-j-75-1.jpg")
          //text = "<html><b>Cards:</b></html>"
          //  horizontalTextPosition = Alignment.Center
        }

        val dealerLabel3 = new Label {

          icon = new ImageIcon("back-red-75-1-1.jpg")
          
        }

        val dealerLabel4 = new Label {

          var dealerCards = playerClient.gameProgressData.dealer.cards.reduceLeft(_ + " ," + _)
          text = "<html><b>Cards: " + dealerCards + "</b></html>"
          opaque = false

        }

        val dealerLabel5 = new Label {
          
          text = "<html><b>Hand Value : " + playerClient.gameProgressData.dealer.handValue + "</b></html>"
          opaque = false

        }

        val player1Label1 = new Label {

          text = "<html><b>Player: " + playerClient.playerData.playerName + " </b></html>"
          opaque = false
        }

        val player1Label2 = new Label {

          //icon = new ImageIcon("clubs-j-75-1.jpg")
          var humanPlayerCards = playerClient.gameProgressData.humanPlayer.cards.reduceLeft(_ + " ," + _)
          text = "<html><b>Cards: " + humanPlayerCards + " </b></html>"
          opaque = false
        }

        val player1Label3 = new Label {

          // icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Hand Value :" + playerClient.gameProgressData.humanPlayer.handValue + " </b></html>"
          opaque = false
        }

        val player1Label4 = new Label {

          //icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Result : " + playerClient.gameProgressData.humanPlayer.result + " "
          +playerClient.gameProgressData.humanPlayer.amountWon + " </b></html>"
          opaque = false
        }

        val player2Label1 = new Label {

          text = "<html><b>Player: RonBot </b></html>"
        }

        val player2Label2 = new Label {

          //icon = new ImageIcon("clubs-j-75-1.jpg")
          var ronBotCards = playerClient.gameProgressData.ronBot.cards.reduceLeft(_ + " ," + _)
          text = "<html><b>Cards: " + ronBotCards + " </b></html>"
          opaque = false

        }

        val player2Label3 = new Label {

          //icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Hand Value :" + playerClient.gameProgressData.ronBot.handValue + " </b></html>"
          opaque = false
        }

        val player2Label4 = new Label {

          //          icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Result : " + playerClient.gameProgressData.ronBot.result + " "
          +playerClient.gameProgressData.ronBot.amountWon + " </b></html>"
          opaque = false
        }

        val player3Label1 = new Label {
          text = "<html><b>Player: SandBot </b></html>"
        }

        val player3Label2 = new Label {

          //icon = new ImageIcon("clubs-j-75-1.jpg")
          var sandBotCards = playerClient.gameProgressData.sandBot.cards.reduceLeft(_ + " ," + _)
          text = "<html><b>Cards: " + sandBotCards + " </b></html>"
          opaque = false
        }

        val player3Label3 = new Label {

          //icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Hand Value :" + playerClient.gameProgressData.sandBot.handValue + " </b></html>"
          opaque = false

        }

        val player3Label4 = new Label {

          //          icon = new ImageIcon("clubs-a-75-1.jpg")
          text = "<html><b>Result : " + playerClient.gameProgressData.sandBot.result + " "
          +playerClient.gameProgressData.sandBot.amountWon + " </b></html>"
          opaque = false

        }

        val dealerGamePanel = new FlowPanel {

          vGap = 20
          contents += dealerLabel1
          contents += dealerLabel2
          contents += dealerLabel3
          opaque = false

        }

        val playerGamePanel = new FlowPanel {

          contents += player1Label2
          contents += player1Label3
          //contents += new Separator
          contents += player1Label1
          opaque = false

        }

        val playerGamePanel2 = new FlowPanel {

          vGap = 110
          contents += player2Label2
          contents += player2Label3
          contents += player2Label1
          opaque = false

        }

        val playerGamePanel3 = new FlowPanel {

          vGap = 110
          contents += player3Label2
          contents += player3Label3
          contents += player3Label1
          opaque = false

        }

        contents += new BorderPanel {
          import BorderPanel.Position._
          //add(PlayerGamePanel2, East)
          add(dealerGamePanel, North)
          //add(playerGamePanel, South)
          //add(PlayerGamePanel3, West)

          add(new GridPanel(9, 1) {
            List(player3Label2, player3Label3, player3Label4, player3Label1, new Label(""), new Label(""), new Label(""),new Label(""),new Label("")).foreach { i => contents += i }
            opaque = false
          }, West)

          add(new GridPanel(9, 1) {
            List(player2Label2, player2Label3, player2Label4, player2Label1, new Label(""), new Label(""), new Label(""),new Label(""),new Label("")).foreach { i => contents += i }
            opaque = false
          }, East)

          add(new GridPanel(9, 1) {

            List(new Label(""), new Label(""),player1Label2, player1Label3, player1Label4, player1Label1, new Label(""), new Label(""), new Label("")).foreach { i => contents += i }
            opaque = false
          }, South)

          add(new GridPanel(9, 1) {
            List(new Label(""), new Label(""), dealerLabel1, dealerLabel4, dealerLabel5, new Label(""),new Label(""), new Label(""),new Label("")).foreach { i => contents += i }
            opaque = false
          }, North)

          opaque = false
        }

        bufferedImage = ImageIO.read(new File("BJT.jpg"))

        override def paintComponent(g: Graphics2D) =
          {
            super.paintComponent(g)
            if (null != bufferedImage) {
              g.drawImage(bufferedImage, 0, 0, null)

            }
          }

      }

      val buttonGrid = new GridPanel(2, 5) {
        List(playerBankrollLabel, playerBankrollTextField, hitButton, doubleDownButton,
          refreshButton, currentBetLabel, currentBetTextField, standButton, splitButton,
          surrenderButton).foreach { i => contents += i }
        border = Swing.EmptyBorder(10, 10, 10, 10)
        maximumSize = new java.awt.Dimension(600, 0)
        minimumSize = new java.awt.Dimension(0, 0)
        xLayoutAlignment = 0
        vGap = 5
        hGap = 5
        opaque = true

      }

      val menuBar = new MenuBar {
        contents += new Menu("File") {
          //contents += new MenuItem("Exit") 

        }

        contents += new Menu("Options") {
          //contents += new MenuItem("Start Casino")
          //contents += new MenuItem("Stop Casino")
          //contents += new MenuItem("Reset Tables")

        }

      }

      val bjLayoutPanel = new BorderPanel {
        import BorderPanel.Position._
        add(menuBar, North)
        //add(imageBackgroundPanel, Center) 
        add(formPanel, Center)
        add(buttonGrid, South)
      }

      contents += bjLayoutPanel
      val timer = new Timer(100, new ActionListener {
        def actionPerformed(e: ActionEvent) {
          bjLayoutPanel.repaint
        }
      })

      timer.start()

      if (playerClient.isStratergyRequested) {

        hitButton.enabled = true
        surrenderButton.enabled = true
        standButton.enabled = true

        if (playerClient.stratergyRequestCount <= 1) {
          doubleDownButton.enabled = true
        }
      }

      listenTo(surrenderButton, hitButton, doubleDownButton, standButton, refreshButton, gameOverButton)
      reactions += {

        case ButtonClicked(`gameOverButton`) => {

          var amountWon = playerClient.gameProgressData.humanPlayer.amountWon
          if (amountWon < 0) {
            amountWon = amountWon - 2 * amountWon
          }

          var n = JOptionPane.showConfirmDialog(null, "You " + playerClient.gameProgressData.humanPlayer.result + " $"
            + amountWon + ". \nNew Bankroll is $" + playerClient.gameProgressData.humanPlayer.bankroll
            + ".\nPlace new Bets?? ", "Game Over", JOptionPane.OK_CANCEL_OPTION)

          if (n == 2) {

            PlayerClientStarter.playerClient.playerData.playerName = " "
            PlayerClientStarter.playerClient.playerData.playerBankroll = 0.0
            dispose()

          }else{
            dispose()
          }
          

          dispose()
          close()
          PlayerPlaceBets.main(null)

        }

        case ButtonClicked(`surrenderButton`) => {

          playerClient.responseStratergy("Surrender", playerClient.playerData.playerID.toInt)
          close()
          //PlayerPlaceBets.main(null)

        }

        case ButtonClicked(`hitButton`) => {

          //top.repaint()
          dispose()
          playerClient.responseStratergy("Hit", playerClient.playerData.playerID.toInt)

          close()
        }

        case ButtonClicked(`doubleDownButton`) => {

          //top.repaint()
          playerClient.responseStratergy("DoubleDown", playerClient.playerData.playerID.toInt)
          dispose()
          close()

        }

        case ButtonClicked(`standButton`) => {

          //top.repaint()
          playerClient.responseStratergy("Stay", playerClient.playerData.playerID.toInt)
          close()
          dispose()
        }

        case ButtonClicked(`refreshButton`) => {


          close()
          dispose()
          //top.repaint()
        }

      }

    }

    def main(args: Array[String]) {
      pack
      val frame = top
      frame.visible = true

    }

  }

}
