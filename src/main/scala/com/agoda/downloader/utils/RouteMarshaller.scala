package com.agoda.downloader.utils

import com.agoda.downloader.modals.{DownloadProgressRequest, DownloadRequest, DownloadResponse}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.OFormat

trait RouteMarshaller extends PlayJsonSupport {
  implicit val downloadRequest: OFormat[DownloadRequest] = DownloadRequest.formatter
  implicit val downloadProgressCheckRequest: OFormat[DownloadProgressRequest] = DownloadProgressRequest.formatter
  implicit val downloadResponse: OFormat[DownloadResponse] = DownloadResponse.formatter
}
