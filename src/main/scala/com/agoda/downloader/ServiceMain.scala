package com.agoda.downloader

import java.sql.{Connection, DriverManager, Statement}

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.Materializer
import akka.util.Timeout
import com.agoda.downloader.actors.ProgressLogger
import com.agoda.downloader.routes.DownloaderApiImpl
import com.agoda.downloader.utils.DownloaderUtils.getConnection
import com.typesafe.config.{Config, ConfigFactory}

object ServiceMain extends App with RequestTimeout {
  val config = ConfigFactory.load()
  val host = config.getString("application.host")
  val port = config.getInt("application.port")

  setupH2DB()

  implicit val system: ActorSystem = ActorSystem("Agoda-Downloader")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  val processLogger = system.actorOf(Props.create(classOf[ProgressLogger]))
  val routes = new DownloaderApiImpl(system, requestTimeout(config), processLogger).routes

  val bindingFuture: Future[ServerBinding] = Http().newServerAt(host, port).bind(routes)

  val log = Logging(system.eventStream, "Agoda-Downloader")

  try {
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress}")
    }
  } catch {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}!", host, port)
      system.terminate()
  }

  def setupH2DB() = {
    val con = getConnection
    val stm = con.createStatement
    val sql =
      """
        |create table if not exists download_progress (
        |   ID BIGINT AUTO_INCREMENT PRIMARY KEY,
        |   DOWNLOAD_ID VARCHAR(50),
        |   FILE_NAME VARCHAR(500),
        |   DOWNLOAD_SPEED_KBS FLOAT(24),
        |   DOWNLOAD_PROGRESS FLOAT(24),
        |   LAST_UPDATED BIGINT
        | );
        |""".stripMargin

    stm.execute(sql)
  }
}

trait RequestTimeout {

  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}