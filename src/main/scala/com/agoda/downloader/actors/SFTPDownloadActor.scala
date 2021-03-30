package com.agoda.downloader.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.agoda.downloader.actors.DownloadManagerActor._
import com.agoda.downloader.modals.SftpRequest
import com.agoda.downloader.utils.DownloaderUtils.{createDownloadDestination, deleteFileIfExists, downloadFile, extractSftpParameters, getOutputPath}
import com.jcraft.jsch.{ChannelSftp, JSch, Session}

import scala.util.{Failure, Success, Try}

class SFTPDownloadActor(progressLogger: ActorRef) extends Actor with ActorLogging {
  def receive: Receive = {
    case DownloadFile(id: String, url: String, location: String) =>
      val sftpRequest = extractSftpParameters(url)

      sftpRequest match {
        case Success(SftpRequest(username, password, hostname, path)) =>
          createDownloadDestination(location, id)
          val filePath = getOutputPath(url, id, location)
          Try {
            val (session, sftpChannel) = getSFTPChannel(username, password, hostname)
            downloadFileFromChannel(id, path, filePath, session, sftpChannel, e => progressLogger ! e)
          } match {
            case Success(_) => sender ! FileDownloaded(filePath)
            case Failure(ex) =>
              deleteFileIfExists(filePath)
              sender ! FileDownloadFailed(filePath, ex)
          }
        case Failure(ex) =>
          sender ! FileDownloadFailed(url, ex)
      }
  }

  def downloadFileFromChannel(downloadId: String, path: String, filePath: String, session: Session, sftpChannel: ChannelSftp, progressCallback: DownloadProgressEvent => Unit): Unit = {
    val fileSize = sftpChannel.lstat(path).getSize.toInt
    downloadFile(downloadId, filePath, sftpChannel.get(path), fileSize, progressCallback)
    sftpChannel.exit()
    session.disconnect()
  }

  def getSFTPChannel(username: String, password: String, hostname: String): (Session, ChannelSftp) = {
    val session = new JSch().getSession(username, hostname, 22)
    session.setConfig("StrictHostKeyChecking", "no")
    session.setPassword(password)
    session.connect()
    session.setTimeout(5000)
    val channel = session.openChannel("sftp")
    channel.connect()
    val sftpChannel = channel.asInstanceOf[ChannelSftp]
    (session, sftpChannel)
  }
}
