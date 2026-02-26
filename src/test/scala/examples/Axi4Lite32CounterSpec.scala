package axiexamples

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import axi._

class Axi4Lite32CounterSpec extends AnyFlatSpec with ChiselSim {
  def splitCounter(v: BigInt): (BigInt, BigInt) = {
    val a =v & 0x00ffffff
    val b = (v >> 24) & 0xf
    (a, b)
  }
  def printCounters(v: BigInt) : Unit = {
    val (a,b) = splitCounter(v)
    println(f"cycles=$a%4d / rdstall=$b%4d")
  }

  "test AxiList32Counter" should "pass" in {
    simulate(new Axi4Lite32Counter) { dut =>
      val bfm = new Axi4Lite32BFM(dut)
      bfm.initMaster()
      // bfm.reset()
      val bresp = bfm.write(0, 0) // reset the counter

      println("Read every cycle")
      for (i <- 0 until 3)   printCounters(bfm.read(0)._1)

      println("Read with stallRReady")
      for (i <- 0 until 3)   printCounters(bfm.read(0, stallRReady = 5)._1)

      println("Read after 1000ms sleep")
      Thread.sleep(1000)
      printCounters(bfm.read(0)._1)
    }
  }
}
