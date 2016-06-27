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

object PlayerBettingHistory extends SimpleGUIApplication {
  private var bufferedImage: BufferedImage = null
  def top = new MainFrame {

    //peer.setLocationRelativeTo(null)
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())

    peer.setLocation(500, 100)
    title = "Players betting History"

    val playerIdLabel = new Label {
      text = "<html><b>Player's ID</html></b>"
    }
    val PlayerNamelabel = new Label {
      text = "<html><b>Player's Name</html></b>"
    }
    val tableDetailsLabel = new Label {
      text = "<html><b>Player History</b></html>"
         horizontalTextPosition = Alignment.Right
    }

    val playerIdTextBox = new TextField { (text = "1", columns = 5, editable = false) }
    val PlayerNameTextBox = new TextField { (text = "25", columns = 5, editable = false) }

    val goBackButton = new Button {
      text = "<html><b>Back</html></b>"

    }

    val tableModel = new DefaultTableModel(new Array[Array[AnyRef]](0, 6), Array[AnyRef]("Sl Id", "Table ID", "Bet Amount", "Card Count", "Dealer Count", "Earnings", "Result"))
    val table = new Table(8, 5) {
      model = tableModel
      // xLayoutAlignment = 100
      tableModel.addRow(Array[AnyRef]("01", "0", "$24", "14", "15", "$200", "LOST", "5000"))
      tableModel.addRow(Array[AnyRef]("02", "7", "$49", "17", "11", "$1255", "WON", "6700"))
      tableModel.addRow(Array[AnyRef]("03", "5", "$67", "20", "9", "$300 ", "LOST", "10000"))
      tableModel.addRow(Array[AnyRef]("04", "3", "$89", "18", "17", "$6890", "WON", "34900"))
      tableModel.addRow(Array[AnyRef]("05", "6", "$90", "9", "7", "$500", "LOST", "34900"))
      tableModel.addRow(Array[AnyRef]("06", "8", "$99", "18", "37", "$700", "WON", "34900"))

    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new FlowPanel {
        contents += tableDetailsLabel
        opaque = false
      }

      val size12 = new Dimension(500, 400)
      preferredSize = size12;
      //preferredSize = new Dimension(,600)
      xLayoutAlignment = 0.0

      contents += new FlowPanel {

        maximumSize = new java.awt.Dimension(600, 0)
        contents += new GridPanel(2, 5) {

          border = Swing.EmptyBorder(0, 10, 0, 10)
          //maximumSize = new java.awt.Dimension(600,0)
          //minimumSize = new java.awt.Dimension(0,0)
          xLayoutAlignment = 0

          //preferredSize = new Dimension(100,50)
          vGap = 15
          hGap = 5
          opaque = false

          contents += playerIdLabel
          contents += playerIdTextBox
          contents += PlayerNamelabel
          contents += PlayerNameTextBox
        }
        opaque = false
      }

      contents += new ScrollPane(table) {

        preferredSize = new Dimension(400, 100)
        border = Swing.EmptyBorder(30, 20, 10, 20)
        opaque = false;
      }

      contents += new FlowPanel {

        contents += goBackButton
        opaque = false;
      }

      border = Swing.EmptyBorder(0, 0, 0, 0)

      listenTo(goBackButton)
      reactions += {
        case ButtonClicked(`goBackButton`) =>
          CasinoTableView.main(null)
          close()
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
      //pack

      val frame = top
      frame.visible = true
    }

  }

}
