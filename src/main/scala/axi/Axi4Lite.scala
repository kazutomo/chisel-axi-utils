// SPDX-License-Identifier: Apache-2.0
// See LICENSE file for details.
package axi

import chisel3._

object AxiLiteResp {
  val OKAY   = 0b00
  val EXOKAY = 0b01 // unused in AXI4-lite
  val SLVERR = 0b10 // slave generated error
  val DECERR = 0b11 // address decode error
}

// A simple AXI4-Lite slave
class AxiLite(AddrBW: Int = 32, DataBW: Int = 32) extends Bundle {
  // write address
  val awaddr  = Input(UInt(AddrBW.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())

  // write data
  val wdata   = Input(UInt(DataBW.W))
  val wstrb   = Input(UInt((DataBW/8).W))
  val wvalid  = Input(Bool())
  val wready  = Output(Bool())

  // write response
  val bresp   = Output(UInt(2.W))
  val bvalid  = Output(Bool())
  val bready  = Input(Bool())

  // read addr
  val araddr  = Input(UInt(AddrBW.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())

  // read data
  val rdata   = Output(UInt(DataBW.W))
  val rresp   = Output(UInt(2.W))
  val rvalid  = Output(Bool())
  val rready  = Input(Bool())
}

class AxiLite32(AddrBW: Int = 32) extends AxiLite(AddrBW, 32)

class AxiLite32IO(addrW: Int) extends Bundle {
  val AXI = new AxiLite32(addrW)
}

trait HasAxiLite32IO { this: Module =>
  val S: AxiLite32IO
}
