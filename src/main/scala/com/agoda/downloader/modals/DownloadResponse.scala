package com.agoda.downloader.modals

import play.api.libs.json.Json

case class DownloadResponse(status: String, id: String)

object DownloadResponse {
  val formatter = Json.format[DownloadResponse]
}