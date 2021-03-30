package com.agoda.downloader.modals

import play.api.libs.json.{Json, OFormat}

case class DownloadRequest(urls: List[String])

object DownloadRequest {
  implicit val formatter: OFormat[DownloadRequest] = Json.format[DownloadRequest]
}
