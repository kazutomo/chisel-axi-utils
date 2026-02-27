package axiexamples

import axi._

import chisel3._
import chisel3.util._

class Axi4Lite32Counter(AxiAddrBW: Int = 24) extends Module
  with HasAxiLite32IO {

  val io = IO(new AxiLite32IO(AxiAddrBW))

  val counterReg = RegInit(0.U(24.W))
  counterReg := counterReg + 1.U

  val rdstallCounterReg = RegInit(0.U(8.W))

  val awHoldValidReg = RegInit(false.B)
  val awHoldAddrReg  = Reg(UInt(AxiAddrBW.W))
  val wHoldValidReg  = RegInit(false.B)
  val wHoldDataReg   = Reg(UInt(32.W))
  val wHoldStrbReg   = Reg(UInt(4.W))

  val bvalid = RegInit(false.B) // response back ready

  io.axi.awready := !awHoldValidReg && !bvalid
  io.axi.wready  := !wHoldValidReg  && !bvalid

  val awFire = io.axi.awvalid && io.axi.awready
  val wFire  = io.axi.wvalid  && io.axi.wready

  when(awFire) {
    awHoldValidReg := true.B
    awHoldAddrReg  := io.axi.awaddr
  }
  when(wFire) {
    wHoldValidReg := true.B
    wHoldDataReg  := Reverse(io.axi.wdata)
    wHoldStrbReg  := io.axi.wstrb
  }

  val doWrite = awHoldValidReg && wHoldValidReg && !bvalid

  when(doWrite) { // don't care addr
    counterReg := wHoldDataReg
    bvalid         := true.B
    awHoldValidReg := false.B
    wHoldValidReg  := false.B
  }

  val bFire = bvalid && io.axi.bready
  when(bFire) { bvalid := false.B }

  io.axi.bvalid := bvalid
  io.axi.bresp  := AxiLiteResp.OKAY

  //
  // Read path: AR -> R
  //
  val rvalidReg = RegInit(false.B)
  val rdataReg  = Reg(UInt(32.W))

  val arready = !rvalidReg
  io.axi.arready := arready
  val arFire = io.axi.arvalid && arready

  val rvalidPipe = RegNext(arFire, init=false.B)

  when(arFire || rvalidReg) {
    rdstallCounterReg := rdstallCounterReg + 1.U
  }

  val rFire = rvalidReg && io.axi.rready
  when(rFire) {
    rvalidReg := false.B
    rdstallCounterReg := 0.U
  }.elsewhen(rvalidPipe) {
    rvalidReg := true.B
    // rdataReg  := counterReg
  }

  io.axi.rvalid := rvalidReg
  // io.axi.rdata  := rdataReg
  io.axi.rdata  := Cat(rdstallCounterReg, counterReg) // I need to return the laster counter number 
  io.axi.rresp  := AxiLiteResp.OKAY
}

object Axi4Lite32Counter extends App {
  import common.GenVerilog
  GenVerilog.generate(new Axi4Lite32Counter())
}
