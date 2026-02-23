package common

import chisel3._
import _root_.circt.stage.ChiselStage

import java.nio.file.{Files, Paths}
import scala.reflect.ClassTag

object GenVerilog {
  val firtoolOpts_a = Array(
    "--disable-all-randomization",
    "--strip-debug-info",
    "--lowering-options=disallowLocalVariables,disallowPackedArrays",
    "--verilog",
  )

  def apply(gen: => RawModule) : Unit = {
    generate(gen)
  }

  def generate[T <: RawModule : ClassTag](gen: => T, opts : Map[String, String] = Map.empty ) : Unit = {
    val topname = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    val targetdir = "generated/" + topname
    Files.createDirectories(java.nio.file.Paths.get(targetdir))

    val args_a = Array("--target-dir", targetdir)

    val st = System.nanoTime()
    ChiselStage.emitSystemVerilogFile(gen,
      args = args_a,
      firtoolOpts = firtoolOpts_a)
    val et = (System.nanoTime() - st)
    val ets = et.toDouble * 1e-9
    println(f"Verilog generation: ${ets}%.2f sec")
  }
}
