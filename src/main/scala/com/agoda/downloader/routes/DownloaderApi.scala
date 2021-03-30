package com.agoda.downloader.routes

import java.nio.file.Paths

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.agoda.downloader.actors.DownloadManagerActor.{DownloadJob, DownloadResponse}

import scala.concurrent.{ExecutionContext, Future}

trait DownloaderApi {
  implicit def ec: ExecutionContext
  implicit def requestTimeout: Timeout

  def downloadManager: ActorRef

  val defaultLocation: String = Paths.get(getClass.getResource("/downloads").toURI).toString

  def downloadFile(urls: List[String]): Future[DownloadResponse] = {
    (downloadManager ? DownloadJob(urls, defaultLocation)).mapTo[DownloadResponse]
  }
}
