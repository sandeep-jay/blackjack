package bj.test
import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit.Test
import org.apache.log4j.PropertyConfigurator
import bj.util.Logs
import bj.actor.House
import bj.actor.Player
import bj.actor.Go

/**
 * This class tests three players, two at different same table.
 * @author Ron Coleman, Ph.D.
 */
class TwoPlayer01 extends Logs {
  PropertyConfigurator.configure("log4j.properties")
  
  @Test
  def test {
    debug("starting the house")
    House.start

    Thread.sleep(1000)

    debug("starting players")
    val players = List[Player](
      new Player("Ron", 100, 30,true),
      new Player("Bob",200,5,true),
      new Player("Alice",300,10,true)
    )

    Player.start(players)

    Thread.sleep(1000)

    debug("telling house go")
    House ! Go
    
    Thread.sleep(5000)
    
    assert(players(0).bankroll == 100)
    assert(players(1).bankroll == 205)
    assert(players(2).bankroll == 310)    
  }
}