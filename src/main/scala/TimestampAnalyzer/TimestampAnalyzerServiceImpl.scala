package TimestampAnalyzer

import HelperUtils.ObtainConfigReference

import scala.concurrent.Future
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.BroadcastHub
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.MergeHub
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import spray.json._
import scalaj.http.{Http, HttpOptions}

class TimestampAnalyzerServiceImpl(system: ActorSystem[_]) extends TimestampAnalyzerService {
  private implicit val sys: ActorSystem[_] = system

  val config = ObtainConfigReference("TimestampAnalyzer") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }


  override def analyzeTimestamp(request: AnalyzeTimestampRequest): Future[AnalyzeTimestampReply] = {

    val endpoint = config.getString("TimestampAnalyzer.API_Gateway_Endpoint")

    val req = s"""{
                 |  "time_stamp": "${request.timeStamp}",
                 |  "time_interval": "${request.timeInterval}",
                 |  "bucket": "${request.bucket}"
                 |}""".stripMargin

    val res = Http(endpoint).postData(req)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString

    Future.successful(AnalyzeTimestampReply(1))
  }

}