package com.agoda.downloader.actors

import java.io.File
import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.agoda.downloader.actors.DownloadManagerActor._
import com.agoda.downloader.utils.DownloaderUtils._

import scala.util.{Failure, Success, Try}

class DefaultDownloadActor(progressLogger: ActorRef) extends Actor with ActorLogging {
  def receive: Receive = {
    case DownloadFile(id: String, url: String, location: String) =>
      createDownloadDestination(location, id)
      val filePath = getOutputPath(url, id, location)

      Try {
        val connection = new URL(url).openConnection()
        connection.setReadTimeout(5000)
        downloadFile(id, filePath, connection.getInputStream, connection.getContentLength, e => progressLogger ! e)
      } match {
        case Success(_) => sender ! FileDownloaded(filePath)
        case Failure(ex) =>
          ex.printStackTrace()
          log.error(ex.getMessage)
          deleteFileIfExists(filePath)
          sender ! FileDownloadFailed(filePath, ex)
      }
  }

}