package com.agoda.downloader.utils

import java.io._
import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}
import java.util.UUID

import com.agoda.downloader.actors.DownloadManagerActor.DownloadProgressEvent
import com.agoda.downloader.modals.SftpRequest

import scala.annotation.tailrec
import scala.util.Try

object DownloaderUtils {
  def getOutputPath(url: String, downloadId: String, location: String): String = {
    val fileName = extractFileName(url)
    val filePath = s"$location/$downloadId/$fileName"
    filePath
  }

  def extractProtocol(urlString: String): String = if (urlString.indexOf("://") != -1) urlString.substring(0, urlString.indexOf("://")) else ""

  def directoryExists(directory: String): Boolean = {
    Files.exists(Paths.get(directory))
  }

  def createDownloadDestination(destinationPath: String, downloadFolder: String): Unit = {
    Files.createDirectories(Paths.get(destinationPath, downloadFolder))
  }

  def deleteFileIfExists(filePath: String): Boolean = {
    val path = Paths.get(filePath)
    Files.deleteIfExists(path)
  }

  def extractSftpParameters(s: String): Try[SftpRequest] = {
    Try {
      val protocolAndTail = s.split("://")
      val usernameAndTail = protocolAndTail.last.split(":")
      val username = usernameAndTail.head
      val passwordAndTail = usernameAndTail.last.split("@")
      val password = passwordAndTail.head
      val hostnameAndTail = passwordAndTail.last.split(";")
      val hostname = hostnameAndTail.head
      val path = hostnameAndTail.last
      SftpRequest(username, password, hostname, path)
    }
  }

  def extractFileName(url: String): String = {
    val fileName = url.substring(url.lastIndexOf("/") + 1)
    fileName match {
      case "" =>
        val urlWithoutProtocol = url.split("://").last
        urlWithoutProtocol.substring(0, urlWithoutProtocol.indexOf("/"))
      case _ => fileName
    }
  }

  def getDownloadId: String = UUID.randomUUID().toString

  def downloadFile(downloadId: String, filePath: String, inputStream: InputStream, contentLength: Int, progressCallback: DownloadProgressEvent => Unit): Unit = {
    val bufferSize = 4096
    val in = new BufferedInputStream(inputStream)
    val out = new BufferedOutputStream(new FileOutputStream(filePath), 4096)
    val buffer = new Array[Byte](bufferSize)
    val downloadStartTime = System.currentTimeMillis()
    val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)

    @tailrec
    def writeBytes(in: InputStream, out: OutputStream, downloadedContentLength: Int): Unit = {
      val readLength = in.read(buffer, 0, bufferSize)
      if (readLength >= 0) {
        out.write(buffer, 0, readLength)
        out.flush()
        val currentProgress = f"${(1.0 * downloadedContentLength / contentLength) * 100}%2.1f".toDouble
        val timeElapsedMillis = System.currentTimeMillis() - downloadStartTime
        val downloadSpeedKBS = f"${1.0 * readLength * 1000 / (1024 * timeElapsedMillis)}%4.2f".toDouble
        progressCallback(DownloadProgressEvent(downloadId, fileName, downloadSpeedKBS, currentProgress))

        writeBytes(in, out, downloadedContentLength + readLength)
      } else if (readLength < 0 && downloadedContentLength > 0) {
        progressCallback(DownloadProgressEvent(downloadId, fileName, 0.0, 100.0))
      }
    }

    writeBytes(in, out, 0)

    out.close()
    in.close()
  }

  def getConnection: Connection = {
    val PARENT_DIR = "./db"
    val DATABASE_NAME = "agoda-downloader"
    val DATABASE_DIR = s"$PARENT_DIR/$DATABASE_NAME"
    val DATABASE_URL = s"jdbc:h2:$DATABASE_DIR;MODE=MYSQL;DB_CLOSE_DELAY=-1"

    DriverManager.getConnection(DATABASE_URL)
  }
}
