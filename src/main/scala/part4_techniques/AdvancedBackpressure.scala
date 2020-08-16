package part4_techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

object AdvancedBackpressure extends App {

  implicit val system = ActorSystem("AdvancedBackpressure")
  implicit val materializer = ActorMaterializer()

  // control backpressure
  val controlledFlow = Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, nInstances: Int = 1)
  case class Notification(email: String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("Service discovery failed", new Date),
    PagerEvent("Illegal elements in the data pipeline", new Date),
    PagerEvent("Number of HTTP 500 spiked", new Date),
    PagerEvent("A service stopped responding", new Date)
  )
  val eventSource = Source(events)

  val oncallEngineer = "daniel@rockthejvm.com" // a fast service for fetching oncall emails

  def sendEmail(notification: Notification) =
    println(s"Dear ${notification.email}, you have an event: ${notification.pagerEvent}") // actually send an email

  val notificationSink = Flow[PagerEvent].map(event => Notification(oncallEngineer, event))
    .to(Sink.foreach[Notification](sendEmail))
  // standard
    eventSource.to(notificationSink).run()
}
