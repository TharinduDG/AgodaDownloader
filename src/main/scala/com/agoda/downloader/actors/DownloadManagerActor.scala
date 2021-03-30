package com.agoda.downloader.actors

import java.io.IOException
import java.net.UnknownHostException
import java.util.UUID

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, Terminated}
import com.agoda.downloader.actors.DownloadManagerActor._
import com.agoda.downloader.utils.DownloaderUtils._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class DownloadManagerActor(progressLogger: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case DownloadJob(urls: List[String], location: String) =>
      val downloadId = getDownloadId
      urls.foreach(url => {
        val protocol = extractProtocol(url)
        protocol match {
          case "http" | "ftp" | "https" =>
            val downloader = context.actorOf(Props(new DefaultDownloadActor(progressLogger)), s"DefaultDownloadActor-${UUID.randomUUID()}")
            downloader ! DownloadFile(downloadId, url, location)
          case "sftp" =>
            val downloader = context.actorOf(Props(new SFTPDownloadActor(progressLogger)), s"SFTPDownloadActor-${UUID.randomUUID()}")
            downloader ! DownloadFile(downloadId, url, location)
          case _ =>
            sender() ! InvalidProtocol
        }
      })
      sender() ! DownloadStarted(downloadId)
    case FileDownloaded(_) => sender ! PoisonPill
    case FileDownloadFailed(_, _) => sender ! PoisonPill
  }

  def createActor[T](actorClass: Class[T], name: String, args: AnyRef*): ActorRef = context.actorOf(Props.create(actorClass, args), name)

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy(3, 4 seconds) {
    case _: UnknownHostException => Stop
    case _: IOException => Restart
    case _: Exception => Stop
  }
}

object DownloadManagerActor {

  sealed trait DownloadRequest

  final case class DownloadJob(urls: List[String], location: String) extends DownloadRequest

  final case class DownloadFile(id: String, url: String, location: String) extends DownloadRequest

  final case class FileDownloaded(filePath: String) extends DownloadRequest

  final case class FileDownloadFailed(filePath: String, exception: Throwable) extends DownloadRequest

  final case class DownloadProgressEvent(id: String, fileName: String, downloadSpeedKBS: Double, downloadProgress: Double) extends DownloadRequest

  sealed trait DownloadResponse

  final case class DownloadStarted(id: String) extends DownloadResponse

  final case object InvalidProtocol extends DownloadResponse

}
