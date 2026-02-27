# axi-simple-utils

A lightweight set of AXI utilities written in **Chisel**.

This repository provides practical building blocks for working with AXI
protocols without aiming for full specification completeness. The focus
is simplicity, clarity, and usability in real designs.

Currently, the repository includes:

-   **AXI4-Lite support**
-   AXI port bundles for easy integration into Chisel modules
-   Simplified bus function models (BFMs)
-   Example AXI-based modules

AXI4-Stream and AXI4-Full support are planned and will be added
incrementally.


Tested on Chisel 7.9.0 and Verilator 5.044

------------------------------------------------------------------------

## Design Philosophy

This project is not a full-featured AXI reference implementation.

Instead, it provides:

-   Minimal and clean AXI interfaces
-   Practical subsets of the protocol
-   Clear structure suitable for integration into real hardware projects
-   Utilities that simplify testbench development

The goal is to support AXI in a lightweight way.

------------------------------------------------------------------------

## Features

### AXI Port Bundles

Predefined AXI bundles that can be directly instantiated in your Chisel
modules:

``` scala
class MyModule(AxiAddrBW: Int = 24) extends Module {
  val io = IO(new Bundle {
    val axi = new AxiLite32IO(AxiAddrBW)
  })
}
```

This allows AXI interfaces to be added cleanly without rewriting channel
wiring logic.

------------------------------------------------------------------------

### Simplified Bus Function Models (BFMs)

Here is an example usage of higher-level interface.

``` scala
  "test AxiList32RevMem" should "pass" in {
    simulate(new Axi4Lite32RevMem) { dut =>
      val bfm = new Axi4Lite32BFM(dut)
      bfm.initMaster()
      val bresp = bfm.write(0x10, 0x123L)  // write 0x123 to the address 0x10
      val (rdata, rresp) = bfm.read(0x10)  // read the adrress 0x10.
	  ...
    }

```

Lower-level channel methods are also available when finer control is
needed:

-   `sendAW(...)`
-   `sendW(...)`
-   `sendSimulAWW(...)`
-   `recvB(...)`
-   `sendAR(...)`
-   `recvR(...)`

This makes testbench code significantly simpler while preserving
flexibility.

------------------------------------------------------------------------

## Roadmap

Planned additions:

-   AXI4-Stream utilities
-   Partial AXI4-Full support
-   Additional adapters and helper components

