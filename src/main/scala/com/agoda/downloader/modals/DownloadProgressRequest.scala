package com.agoda.downloader.modals

import play.api.libs.json.{Json, OFormat}

case class DownloadProgressRequest(id: String)

object DownloadProgressRequest {
  implicit val formatter: OFormat[DownloadProgressRequest] = Json.format[DownloadProgressRequest]
}