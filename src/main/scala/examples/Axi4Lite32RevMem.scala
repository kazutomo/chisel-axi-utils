package axiexamples

import axi._

import chisel3._
import chisel3.util._

class Axi4Lite32RevMem(nwords: Int = 128, AxiAddrBW: Int = 24) extends Module
  with HasAxiLite32IO {

  val io = IO(new AxiLite32IO(AxiAddrBW))

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

  when(doWrite) {
    val idx = wordIdx(awHoldAddrReg)

    val bytes = Wire(Vec(4, UInt(8.W)))
    bytes(0) := wHoldDataReg(7, 0)
    bytes(1) := wHoldDataReg(15, 8)
    bytes(2) := wHoldDataReg(23, 16)
    bytes(3) := wHoldDataReg(31, 24)

    mem.write(idx, bytes, wHoldStrbReg.asBools) // mask per byte

    bvalid         := true.B
    awHoldValidReg := false.B
    wHoldValidReg  := false.B
  }

  val bFire = bvalid && io.axi.bready
  when(bFire) { bvalid := false.B }

  io.axi.bvalid := bvalid
  io.axi.bresp  := AxiLiteResp.OKAY.U

  //
  // Read path: AR -> R
  //

  val rvalid = RegInit(false.B)
  val rdata  = Reg(UInt(32.W))

  val arready = !rvalid
  io.axi.arready := arready
  val arFire = io.axi.arvalid && arready

  val arIdx = wordIdx(io.axi.araddr)
  val memOutVec = mem.read(arIdx, arFire)
  val memOutU32 = Cat(memOutVec(3), memOutVec(2), memOutVec(1), memOutVec(0))

  val rvalidPipe = RegNext(arFire, init=false.B)

  val rFire = rvalid && io.axi.rready
  when(rFire) {
    rvalid := false.B
  }.elsewhen(rvalidPipe) {
    rvalid := true.B
    rdata  := memOutU32
  }

  io.axi.rvalid := rvalid
  io.axi.rdata  := rdata
  io.axi.rresp  := AxiLiteResp.OKAY.U
}

object Axi4Lite32RevMem extends App {
  import common.GenVerilog
  GenVerilog.generate(new Axi4Lite32RevMem())
}
