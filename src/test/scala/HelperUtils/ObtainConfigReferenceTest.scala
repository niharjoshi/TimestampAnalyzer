package HelperUtils

// Importing necessary packages
import org.scalatest.flatspec.AnyFlatSpec


class ObtainConfigReferenceTest extends AnyFlatSpec {

  // Testing config reader creation
  "Config utility" should "create a config reader" in {

    val config = ObtainConfigReference.apply("TimestampAnalyzer")

    val expected_config = ObtainConfigReference("TimestampAnalyzer") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    assert(config == Some(expected_config))

  }
}
