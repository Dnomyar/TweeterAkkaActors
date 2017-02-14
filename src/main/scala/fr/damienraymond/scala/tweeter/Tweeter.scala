package fr.damienraymond.scala.tweeter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import fr.damienraymond.scala.tweeter.Registry.{Bind, Lookup, LookupAnswer}
import fr.damienraymond.scala.tweeter.Tweeter._
import fr.damienraymond.scala.tweeter.TweeterView.{RegisterTweeter, RetweetView, TweetView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class Tweeter(name: String, registry: ActorRef, viewRef: ActorRef)
  extends Actor
    with ActorLogging {

  log.debug(s"Creating actor named $name : $self")

  // Register actor to Register
  log.info(s"$name => Bind($name, this)")
  registry ! Bind(name, self)

  // Register actor to it
  viewRef ! RegisterTweeter(self)

  var followers: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case Tweet(content) =>
      log.info(s"$name => Tweet($content)")
      log.debug(s"TRACE -> $name's followers : $followers")
      self ! HandleTweet(name, content, isFromMe = true)
      followers.foreach(_ ! HandleTweet(name, content))

    case HandleTweet(user, content, isFromMe) =>
      log.info(s"$name => Received tweet from $user : $content")
      viewRef ! TweetView(user, content, isFromMe)

    case Follow(user) =>
      log.info(s"$name => Follow($user)")
      getUser(user).foreach(_.foreach(userRef => userRef ! AddFollower(self)))

    case AddFollower(user) =>
      log.info(s"$name => AddFollower($user)")
      followers += user

    case Retweet(retweeter, targetName, targetContent) =>
      // Ask to show his/her own rt
      self ! HandleRetweet(retweeter, targetName, targetContent, isFromMe = true)
      // Ask to show his/her rt to his/her followers
      followers.foreach(_ ! HandleRetweet(retweeter, targetName, targetContent))

    case HandleRetweet(retweeter, targetName, targetContent, isFromMe) =>
      viewRef ! RetweetView(retweeter, targetName, targetContent, isFromMe)
  }


  /**
    * Get user from registry from it name
    */
  def getUser(username: String): Future[Option[ActorRef]] = {
    log.debug(s"$name => Lookup($username)")
    (registry ? Lookup(username)) map {
      case LookupAnswer(Some(ar: ActorRef)) =>
        log.debug(s"$name => LookupAnswer(Some($ar))")
        Some(ar)
      case res =>
        log.debug(s"$name => LookupAnswer($res)")
        None
    }
  }
}


object Tweeter {

  case class Tweet(content: String)
  case class HandleTweet(user: String, content: String, isFromMe: Boolean = false)

  case class Follow(user: String)
  case class AddFollower(user: ActorRef)

  case class Retweet(retweeter: String, targetName: String, targetContent: String)
  case class HandleRetweet(retweeter: String, targetName: String, targetContent: String, isFromMe: Boolean = false)


  /**
    * Create actor and it actor's view
    */
  def apply(name: String, registry: ActorRef): ActorRef = {
    val viewRef = TweeterView(name)
    Registry.system.actorOf(Props(new Tweeter(name, registry, viewRef)))
  }

}

