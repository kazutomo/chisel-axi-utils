// SPDX-License-Identifier: Apache-2.0
// See LICENSE file for details.
package axiexamples

import axi._

import chisel3._
import chisel3.util._

class Axi4Lite32RevMem(nwords: Int = 128, AxiAddrBW: Int = 32) extends Module
  with HasAxiLite32IO {

  val S = IO(new AxiLite32IO(AxiAddrBW))

  val mem = SyncReadMem(nwords, Vec(4, UInt(8.W))) // each word is 4 bytes

  def wordIdx(byteAddr: UInt) : UInt = {
    val idxW = log2Ceil(nwords)
    (byteAddr >> 2)(idxW - 1, 0)
  }

  //
  // Write path: AW -> W -> B
  //
  val awHoldValidReg = RegInit(false.B)
  val awHoldAddrReg  = Reg(UInt(AxiAddrBW.W))
  val wHoldValidReg  = RegInit(false.B)
  val wHoldDataReg   = Reg(UInt(32.W))
  val wHoldStrbReg   = Reg(UInt(4.W))

  val bvalidReg = RegInit(false.B) // response back ready

  S.AXI.awready := !awHoldValidReg && !bvalidReg
  S.AXI.wready  := !wHoldValidReg  && !bvalidReg

  val awFire = S.AXI.awvalid && S.AXI.awready
  val wFire  = S.AXI.wvalid  && S.AXI.wready

  when(awFire) {
    awHoldValidReg := true.B
    awHoldAddrReg  := S.AXI.awaddr
  }
  when(wFire) {
    wHoldValidReg := true.B
    wHoldDataReg  := Reverse(S.AXI.wdata)
    wHoldStrbReg  := S.AXI.wstrb
  }

  val doWrite = awHoldValidReg && wHoldValidReg && !bvalidReg

  when(doWrite) {
    val idx = wordIdx(awHoldAddrReg)

    val bytes = Wire(Vec(4, UInt(8.W)))
    bytes(0) := wHoldDataReg(7, 0)
    bytes(1) := wHoldDataReg(15, 8)
    bytes(2) := wHoldDataReg(23, 16)
    bytes(3) := wHoldDataReg(31, 24)

    mem.write(idx, bytes, wHoldStrbReg.asBools) // mask per byte

    bvalidReg      := true.B
    awHoldValidReg := false.B
    wHoldValidReg  := false.B
  }

  val bFire = bvalidReg && S.AXI.bready
  when(bFire) { bvalidReg := false.B }

  S.AXI.bvalid := bvalidReg
  S.AXI.bresp  := AxiLiteResp.OKAY.U

  //
  // Read path: AR -> R
  //

  val rvalidReg = RegInit(false.B)
  val rdataReg  = Reg(UInt(32.W))
  val araddrHoldReg = RegInit(0.U(AxiAddrBW.W))
  val arFiredReg = RegInit(0.U(16.W))

  val arready = !rvalidReg
  S.AXI.arready := arready
  val arFire = S.AXI.arvalid && arready

  when(arFire) {
    araddrHoldReg := S.AXI.araddr
    arFiredReg := arFiredReg + 1.U
  }

  val arIdx = wordIdx(S.AXI.araddr)
  val memOutVec = mem.read(arIdx, arFire)
  val memOutU32 = Cat(memOutVec(3), memOutVec(2), memOutVec(1), memOutVec(0))

  val rvalidPipe = RegNext(arFire, init=false.B)

  val rFire = rvalidReg && S.AXI.rready
  val rFiredReg = RegInit(0.U(16.W))
  when(rFire) {
    rvalidReg := false.B
    rFiredReg := rFiredReg + 1.U
  }.elsewhen(rvalidPipe) {
    rvalidReg := true.B
    when(araddrHoldReg === (nwords*4).U) {
      rdataReg := (rFiredReg << 16) | arFiredReg
    }.otherwise {
      rdataReg := memOutU32
    }
  }

  S.AXI.rvalid := rvalidReg
  S.AXI.rdata  := rdataReg
  S.AXI.rresp  := AxiLiteResp.OKAY.U
}

object Axi4Lite32RevMem extends App {
  import rtlgen.EmitVerilog
  EmitVerilog.generate(new Axi4Lite32RevMem())
}
