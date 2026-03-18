// SPDX-License-Identifier: Apache-2.0
// See LICENSE file for details.
package axiexamples

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import axi._

class Axi4Lite32RevMemSpec extends AnyFlatSpec with ChiselSim {
  def rev32b(x: Int): Int = {
    var v = x
    v = ((v >>> 1) & 0x55555555) | ((v & 0x55555555) << 1)
    v = ((v >>> 2) & 0x33333333) | ((v & 0x33333333) << 2)
    v = ((v >>> 4) & 0x0f0f0f0f) | ((v & 0x0f0f0f0f) << 4)
    v = ((v >>> 8) & 0x00ff00ff) | ((v & 0x00ff00ff) << 8)
    (v >>> 16) | (v << 16)
  }

  "test AxiList32RevMem" should "pass" in {
    val nwords = 16
    simulate(new Axi4Lite32RevMem(nwords = nwords)) { dut =>
      val bfm = new Axi4Lite32BFM(dut)
      bfm.initMaster()
      bfm.reset()

      val indata = List(1, 15, 0xaaaa5555, 2, 5, 8)
      for ( (elem, idx) <- indata.zipWithIndex) {
        val bresp = bfm.write(idx*4, elem & 0xffffffffL) // mode = AxiWriteMode.SameCycle)
      }

      for ( (elem, idx) <- indata.zipWithIndex) {
        val (rdata, rresp) = bfm.read(idx * 4)
        val ref = rev32b(elem) & 0xffffffffL
        assert(rdata == ref, f"$rdata%08x, $ref%08x, $rresp")
      }

      val (rdata, rresp) = bfm.read(nwords * 4)
      val arfiredcnt = rdata & 0xffff
      val rfiredcnt = (rdata >> 16) & 0xffff
      println(f"arfiredcnt=$arfiredcnt")
      println(f"rfiredcnt=$rfiredcnt")
    }
  }
}
