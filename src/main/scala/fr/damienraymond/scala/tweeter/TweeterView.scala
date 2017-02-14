package fr.damienraymond.scala.tweeter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import fr.damienraymond.scala.tweeter.Tweeter.Retweet
import fr.damienraymond.scala.tweeter.TweeterView.{GuiRetweet, RegisterTweeter, RetweetView, TweetView}


/**
  * Tweeter view interface
  */

class TweeterView(name: String, view: ITweeterViewGUI)
  extends Actor
    with ActorLogging
    with ThreadSafeRun {

  safeRun(view.createAndShowGUI(s"Tweeter $name"))

  // Fix cyclic dependency by giving `this` to the view (GUI)
  safeRun(view.setTweeterView(self))

  // Store last tweet to be able to handle rts
  var lastTweetOpt: Option[TweetView] = Option.empty

  var tweeterOpt: Option[ActorRef] = Option.empty

  override def receive: Receive = {
    case tweet @ TweetView(author, content, isFromMe) =>
      lastTweetOpt = Some(tweet) // store lt
      safeRun(view.displayTweet(author, content, isFromMe))

    case RetweetView(retweeter, targetName, targetContent, isFromMe) =>
      //lastTweetOpt = Some(tweet) // ?
      safeRun(view.displayRetweet(retweeter, targetName, targetContent, isFromMe))

    case RegisterTweeter(tweeterRef) =>
      tweeterOpt = Some(tweeterRef)

    case GuiRetweet => retweet()
  }


  def retweet(): Unit = (lastTweetOpt, tweeterOpt) match {
    case (Some(lt), Some(tweeter)) =>
      log.debug("Send retweet message to Tweeter")
      tweeter ! Retweet(name, lt.author, lt.content)
    case _ =>
      // show error message
      log.error("lastTweetOpt or/and tweeterOpt are not bound")
  }
}

object TweeterView {

  case class TweetView(author: String, content: String, isFromMe: Boolean)
  case class RetweetView(retweeter: String, targetName: String, targetContent: String, isFromMe: Boolean)

  case class RegisterTweeter(tweeter: ActorRef)

  case object GuiRetweet

  def apply(name: String): ActorRef = {
    val view = new TweeterViewGUI
    Registry.system.actorOf(Props(new TweeterView(name, view)))
  }

}
