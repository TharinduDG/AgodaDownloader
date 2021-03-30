package com.agoda.downloader.actors

import akka.actor.Actor
import com.agoda.downloader.actors.DownloadManagerActor.DownloadProgressEvent
import com.agoda.downloader.utils.DownloaderUtils.getConnection
import com.typesafe.config.ConfigFactory

class ProgressLogger extends Actor {
  val config = ConfigFactory.load()
  val host = config.getString("application.slow-speed-threshold-KBS")
  val port = config.getInt("application.small-file-threshold-MB")

  val con = getConnection
  val stm = con.createStatement

  override def receive: Receive = {
    case e@DownloadProgressEvent(id, fileName, downloadSpeedKBS, downloadProgress) =>
      val updateQuery = s"""insert into download_progress(DOWNLOAD_ID, FILE_NAME, DOWNLOAD_SPEED_KBS, DOWNLOAD_PROGRESS, LAST_UPDATED)
        |values ('$id', '$fileName', $downloadSpeedKBS, $downloadProgress, ${System.currentTimeMillis()})
        |""".stripMargin
      stm.executeUpdate(updateQuery)
      println(e)
  }
}
