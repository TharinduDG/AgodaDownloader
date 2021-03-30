package com.agoda.downloader.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.agoda.downloader.actors.DownloadManagerActor

import scala.concurrent.ExecutionContext

class DownloaderApiImpl(system: ActorSystem, timeout: Timeout, progressLogger: ActorRef) extends DownloaderRoutes {
  val downloadManagerActor: ActorRef = system.actorOf(Props.create(classOf[DownloadManagerActor], progressLogger))

  override implicit def ec: ExecutionContext = system.dispatcher

  override implicit def requestTimeout: Timeout = timeout

  override def downloadManager: ActorRef = downloadManagerActor
}