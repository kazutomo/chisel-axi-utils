package axiexamples

import axi._

import chisel3._
import chisel3.util._

class Axi4Lite32Counter(AxiAddrBW: Int = 24) extends Module
  with HasAxiLite32IO {

  val io = IO(new AxiLite32IO(AxiAddrBW))
  val counter = RegInit(0.U(32.W))
  counter := counter + 1.U

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
    counter := wHoldDataReg
    bvalid         := true.B
    awHoldValidReg := false.B
    wHoldValidReg  := false.B
  }

  val bFire = bvalid && io.axi.bready
  when(bFire) { bvalid := false.B }

  io.axi.bvalid := bvalid
  io.axi.bresp  := 0.U // 0 means OKAY

  //
  // Read path: AR -> R
  //
  val rvalid = RegInit(false.B)
  val rdata  = Reg(UInt(32.W))

  val arready = !rvalid
  io.axi.arready := arready
  val arFire = io.axi.arvalid && arready

  val rvalidPipe = RegNext(arFire, init=false.B)

  val rFire = rvalid && io.axi.rready
  when(rFire) {
    rvalid := false.B
  }.elsewhen(rvalidPipe) {
    rvalid := true.B
    rdata  := counter
  }

  io.axi.rvalid := rvalid
  io.axi.rdata  := rdata
  io.axi.rresp  := 0.U // OKAY
}

object Axi4Lite32Counter extends App {
  import common.GenVerilog
  GenVerilog.generate(new Axi4Lite32Counter())
}
