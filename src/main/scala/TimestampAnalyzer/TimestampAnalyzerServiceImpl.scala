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
import requests.post
import ujson.read

class TimestampAnalyzerServiceImpl(system: ActorSystem[_]) extends TimestampAnalyzerService {
  private implicit val sys: ActorSystem[_] = system

  val config = ObtainConfigReference("TimestampAnalyzer") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }


  override def analyzeTimestamp(request: AnalyzeTimestampRequest): Future[AnalyzeTimestampReply] = {

    val endpoint = config.getString("TimestampAnalyzer.API_Gateway_Endpoint") + "/analyze"

    val req = s"""{
                 |  "time_stamp": "${request.timeStamp}",
                 |  "time_interval": "${request.timeInterval}",
                 |  "bucket": "${request.bucket}"
                 |}""".stripMargin

    val res = post(endpoint, data = req)

    val json_output = ujson.read(res.text()).toString()

    Future.successful(AnalyzeTimestampReply(json_output))
  }

}