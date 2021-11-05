package TimestampAnalyzer

// Importing necessary packages
import HelperUtils.ObtainConfigReference
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import requests.{RequestFailedException, post, get}
import ujson.read
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.client.util.StringContentProvider


class TimestampAnalyzerServiceImpl(system: ActorSystem[_]) extends TimestampAnalyzerService {

  // Declaring Actor System
  private implicit val sys: ActorSystem[_] = system

  // Initializing config reader
  val config = ObtainConfigReference("TimestampAnalyzer") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  // This function is the RPC implementation for communicating via POST through AWS API Gateway to our Lambda function
  override def analyzeTimestamp(request: AnalyzeTimestampRequest): Future[AnalyzeTimestampReply] = {

    // Setting the AWS API Gateway endpoint that connects to our Lambda function
    val endpoint = config.getString("TimestampAnalyzer.API_Gateway_Endpoint") + "/analyze"

    // Creating the JSON object to pass as Lambda function input
    val req = s"""{
                 |  "time_stamp": "${request.timeStamp}",
                 |  "time_interval": "${request.timeInterval}",
                 |  "bucket": "${request.bucket}"
                 |}""".stripMargin

    try {

      // Sending a POST request to the API endpoint
      val res = post(endpoint, data = req)

      // Converting JSON response to string format
      val json_output = read(res.text()).toString()

      // Returning the response through gRPC
      Future.successful(AnalyzeTimestampReply(json_output))

    } catch{

      case e: RequestFailedException => {
        // In case of 404 (no matching logs)
        Future.successful(AnalyzeTimestampReply("404 - no matching logs found within the given time range"))
      }

      // In case of any other exception
      case _: Throwable => Future.successful(AnalyzeTimestampReply("Some other runtime exception occurred"))

    }

  }

  // This function is the RPC implementation for communicating via GET through AWS API Gateway to our Lambda function
  override def requestLogs(request: AnalyzeTimestampRequest): Future[AnalyzeTimestampReply] = {

    // Setting the AWS API Gateway endpoint that connects to our Lambda function
    val endpoint = config.getString("TimestampAnalyzer.API_Gateway_Endpoint") + "/analyze"

    // Creating the JSON object to pass as Lambda function input
    val req = s"""{
                 |  "time_stamp": "${request.timeStamp}",
                 |  "time_interval": "${request.timeInterval}",
                 |  "bucket": "${request.bucket}"
                 |}""".stripMargin

    try {

      // Creating and initializing a HTTP client object
      val httpClient = new HttpClient()
      httpClient.start()

      // Sending a GET request to the API endpoint
      val res = httpClient.newRequest(endpoint).method(HttpMethod.GET).content(new StringContentProvider(req)).send()

      // Converting JSON response to string format
      val json_output = res.getContentAsString

      // Returning the response through gRPC
      Future.successful(AnalyzeTimestampReply(json_output))

    } catch{

      // In case of 404 (no matching logs)
      case e: RequestFailedException => {
        Future.successful(AnalyzeTimestampReply("404 - no matching logs found within the given time range"))
      }

      // In case of any other exception
      case _: Throwable => Future.successful(AnalyzeTimestampReply("Some other runtime exception occurred"))

    }

  }

}