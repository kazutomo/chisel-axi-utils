package axi

import chisel3._

// A simple 32-bit AXI4-Lite slave
class AxiLite32(AxiAddrBW: Int = 24) extends Bundle {
  // write address
  val awaddr  = Input(UInt(AxiAddrBW.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())

  // write data
  val wdata   = Input(UInt(32.W))
  val wstrb   = Input(UInt(4.W))
  val wvalid  = Input(Bool())
  val wready  = Output(Bool())

  // write response
  val bresp   = Output(UInt(2.W))
  val bvalid  = Output(Bool())
  val bready  = Input(Bool())

  // read addr
  val araddr  = Input(UInt(AxiAddrBW.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())

  // read data
  val rdata   = Output(UInt(32.W))
  val rresp   = Output(UInt(2.W))
  val rvalid  = Output(Bool())
  val rready  = Input(Bool())
}

class AxiLite32IO(addrW: Int) extends Bundle {
  val axi = new AxiLite32(addrW)
}

trait HasAxiLite32IO { this: Module =>
  val io: AxiLite32IO
}


