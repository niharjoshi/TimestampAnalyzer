package TimeStampAnalyzer

import org.scalatest.flatspec.AnyFlatSpec

import HelperUtils.ObtainConfigReference

class gRPCConfigTest extends AnyFlatSpec {

  val config = ObtainConfigReference("gRPC") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  "gRPC config" should "contain gRPC client address" in {
    val address = config.getString("gRPC.host")
    val expected_address = config.getString("Testing.host")
    assert(address == expected_address)
  }

  it should "contain gRPC client port" in {
    val port = config.getInt("gRPC.port")
    val expected_port = config.getInt("Testing.port")
    assert(port == expected_port)
  }

  it should "contain gRPC override-authority" in {
    val override_authority = config.getString("gRPC.override-authority")
    val expected_override_authority = config.getString("Testing.override-authority")
    assert(override_authority == expected_override_authority)
  }

  it should "contain gRPC certificate" in {
    val cert = config.getString("gRPC.trusted")
    val expected_cert = config.getString("Testing.trusted")
    assert(cert == expected_cert)
  }

}

class TimestampAnalyzerConfigTest extends AnyFlatSpec {

  val config = ObtainConfigReference("TimestampAnalyzer") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  "Timestamp Analyzer config" should "contain AWS API Gateway endpoint" in {
    val endpoint = config.getString("TimestampAnalyzer.API_Gateway_Endpoint")
    val expected_endpoint = config.getString("Testing.API_Gateway_Endpoint")
    assert(endpoint == expected_endpoint)
  }

}
