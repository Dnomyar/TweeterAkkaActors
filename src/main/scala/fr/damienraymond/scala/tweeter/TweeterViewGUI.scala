package fr.damienraymond.scala.tweeter

import javax.swing.{JButton, JFrame, JPanel, JTextArea}


/**
  * `Interface` of the GUI
  */
trait ITweeterViewGUI {

  def init(title: String): Unit

  def displayTweet(author: String, content: String, isFromMe: Boolean): Unit
  def displayRetweet(retweeter: String, targetName: String, targetContent: String, isFromMe: Boolean): Unit

  def setTweeterView(view: TweeterView): Unit

}


class TweeterViewGUI
  extends JFrame
    with ITweeterViewGUI {

  val panel = new JPanel
  val tweetsTextArea = new JTextArea
  val rtButton = new JButton("RT !")

  var tweeterViewActor: Option[TweeterView] = Option.empty

  rtButton.addActionListener { e =>
    println("TRACE : press button RT")
    tweeterViewActor match {
      case Some(v) =>
        println("TRACE : Launch rt")
        v.retweet()
      case None =>
        // Show error message
        println("TRACE : error tweeterViewActor is not bound")
    }
  }

  override def init(title: String): Unit =
    javax.swing.SwingUtilities.invokeLater(() => {
      createAndShowGUI(title)
    })

  def createAndShowGUI(title: String): Unit = {
    this.setTitle(title)
    this.setVisible(true)
    this.setSize(400, 100)
    this.setLocationRelativeTo(null)
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    panel.add(tweetsTextArea)
    panel.add(rtButton)

    this.setContentPane(panel)
  }


  override def displayTweet(author: String, content: String, isFromMe: Boolean): Unit =
    if(isFromMe)
      tweetsTextArea.append(s"you tweeted : $content\n")
    else
      tweetsTextArea.append(s"@$author : $content\n")


  override def displayRetweet(retweeter: String, targetName: String, targetContent: String, isFromMe: Boolean): Unit =
    if(isFromMe)
      tweetsTextArea.append(s"you have retweeted $targetName : $targetContent\n")
    else
      tweetsTextArea.append(s"@$retweeter has retweeted $targetName : $targetContent\n")



  override def setTweeterView(view: TweeterView): Unit =
    tweeterViewActor = Some(view)
}
