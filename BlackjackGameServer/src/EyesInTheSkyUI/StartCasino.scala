package EyesInTheSkyUI

import scala.swing._
import event._
import javax.swing.table._
import javax.swing.JScrollPane
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import scala.swing.event.TableRowsSelected
import javax.swing.table.DefaultTableModel
import scala.swing.MainFrame
import bj.actor.GameServerStarter
import scala.actors.remote.RemoteActor

object StartCasino extends SimpleSwingApplication {

  private var bufferedImage: BufferedImage = null

  val gameServer = GameServerStarter.gameServer

  // Top frame 
  def top = new MainFrame {
    title = "Start Casino"
    peer.setLocation(500, 100)
    preferredSize = new Dimension(600, 490)
    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName())

    menuBar = new MenuBar {
      contents += new Menu("File") {
        //contents += new MenuItem("Open")
        contents += new Separator
        contents += new MenuItem("Close")
        contents += new Separator
        contents += new MenuItem("Exit") {
          action = new Action("Exit") {
            def apply = System.exit(0)
          }
        }
      }

      contents += new Menu("Options") {
        contents += new MenuItem("Start Casino") {
          action = new Action("Start Casino") {

            GameServerStarter.main(null)
            def apply = createFrame
          }
        }

        contents += new Separator
        contents += new MenuItem("Stop Casino") {
          reactions += {
            case ButtonClicked(e) => {
              close()
              //WelcomeToCasino.main(null)						                 
            }
            case _ => None
          }
        }

        contents += new Separator
        contents += new MenuItem("Reset Tables") {
          reactions += {
            case ButtonClicked(e) => {
              val newFrame = MyInternalFrame()

            }
            case _ => None
          }
        }
      }

      contents += new Menu("Help") {
        contents += new MenuItem(Action("About") {
          //new BjInfoBox(new Label("<html>Alpha version</html>"))
        })
      }
      opaque = false
      val newFrame = MyInternalFrame()
      def createFrame {
        val newFrame = MyInternalFrame()

      }

      import javax.swing.{ JDesktopPane, JInternalFrame }
      import collection.mutable.ArrayBuffer
    }

    def processAvailableTables(availableTables: List[bj.table.Table]): Array[Array[String]] = {

      var bjTablesSize = availableTables.size

      var bjTablesArray = new Array[Array[String]](bjTablesSize, 8)


      for (i <- 0 until availableTables.size) {

        bjTablesArray(i)(0) = availableTables(i).tid.toString()
        bjTablesArray(i)(1) = "$" + availableTables(i).minBet.toString()
        bjTablesArray(i)(2) = "3"
        bjTablesArray(i)(3) = availableTables(i).bets.size.toString()
        bjTablesArray(i)(4) = "$" + availableTables(i).tableEarnings.toString()
        bjTablesArray(i)(5) = "$" + availableTables(i).tableLosses.toString()
        bjTablesArray(i)(6) = "$" + availableTables(i).tableBankroll.toString()
        bjTablesArray(i)(7) = availableTables(i).tableStatus.toString()

      }

      return bjTablesArray
      //println( bjTablesArray)

    }

    object MyInternalFrame {

      def apply() = {

        //println("Tables man ")

        val model = processAvailableTables(gameServer.availableTables)

        /* val model = Array(
          Array("01", "0", "24", "Wayne", "5", "2", "$35,000", "5000", "started"),
          Array("02", "25", "49", "Rooney", "5", "5 ", "$44,000", "6700", "stopped"),
          Array("03", "50", "67", "Fernando", "5", "3 ", "$25,908", "10000", "stopped"),
          Array("04", "90", "89", "Torres", "5", "6", "$46,000", "34900", "started"),
          Array("05", "120", "200", "Lenord", "5", "7", "$45,000", "12900", "stopped"),
          Array("06", "50", "67", "Fernando", "5", "3 ", "$25,908", "10000", "started"),
          Array("07", "90", "89", "Torres", "5", "6", "$46,000", "34900", "started"),
          Array("08", "120", "200", "Lenord", "5", "7", "$45,000", "12900", "stopped"),
          Array("09", "75", "50", "Charlie", "9", "1", "$35,000", "13400", "stopped"),
          Array("10", "12", "400", "Alan", "8", "4", "$74,000", "18900", "stopped"),
          Array("", "", "", "", "", "", "", "", ""),
          Array("", "", "", "", "", "", "", "", ""))*/

        val headers = Array("Table id", "Min bet", "Max players", " No .of Players", "Earnings", "Losses", "Bankroll", "Status")

        val tableModel = new DefaultTableModel(
          model.asInstanceOf[Array[Array[Object]]],
          headers.asInstanceOf[Array[Object]])

        val bjTablesTable = new Table(model.size, headers.size) {

          model = tableModel
          showGrid = true
          border = Swing.EmptyBorder(20)
          visible = true
          enabled = false 
        }

        val starttable = new Button {
          text = "<html><b>Start Table</b></html>"
          enabled = false  
        }

        val viewtable = new Button {
          text = "<html><b>View Table</b></html>"
            enabled = false 
        }

        val addtable = new Button {
          text = "<html><b>Add Table</b></html>"
          enabled = false
          reactions += {
            case ButtonClicked(b) =>
              tableModel.addRow(Array[AnyRef]("", "", "", "", "", "", "", "", ""))
          }
        }

        val resettable = new Button {
          text = "<html><b>Remove Table</b></html>"
            enabled = false 
        }

        val exitButton = new Button {
          text = "<html><b>Exit</b></html>"
            
        }

        val stoptable = new Button {
          text = "<html><b>Stop Table</b></html>"
            enabled = false 
        }

        val buttonGrid = new GridPanel(2, 3) {
          List(starttable, addtable, viewtable, resettable, stoptable, exitButton).foreach { i => contents += i }
          border = Swing.EmptyBorder(20, 20, 20, 20)
          maximumSize = new java.awt.Dimension(200, 0)
          minimumSize = new java.awt.Dimension(0, 0)
          xLayoutAlignment = 0
          vGap = 10
          hGap = 100
          opaque = true
        }

        val title = new Label {
          text = "<html><b>Welcome to Casino</b></html> "
          horizontalTextPosition = Alignment.Center
        }

        val title1 = new Label {
          text = "<html><b>Start Black Jack Tables</b></html>"
          horizontalTextPosition = Alignment.Center
        }

        title.font = new Font("Arial", 0, 15)
        title1.font = new Font("Arial", 0, 15)

        val textgrid = new GridPanel(4, 4) {
          List(new Label(""), title, new Label(""), title1).foreach { i => contents += i }
          border = Swing.EmptyBorder(10, 10, 10, 10)
          vGap = 0
          opaque = false
        }

        contents = new BoxPanel(Orientation.Vertical) {
          background = Color.green
          opaque = false
          contents += new BorderPanel {
            import BorderPanel.Position._
            add(textgrid, North)
            add(bjTablesTable, Center)
            add(new ScrollPane(bjTablesTable) {
              border = Swing.EmptyBorder(20, 20, 20, 20)
              opaque = false
            }, Center)

            add(buttonGrid, South)
            opaque = false

            var tableNum = 0
            listenTo(starttable)
            listenTo(viewtable)
            listenTo(addtable)
            listenTo(resettable)
            listenTo(exitButton)
            listenTo(stoptable)
            reactions += {
              case ButtonClicked(`starttable`) =>
                println("Player " + selctRow + " Selected")
                startTablesMsg

              case ButtonClicked(`viewtable`) => {
                CasinoTableView.main(null)
                close()
              }
              case ButtonClicked(`addtable`) => {
           
                close()
              }
              case ButtonClicked(`resettable`) => {
                bjTablesTable.selection.rows.foreach(X => tableModel.removeRow(bjTablesTable.selection.rows.first))
              }
              case ButtonClicked(`exitButton`) => {
                
                closeOperation()
              }

              case ButtonClicked(`stoptable`) =>
                println("Player " + selctRow + " Selected")
                stopTablesMsg
            }

            var selctRow = -1

            val tblRow = bjTablesTable.selection.rows.result()
            bjTablesTable.listenTo(bjTablesTable.selection)
            bjTablesTable.reactions += {
              case tableModel => tblRow foreach { i => selctRow = i }
            }

            def startTablesMsg {

              if (tableModel.getValueAt(selctRow, 7) == "Stopped") {
                bjTablesTable.update(selctRow, 7, "Started")
              }

            }

            def stopTablesMsg {

              if (tableModel.getValueAt(selctRow, 7) == "Started") {
                bjTablesTable.update(selctRow, 7, "Stopped")
                // startButton.enabled = true
              }

            }

            bufferedImage = ImageIO.read(new File("BJT.jpg"))
            override def paintComponent(g: Graphics2D) = {
              super.paintComponent(g)
              if (null != bufferedImage) {
                g.drawImage(bufferedImage, 0, 0, null)
              }
            }

          }

        }
      }

    }

    // class defines the function of rows and columns
    class MyTableModel(var rowData: Array[Array[Any]], val columnNames: Seq[String]) extends AbstractTableModel {
      override def getColumnName(column: Int) = columnNames(column).toString

      def getRowCount() = rowData.length

      def getColumnCount() = columnNames.length

      def getValueAt(row: Int, col: Int): AnyRef = rowData(row)(col).asInstanceOf[AnyRef]

      override def isCellEditable(row: Int, column: Int) = true

      override def setValueAt(value: Any, row: Int, col: Int) {

        rowData(row)(col) = value
      }

      def addRow(data: Array[AnyRef]) {

        rowData ++= Array(data.asInstanceOf[Array[Any]])

      }

    }

  }

}



