// SPDX-License-Identifier: Apache-2.0
// See LICENSE file for details.
package rtlgen

import chisel3._
import _root_.circt.stage.ChiselStage

import java.io.PrintWriter
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Paths}
import scala.reflect.ClassTag

object EmitVerilog {

  def apply(gen: => RawModule) : Unit = {
    generate(gen)
  }

  def generate[T <: RawModule : ClassTag](gen: => T,
                                          firtoolOpts : Array[String] = Array(
                                            "--disable-all-randomization",
                                            "--strip-debug-info",
                                            "--lowering-options=disallowLocalVariables,disallowPackedArrays",
                                            "--verilog",
                                          )              ) : Unit = {
    val topname = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    val targetdir = "generated/" + topname
    Files.createDirectories(java.nio.file.Paths.get(targetdir))

    val args_a = Array("--target-dir", targetdir)

    val st = System.nanoTime()
    ChiselStage.emitSystemVerilogFile(gen,
      args = args_a,
      firtoolOpts = firtoolOpts)
    val et = (System.nanoTime() - st)
    val ets = et.toDouble * 1e-9
    println(f"Verilog generation: ${ets}%.2f sec")
    genAxiWrapper(targetdir, topname, "user_accel_bd_wrapper")
  }

  def writeto(fn: String, text: String, executable: Boolean = false) : Unit = {
    new PrintWriter(fn) {
      write(text)
      close()
    }
    if (executable) {
      val path = Paths.get(fn)
      Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxrw-r--"))
    }
  }

  def genAxiWrapper(targetdir : String, topname : String,
                    axiwrappername : String = "") : Unit = {
    val axitop = if (axiwrappername.isEmpty) {
      topname + "_bd_wrapper"
    } else axiwrappername
    val fn = targetdir + "/" + axitop + ".v"

    val str =
      s"""
        |module $axitop (
        |  input  wire        s_axi_aclk,
        |  input  wire        s_axi_aresetn,
        |
        |  input  wire [31:0] S_AXI_awaddr,
        |  input  wire        S_AXI_awvalid,
        |  output wire        S_AXI_awready,
        |  input  wire [31:0] S_AXI_wdata,
        |  input  wire [ 3:0] S_AXI_wstrb,
        |  input  wire        S_AXI_wvalid,
        |  output wire        S_AXI_wready,
        |  output wire [ 1:0] S_AXI_bresp,
        |  output wire        S_AXI_bvalid,
        |  input  wire        S_AXI_bready,
        |  input  wire [31:0] S_AXI_araddr,
        |  input  wire        S_AXI_arvalid,
        |  output wire        S_AXI_arready,
        |  output wire [31:0] S_AXI_rdata,
        |  output wire [ 1:0] S_AXI_rresp,
        |  output wire        S_AXI_rvalid,
        |  input  wire        S_AXI_rready
        |);
        |
        |  wire s_axi_reset = ~s_axi_aresetn;
        |
        |  $topname u_core (
        |    .clock           (s_axi_aclk),
        |    .reset           (s_axi_reset),
        |    .S_AXI_awaddr    (S_AXI_awaddr),
        |    .S_AXI_awvalid   (S_AXI_awvalid),
        |    .S_AXI_awready   (S_AXI_awready),
        |    .S_AXI_wdata     (S_AXI_wdata),
        |    .S_AXI_wstrb     (S_AXI_wstrb),
        |    .S_AXI_wvalid    (S_AXI_wvalid),
        |    .S_AXI_wready    (S_AXI_wready),
        |    .S_AXI_bresp     (S_AXI_bresp),
        |    .S_AXI_bvalid    (S_AXI_bvalid),
        |    .S_AXI_bready    (S_AXI_bready),
        |    .S_AXI_araddr    (S_AXI_araddr),
        |    .S_AXI_arvalid   (S_AXI_arvalid),
        |    .S_AXI_arready   (S_AXI_arready),
        |    .S_AXI_rdata     (S_AXI_rdata),
        |    .S_AXI_rresp     (S_AXI_rresp),
        |    .S_AXI_rvalid    (S_AXI_rvalid),
        |    .S_AXI_rready    (S_AXI_rready)
        |  );
        |endmodule
        |""".stripMargin

    writeto(fn, str)
  }
}
