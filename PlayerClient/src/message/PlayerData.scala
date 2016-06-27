package message


@serializable case class PlayerData {
  
  var playerID = 1
  var tableID = 0
  var playerName = " "
  var playerBankroll = 0.0
  var playerBetAmount = 0.0
    

}

case class RequestStratergy(stratergy : String , gamePlayData : GamePlayData)
case class TableData(tid : Int)
case class GameComplete(flush : String)
case class GameEnded