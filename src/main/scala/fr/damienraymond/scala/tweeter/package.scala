package fr.damienraymond.scala

import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

package object tweeter {

  // package global timeout
  implicit val timeout: Timeout = 1 second

}