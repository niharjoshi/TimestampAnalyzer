package TimestampAnalyzer

// Importing necessary packages
import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import HelperUtils.CreateLogger


object TimestampAnalyzerClient {

  // Initializing logger
  private val logger = CreateLogger(classOf[TimestampAnalyzerClient.type])

  // This is the gRPC client driver function
  def main(args: Array[String]): Unit = {

    // Defining the Actor System and Execution Context for Akka framework
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "TimestampAnalyzerClient")
    implicit val ec: ExecutionContext = sys.executionContext

    // Setting our client from the proto-generated service
    val client = TimestampAnalyzerServiceClient(GrpcClientSettings.fromConfig("TimestampAnalyzer.TimestampAnalyzerService"))

    // Unpacking the passed command line arguments into input variables
    val time_stamp :: time_interval :: bucket :: _ = args.toList

    // Calling both the RPC methods (communication via POST, GET)
    analyzeTimestampReply(time_stamp, time_interval, bucket)
    requestLogsReply(time_stamp, time_interval, bucket)


    // This method invokes the first RPC method from the proto-generated client service
    def analyzeTimestampReply(time_stamp: String, time_interval: String, bucket: String): Unit = {

      logger.info("Using POST request")
      logger.info(s"Time stamp: $time_stamp")
      logger.info(s"Time interval: +-$time_interval")
      logger.info(s"Reading data from S3 bucket: $bucket")

      // Calling the RPC method
      val reply = client.analyzeTimestamp(AnalyzeTimestampRequest(time_stamp, time_interval, bucket))

      // Ensuring completion of RPC
      reply.onComplete {
        // On success
        case Success(msg) =>
          logger.info(s"Msg: $msg")
          println(s"analyzeTimestampReply (POST): $msg")
        // On failure
        case Failure(e) =>
          logger.info(s"Error: $e")
      }

    }

    // This method invokes the second RPC method from the proto-generated client service
    def requestLogsReply(time_stamp: String, time_interval: String, bucket: String): Unit = {

      logger.info("Using GET request")
      logger.info(s"Time stamp: $time_stamp")
      logger.info(s"Time interval: +-$time_interval")
      logger.info(s"Reading data from S3 bucket: $bucket")

      // Calling the RPC method
      val reply = client.requestLogs(AnalyzeTimestampRequest(time_stamp, time_interval, bucket))

      // Ensuring completion of RPC
      reply.onComplete {
        // On success
        case Success(msg) =>
          logger.info(s"Msg: $msg")
          println(s"requestLogsReply (GET): $msg")
        // On failure
        case Failure(e) =>
          logger.info(s"Error: $e")
      }

    }

  }

}