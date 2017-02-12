package fr.damienraymond.scala.tweeter

import fr.damienraymond.scala.tweeter.Tweeter._

/**
  * Object to test Tweeter
  */
object TestTweeter extends App {

  test2()

  def test1(): Unit = {
    val r = Registry("registry")
    val bob = Tweeter("bob", r)
    val alice = Tweeter("alice", r)
    alice ! Tweet("I am Alice")
    bob ! Follow("alice")

    Thread.sleep(1000)

    alice ! Tweet("I am Alice")
  }


  def test2(): Unit = {
    val r = Registry("registry")

    val bob = Tweeter("bob", r)
    val alice = Tweeter("alice", r)
    val carol = Tweeter("carol", r)

    bob ! Follow("alice")
    carol ! Follow("bob")

    Thread.sleep(1000)

    alice ! Tweet("I am Alice")
  }

}
