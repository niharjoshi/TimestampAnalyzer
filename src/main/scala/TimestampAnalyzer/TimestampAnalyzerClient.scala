package TimestampAnalyzer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.Success
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings

object TimestampAnalyzerClient {

  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "GreeterClient")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = TimestampAnalyzerServiceClient(GrpcClientSettings.fromConfig("TimestampAnalyzer.TimestampAnalyzerService"))

    val time_stamp :: time_interval :: bucket :: _ = args.toList

    analyzeTimestampReply(time_stamp, time_interval, bucket)


    def analyzeTimestampReply(time_stamp: String, time_interval: String, bucket: String): Unit = {
      println(s"Time stamp: $time_stamp")
      println(s"Time interval: +-$time_interval")
      println(s"Reading data from S3 bucket: $bucket")
      val reply = client.analyzeTimestamp(AnalyzeTimestampRequest(time_stamp, time_interval, bucket))
      reply.onComplete {
        case Success(msg) =>
          println(msg)
        case Failure(e) =>
          println(s"Error: $e")
      }
    }


  }

}