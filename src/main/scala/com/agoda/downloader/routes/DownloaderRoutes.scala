package com.agoda.downloader.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.agoda.downloader.actors.DownloadManagerActor._
import com.agoda.downloader.modals.{DownloadProgressRequest, DownloadRequest, DownloadResponse}
import com.agoda.downloader.utils.RouteMarshaller
import org.slf4j.LoggerFactory

trait DownloaderRoutes extends DownloaderApi with RouteMarshaller {
  val service = "downloader"
  val logger = LoggerFactory.getLogger(classOf[DownloaderRoutes])

  protected val downloadRoute: Route = {
    pathPrefix(service / "download") {
      put {
        pathEndOrSingleSlash {
          entity(as[DownloadRequest]) { r =>
            onSuccess(downloadFile(r.urls)) {
              case DownloadStarted(id) => complete(Created, DownloadResponse("Started", id))
              case InvalidProtocol => complete(BadRequest, "Invalid Protocol")
            }
          }
        }
      }
    }
  }

  protected val downloadStatusCheckRoute: Route = {
    pathPrefix(service / "download") {
      get {
        pathEndOrSingleSlash {
          entity(as[DownloadProgressRequest]) { r =>
            complete(Created, s"Downloading...")
          }
        }
      }
    }
  }

  val routes: Route = downloadRoute ~ downloadStatusCheckRoute
}
