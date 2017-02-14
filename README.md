# Tweeter

Tweeter is a **very minimalist** "Twitter-like" application.

Tweeter is a school project (Jacques Noyé's Scala lesson) to train us to use Akka Actors.
The aim of this project is to learn how to build **message based app** (opposite to *usual* memory-shared ones).

Note : information about scala and akka version are available in `build.sbt`.

## How to test ?
To test just run `fr.damienraymond.scala.tweeter.Tweeter.TestTweeter`.

To do that, you can run `sbt run`.

## Choices

### How to handle more than one follows
If you asks to follow two times one person, a tweet should be shown more than once.

I have used a **set** data structure to avoid duplicated values.
Two ActorRef from the same actor are equal, so they can be duplicated.


### Separation between tweet and retweet
Conceptually a retweet is a tweet. It is easier to manage both separately especially for the display.

I have chosen to create a tweet class and a retweet class.

### Usage of pattern `ask`
In `Tweeter`, we had to ask to the registry which `ActorRef` matches with a given `name`. Then registry send back an answer (message).

As this process is unique, a simple exchange of messages using `!` could be enough. But, I think it's more logical to use pattern `ask` to **keep the context**.

Pattern `ask` return a `Future`. This can cause performance implications ([http://doc.akka.io/docs/akka/current/scala/actors.html#Send_messages](http://doc.akka.io/docs/akka/current/scala/actors.html#Send_messages)).


### Are you the author of the tweet ?
I have chosen to add a flag to know whether you are the author of the tweet or not. It helps me to simplify the tweet display.

Another (best) solution would have been to spit a case class with a flag into two. Example :
```
// from :
case class HandleTweet(user: String, content: String, isFromMe: Boolean = false)

// to :
case class HandleYourTweet(user: String, content: String)
case class HandleTweet(user: String, content: String)
```


### Handle thread safe calls between GUI and ViewActor
To handle thread safe calls between GUI and ViewActor I created this trait :
```
trait ThreadSafeRun {

  def safeRun(block: => Any) =
    javax.swing.SwingUtilities.invokeLater(() => {
      block
    })

}
```


I used `safeRun` for each call from ViewActor to the GUI.

On the other direction (GUI to ViewActor) I used message sending (`!`).