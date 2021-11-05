package TimestampAnalyzer

// Importing necessary packages
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import scala.io.Source
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.ConnectionContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.pki.pem.DERPrivateKeyLoader
import akka.pki.pem.PEMDecoder
import com.typesafe.config.ConfigFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration._
import HelperUtils.{CreateLogger, ObtainConfigReference}


object TimestampAnalyzerServer {

  def main(args: Array[String]): Unit = {

    // Enabling HTTP/2 in Actor System
    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on").withFallback(
      ConfigFactory.defaultApplication()
    )

    // Defining the Actor System
    val system = ActorSystem[Nothing](Behaviors.empty, "TimestampAnalyzerServer", conf)

    // Running the gRPC server
    new TimestampAnalyzerServer(system).run()

  }

}

// This class hold the config for the gRPC server
class TimestampAnalyzerServer(system: ActorSystem[_]) {

  // Initializing logger
  private val logger = CreateLogger(classOf[TimestampAnalyzerServer.type])

  // This is the driver function of the server
  def run(): Future[Http.ServerBinding] = {
    implicit val sys = system
    // Defining the Execution Context
    implicit val ec: ExecutionContext = system.executionContext

    // Routing the execution to the proto-generated handler through the user-defined RPC implementation (overridden methods)
    val service: HttpRequest => Future[HttpResponse] =
      TimestampAnalyzerServiceHandler(new TimestampAnalyzerServiceImpl(system))

    // Binding server to localhost and default port
    val bound: Future[Http.ServerBinding] = Http(system)
      .newServerAt(interface = "127.0.0.1", port = 8080)
      .enableHttps(serverHttpContext)
      .bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    // Ensuring server is bound
    bound.onComplete {
      // On success
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(s"gRPC server bound to ${address.getHostString}, ${address.getPort}")
        println(s"gRPC server bound to ${address.getHostString}, ${address.getPort}")
      // On failure
      case Failure(ex) =>
        logger.info(s"Failed to bind gRPC endpoint, terminating system: $ex")
        // Terminating the server
        system.terminate()
    }

    // Returning the bound server object
    bound

  }

  // This method defines the HTTP context and handles the HTTP/2 authentication
  private def serverHttpContext: HttpsConnectionContext = {

    // Loading the private key pair using .pem file
    val privateKey =
      DERPrivateKeyLoader.load(PEMDecoder.decode(readPrivateKeyPem()))
    val fact = CertificateFactory.getInstance("X.509")
    val cer = fact.generateCertificate(
      classOf[TimestampAnalyzerServer].getResourceAsStream("/certs/server1.pem")
    )
    // Defining the key store
    val ks = KeyStore.getInstance("PKCS12")
    ks.load(null)
    ks.setKeyEntry(
      "private",
      privateKey,
      new Array[Char](0),
      Array[Certificate](cer)
    )
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, null)
    // Getting the SSL context
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
    ConnectionContext.https(context)
  }

  // This function reads the private key
  private def readPrivateKeyPem(): String = {

    // Initializing config reader
    val config = ObtainConfigReference("gRPC") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    // Getting key file path via config
    val key = config.getString("gRPC.server1_key")

    // Loading the private key
    Source.fromResource(key).mkString
  }
}
