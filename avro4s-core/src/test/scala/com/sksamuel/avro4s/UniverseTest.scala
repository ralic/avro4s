package com.sksamuel.avro4s

import java.io.ByteArrayOutputStream

import org.scalatest.{Matchers, WordSpec}

/**
  * An end to end test for a hugely nested structure.
  *
  * Currently tests:
  *
  * - Strings,
  * - Ints
  * - doubles
  * - booleans
  * - byte arrays
  * - longs
  * - seqs of case classes
  * - arrays of case classes
  * - Maps of Strings to Ints
  * - Maps of Strings to Case Classes
  * - Options of Strings
  * - Options of Case classes
  * - Either[A,B] where A and B are both case classes
  * - Either[A,B] where A and B are both primitives
  */
class UniverseTest extends WordSpec with Matchers {

  val clipper = Ship(name = "Imperial Clipper", role = "fighter escort", maxSpeed = 430, jumpRange = 8.67, hardpoints = Map("medium" -> 4, "large" -> 2), defaultWeapon = Some("pulse laser"))
  val eagle = Ship(name = "Eagle", role = "fighter", maxSpeed = 350, jumpRange = 15.4, hardpoints = Map("small" -> 3), defaultWeapon = None)

  val g = Universe(
    factions = Seq(
      Faction("Imperial", true, homeworld = Option(Planet("Earth")), shipRanks = Map("baron" -> clipper)),
      Faction("Federation", true, homeworld = Option(Planet("Earth"))),
      Faction("Independant", false, homeworld = None)
    ),
    manufacturers = Array(
      Manufacturer(
        name = "Gutamaya",
        ships = Seq(clipper)
      ),
      Manufacturer(
        name = "Core Dynamics",
        ships = Seq(eagle)
      )
    ),
    cqc = CQC(
      maps = Seq(
        PlayableMap(name = "level1", bonus = Left("weapon"), stationOrPlanet = Left(Station("orbis"))),
        PlayableMap(name = "level2", bonus = Right(123l), stationOrPlanet = Right(Planet("earth")))
      )
    )
  )

  "Avro4s" should {
    "support complex schema" in {
      val schema = AvroSchema2[Universe]
      println(schema.toString(true))
      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/universe.avsc"))
      schema.toString(true) shouldBe expected.toString(true)
    }
    "support complex write" in {
      val output = new ByteArrayOutputStream
      val avro = AvroOutputStream[Universe](output)
      avro.write(g)
      avro.close()
    }
  }
}

case class Universe(factions: Seq[Faction], manufacturers: Array[Manufacturer], cqc: CQC)

case class Faction(name: String, playable: Boolean, homeworld: Option[Planet], shipRanks: Map[String, Ship] = Map.empty)

case class Planet(name: String)

case class Station(name: String)

case class Manufacturer(name: String, ships: Seq[Ship])

case class Ship(name: String, role: String, maxSpeed: Int, jumpRange: Double, hardpoints: Map[String, Int], defaultWeapon: Option[String])

case class CQC(maps: Seq[PlayableMap])

case class PlayableMap(name: String, bonus: Either[String, Long], stationOrPlanet: Either[Station, Planet])