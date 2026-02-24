package axiexamples

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import axi._

class Axi4Lite32CounterSpec extends AnyFlatSpec with ChiselSim {
  "test AxiList32Counter" should "pass" in {
    simulate(new Axi4Lite32Counter) { dut =>
      val bfm = new Axi4Lite32BFM(dut)
      bfm.initMaster()
      bfm.reset()
      val bresp = bfm.write(0, 0) // reset the counter
      println(f"${bfm.read(0)._1}")
      Thread.sleep(1000)
      println(f"${bfm.read(0)._1}")
    }
  }
}
