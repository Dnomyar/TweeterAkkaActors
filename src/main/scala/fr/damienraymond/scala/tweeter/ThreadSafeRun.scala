package fr.damienraymond.scala.tweeter

/**
  * Created by damien on 14/02/2017.
  */
trait ThreadSafeRun {

  def safeRun(block: => Any) =
    javax.swing.SwingUtilities.invokeLater(() => {
      block
    })

}
