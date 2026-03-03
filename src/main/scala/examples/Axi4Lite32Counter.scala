package axiexamples

import axi._

import chisel3._
import chisel3.util._

class Axi4Lite32Counter(AxiAddrBW: Int = 32) extends Module
  with HasAxiLite32IO {

  val S = IO(new AxiLite32IO(AxiAddrBW))

  val counterReg = RegInit(0.U(24.W))
  counterReg := counterReg + 1.U

  val rdstallCounterReg = RegInit(0.U(8.W))

  val awHoldValidReg = RegInit(false.B)
  val awHoldAddrReg  = Reg(UInt(AxiAddrBW.W))
  val wHoldValidReg  = RegInit(false.B)
  val wHoldDataReg   = Reg(UInt(32.W))
  val wHoldStrbReg   = Reg(UInt(4.W))

  val bvalid = RegInit(false.B) // response back ready

  S.AXI.awready := !awHoldValidReg && !bvalid
  S.AXI.wready  := !wHoldValidReg  && !bvalid

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

  val doWrite = awHoldValidReg && wHoldValidReg && !bvalid

  when(doWrite) { // don't care addr
    counterReg := wHoldDataReg
    bvalid         := true.B
    awHoldValidReg := false.B
    wHoldValidReg  := false.B
  }

  val bFire = bvalid && S.AXI.bready
  when(bFire) { bvalid := false.B }

  S.AXI.bvalid := bvalid
  S.AXI.bresp  := AxiLiteResp.OKAY.U

  //
  // Read path: AR -> R
  //
  val rvalidReg = RegInit(false.B)
  val rdataReg  = Reg(UInt(32.W))

  val arready = !rvalidReg
  S.AXI.arready := arready
  val arFire = S.AXI.arvalid && arready

  val rvalidPipe = RegNext(arFire, init=false.B)

  when(arFire || rvalidReg) {
    rdstallCounterReg := rdstallCounterReg + 1.U
  }

  val rFire = rvalidReg && S.AXI.rready
  when(rFire) {
    rvalidReg := false.B
    rdstallCounterReg := 0.U
  }.elsewhen(rvalidPipe) {
    rvalidReg := true.B
    // rdataReg  := counterReg
  }

  S.AXI.rvalid := rvalidReg
  // io.AXI.rdata  := rdataReg
  S.AXI.rdata  := Cat(rdstallCounterReg, counterReg) // I need to return the laster counter number
  S.AXI.rresp  := AxiLiteResp.OKAY.U
}

object Axi4Lite32Counter extends App {
  import common.GenVerilog
  GenVerilog.generate(new Axi4Lite32Counter())
}
