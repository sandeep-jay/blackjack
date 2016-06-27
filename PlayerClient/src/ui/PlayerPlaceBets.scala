package ui

import scala.swing._
import javax.swing.JFrame
import scala.swing.event.ButtonClicked
import javax.swing.JPanel
import scala.swing.GridPanel
import javax.swing.JLabel
import remoteActor.PlayerClientStarter
import message.PlayerData
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import scala.actors.Actor
import scala.actors.SchedulerAdapter
import remoteActor.Start

case class Dummy
object PlayerPlaceBets extends JFrame {

  private var bufferedImage: BufferedImage = null
  val playerClient = PlayerClientStarter.playerClient
  var playerLoginData = new PlayerData()

  def top = new MainFrame {

    title = "Place bets"
    peer.setLocation(550, 150)
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())

   
    val playerName = new Label {

      if (playerClient.playerData.playerName == " ") {
        text = "<html><b>User Name :</b></html>"
      } else {
        text = "<html><b>User Name :</b></html>"
      }

      horizontalTextPosition = Alignment.Right

    }
    

    val playerNameLabel = new Label {

      text = "<html><b>Name :</b></html>"
      horizontalTextPosition = Alignment.Right

    }

    val playerBankrollLabel = new Label {

      text = "<html><b>Bankroll :</b></html>"
      horizontalTextPosition = Alignment.Right
    }

   

    val playerBetAmountLabel = new Label {

      text = "<html><b>Bet amount :</b></html>"
      horizontalTextPosition = Alignment.Right
    }

    val playerNameTextField = new TextField {

      if (playerClient.playerData.playerName == " ") {
        text = "Enter name"
      } else {
        text = playerClient.playerData.playerName.toString()
      }
      //text = "Sam"

    }

    val playerBankrollTextBox = new TextField {

      //text = "3000"
      if (playerClient.playerData.playerBankroll == 0.0) {
        text = "Enter bankroll"
      } else {
        text = playerClient.playerData.playerBankroll.toString()
      }

    }

    val playerBetAmountTextBox = new TextField {

      text = "Enter bet amount"
    }

    val playeridTextBox = new TextField {

      text = "01"
      enabled = false

    }

    val playerNameTextBox = new TextField {

      enabled = false

    }
    val betButton = new Button {
      text = "<html><b>Bet</b></html>"
    }

    val exitButton = new Button {
      text = "<html><b>Exit</b></html>"
    }

    contents = new BorderPanel {
      import BorderPanel.Position._
      add(new GridPanel(5, 3) {
        List(new Label(""), playerNameLabel, playerNameTextField, new Label(""),
          new Label(""), playerBankrollLabel, playerBankrollTextBox, new Label(""),
          new Label(""), playerBetAmountLabel, playerBetAmountTextBox, new Label(""),
          new Label(""), new Label(""), new Label(""), new Label(""),
          new Label(""), betButton, exitButton).foreach { i => contents += i }
        border = Swing.EmptyBorder(60, 60, 60, 60)
        vGap = 15
        hGap = 5
        opaque = false
      }, South)
      //border = Swing.EmptyBorder(100,100,100,100)
      opaque = false

      bufferedImage = ImageIO.read(new File("BJT.jpg"))

      override def paintComponent(g: Graphics2D) =
        {
          super.paintComponent(g)
          if (null != bufferedImage) {
            g.drawImage(bufferedImage, 0, 0, null)
          }

        }

    }

    // button actions for the GUI
    listenTo(betButton, exitButton)
    reactions += {
      case ButtonClicked(`exitButton`) => {

        //close()
        closeOperation()

      }
      // performs validations on user input. Starts the player client actor.
      case ButtonClicked(`betButton`) => {

        var message = validatePlayerInputs()

        if (message.length == 0) {

          playerLoginData.playerName = playerNameTextField.text
          playerLoginData.playerBankroll = playerBankrollTextBox.text.toDouble
          playerLoginData.playerBetAmount = playerBetAmountTextBox.text.toDouble
          playerClient.playerData.playerID = 0
          playerClient.playerData.tableID = 0

          playerClient.playerData = playerLoginData
          PlayerClientStarter.main(null)

          close()
          PlayerClientStarter.bjScreen.main(null)

        } else {
          //println(message)
          Dialog.showMessage(null, message, "Error", Dialog.Message.Error)
        }

      }

    }

    // Validations for the user entered text fields.
    def validatePlayerInputs(): String = {

      var message = ""
      if (playerNameTextField.text == "" || playerNameTextField.text == " " || playerNameTextField.text == "Enter name") {
        message += "Enter player name."
      }
      if (playerBankrollTextBox.text == "" || playerBankrollTextBox.text == " " || playerBankrollTextBox.text == "Enter bankroll") {
        message += "\nEnter the Bankroll amount."
      }
      if (playerBetAmountTextBox.text == "" || playerBetAmountTextBox.text == " " || playerBetAmountTextBox.text == "Enter bet amount") {
        message += "\nEnter the bet amount. "
      }

      var isBankrollNumeric = false
      try {
        playerBankrollTextBox.text.toDouble
        isBankrollNumeric = true
      } catch {
        case e: Exception => isBankrollNumeric = false
      }

      var isBetAmountNumeric = false
      try {
        playerBetAmountTextBox.text.toDouble
        isBetAmountNumeric = true
      } catch {
        case e: Exception => isBetAmountNumeric = false
      }

      try {

        if (playerBetAmountTextBox.text.toDouble > playerBankrollTextBox.text.toDouble) {

          message += "\nBet amount value should be less than bankroll"
        }
      } catch {
        case e: Exception => message += ""
      }

      if (!isBankrollNumeric) {
        message += "\nBankroll must be a numeric value."
      }

      if (!isBetAmountNumeric) {
        message += "\nBet amount must be a numeric value."
      } else if (playerBetAmountTextBox.text.toDouble < 5) {

        message += "\nMinimum bet amount allowed is $5"
      }

      message

    }

  }
//main method
  def main(args: Array[String]) {

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    pack
    val frame = top
    frame.visible = true

  }

}
