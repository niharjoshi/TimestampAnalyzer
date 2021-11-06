package HelperUtils

// Importing necessary packages
import org.scalatest.flatspec.AnyFlatSpec


class CreateLoggerTest extends AnyFlatSpec {

  // Testing logger creation
  "Logger utility" should "create a logger" in {

    val logger = CreateLogger(classOf[CreateLoggerTest])

    assert(logger.getName == "HelperUtils.CreateLoggerTest" || logger.getName == "NOP")

  }

}
