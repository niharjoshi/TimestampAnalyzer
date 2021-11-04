package TimestampAnalyzer

import HelperUtils.ObtainConfigReference

import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import requests.{RequestFailedException, post}
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

    try {

      val res = post(endpoint, data = req)
      val json_output = read(res.text()).toString()
      Future.successful(AnalyzeTimestampReply(json_output))

    } catch{

      case e: RequestFailedException => {
        Future.successful(AnalyzeTimestampReply("404 - no matching logs found within the given time range"))
      }

      case _: Throwable => Future.successful(AnalyzeTimestampReply("Some other runtime exception occurred"))
    }
  }

}