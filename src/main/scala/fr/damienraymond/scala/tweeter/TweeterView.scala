package fr.damienraymond.scala.tweeter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import fr.damienraymond.scala.tweeter.Tweeter.Retweet
import fr.damienraymond.scala.tweeter.TweeterView.{RegisterTweeter, RetweetView, TweetView}


/**
  * Tweeter view interface
  */
trait ITweeterView {
  def retweet(): Unit
}


class TweeterView(name: String, view: ITweeterViewGUI)
  extends Actor
    with ITweeterView
    with ActorLogging  {

  view.init(s"Tweeter $name")

  // Fix cyclic dependency by giving `this` to the view (GUI)
  view.setTweeterView(this)

  // Store last tweet to be able to handle rts
  var lastTweetOpt: Option[TweetView] = Option.empty

  var tweeterOpt: Option[ActorRef] = Option.empty

  override def receive: Receive = {
    case tweet @ TweetView(author, content, isFromMe) =>
      lastTweetOpt = Some(tweet) // store lt
      view.displayTweet(author, content, isFromMe)

    case RetweetView(retweeter, targetName, targetContent, isFromMe) =>
      //lastTweetOpt = Some(tweet) // ?
      view.displayRetweet(retweeter, targetName, targetContent, isFromMe)

    case RegisterTweeter(tweeterRef) =>
      tweeterOpt = Some(tweeterRef)
  }

  override def retweet(): Unit = (lastTweetOpt, tweeterOpt) match {
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


  def apply(name: String): ActorRef = {
    val view = new TweeterViewGUI
    Registry.system.actorOf(Props(new TweeterView(name, view)))
  }

}
