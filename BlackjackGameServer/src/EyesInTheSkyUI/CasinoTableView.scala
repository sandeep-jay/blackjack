package EyesInTheSkyUI

import scala.swing._
import javax.swing.ImageIcon._
import javax.swing.JPanel
import javax.swing.table._
import javax.swing.ImageIcon
import javax.swing.JLabel
import scala.swing.event.ButtonClicked
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

// import javax.Alignment

object CasinoTableView extends SimpleGUIApplication {
  private var bufferedImage: BufferedImage = null
  def top = new MainFrame {

    //peer.setLocationRelativeTo(null)
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())

    peer.setLocation(500, 100)
    title = "Casino Induvidual Table view"
    menuBar = new MenuBar {
      contents += new Menu("File") {
        //contents += new MenuItem("New game")      
        contents += new MenuItem(Action("Exit") { println(title) })
      }
      contents += new Menu("Options")
      contents += new Menu("Help")
    }

    val tableIdLabel = new Label {
      text = "<html><b>Table ID</b></html>"
    }
    val miniBetLabel = new Label {
      text = "<html><b>Min bet </b></html>"
    }
    val earningsLabel = new Label {
      text = "<html><b>Earnings</b></html>"
    }
    val handsDealtLabel = new Label {
      text = "<html><b>Hands Dealt</b></html>"
    }
    val maxBetLabel = new Label {
      text = "<html><b>Max Bet</b></html>"
    }
    val lossesLabel = new Label {
      text = "<html><b>Losses</b></html>"
    }
    val tableDetailsLabel = new Label {
      text = "<html><b>Table Details</b></html>"
    }

    val tableIdTextBox = new TextField { (text = "1", columns = 5, editable = false) }
    val miniBetTextBox = new TextField { (text = "25", columns = 5, editable = false) }
    val earningsTextBox = new TextField { (text = "$16000", columns = 5, editable = false) }
    val handsDealtTextBox = new TextField { (text = "68", columns = 5, editable = false) }
    val lossesTextBox = new TextField { (text = "$50", columns = 5, editable = false) }
    val maxBetTextBox = new TextField { (text = "$50", columns = 5, editable = false) }
    val tableDetailsTextBox = new TextField { (text = "2000", columns = 5, editable = false) }

    var initial = Array(
      //Array("ID","NAME","BANKROLL", "HANDS", "EARNING","LOSSES"),
      Array("1", "Jim", "$5000", "5", "$300", "$550"),
      Array("2", "Robert", "$3000", "14", "$400", "$243"),
      Array("3", "John", "$2000", "17", "$500", "$50"),
      Array("4", "Joe", "$6000", "11", "$800", "$780"),
      Array("4", "Joe", "$8000", "11", "$10", "$1200"),
      Array("4", "Joe", "$700", "11", "$200", "$6780"),
      Array("4", "Joe", "$100", "11", "$9000", "$7500"))

    val names = Array("id", "First name", "Bankroll", "Hands played", "Earnings", "Losses")

    var memTable = new Table(initial.asInstanceOf[Array[Array[Any]]], names.asInstanceOf[Array[Any]]) {

      peer.enable(false)
    }

    val playerBettingHistoryButton = new Button {
      text = "<html><b>Player Betting History</b></html>"

    }

    val leaveTableButton = new Button {
      text = "<html><b>Back</b></html>"
    }

    contents = new BoxPanel(Orientation.Vertical) {

      /* val size12 = new Dimension(600,650)
			      preferredSize = size12;
            //preferredSize = new Dimension(,600)
            xLayoutAlignment = 0.0 
            val imageBackgroundPanel = new ImagePanel
         {   
			      imagePath = ("BJT.jpg")
			      
			      val size1 = new Dimension(600,490)
			      preferredSize = size1
			      //xLayoutAlignment = 600.0 
	              //yLayoutAlignment = 600.0
	              opaque = true
	       
	                         			      
         }  */

      //   contents +=  imageBackgroundPanel

      contents += new FlowPanel {
        contents += tableDetailsLabel
        opaque = false
      }

      contents += new FlowPanel {

        //maximumSize = new java.awt.Dimension(600,0)
        contents += new GridPanel(2, 5) {

          border = Swing.EmptyBorder(10, 10, 10, 10)
          //maximumSize = new java.awt.Dimension(600,0)
          //minimumSize = new java.awt.Dimension(0,0)
          xLayoutAlignment = 0

          //preferredSize = new Dimension(100,50)
          vGap = 15
          hGap = 5
          opaque = false

          contents += tableIdLabel
          contents += tableIdTextBox
          contents += miniBetLabel
          contents += miniBetTextBox
          contents += earningsLabel
          contents += earningsTextBox

          contents += handsDealtLabel
          contents += handsDealtTextBox
          contents += maxBetLabel
          contents += maxBetTextBox
          contents += lossesLabel
          contents += lossesTextBox
        }
        opaque = false
      }
      
      contents += new ScrollPane(memTable) {

        preferredSize = new Dimension(600, 150)
        border = Swing.EmptyBorder(10, 20, 10, 20)
        opaque = false;
      }
      /*contents += new ScrollPane(memTable) {
        border = Swing.EmptyBorder(0, 2, 0, 2)
        preferredSize = new Dimension(600, 100)

      }*/

      

      contents += new FlowPanel {

        contents += playerBettingHistoryButton

        contents += leaveTableButton
        opaque = false
        preferredSize = new Dimension(600, 80)
      }
      border = Swing.EmptyBorder(0, 0, 0, 0)

      listenTo(leaveTableButton, playerBettingHistoryButton)
      reactions += {
        case ButtonClicked(`playerBettingHistoryButton`) =>
          {
            PlayerBettingHistory.main(null)
            close()
          }
        case ButtonClicked(`leaveTableButton`) =>
          {
            StartCasino.main(null)
            close()
          }
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

    def main(args: Array[String]) {
      //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      pack

      val frame = top
      frame.visible = true

    }

  }
}
 