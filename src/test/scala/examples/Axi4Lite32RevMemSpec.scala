// SPDX-License-Identifier: Apache-2.0
// See LICENSE file for details.
package axiexamples

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import axi._

class Axi4Lite32RevMemSpec extends AnyFlatSpec with ChiselSim {
  "test AxiList32RevMem" should "pass" in {
    simulate(new Axi4Lite32RevMem) { dut =>
      val bfm = new Axi4Lite32BFM(dut)
      bfm.initMaster()
      bfm.reset()
      val bresp = bfm.write(0x10, 0x1L) // mode = AxiWriteMode.SameCycle)
      val (rdata, rresp) = bfm.read(0x10)
      println(f"$rdata%08x, $rresp")
    }
  }
}
