
import itertools
import logging
import os
import random

import cocotb_test.simulator
import pytest

import cocotb
from cocotb.clock import Clock
from cocotb.triggers import RisingEdge, Timer
from cocotb.regression import TestFactory

from cocotbext.axi import AxiLiteBus, AxiLiteMaster, AxiLiteRam

import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)


async def reset_dut(dut, cycles=5):
    dut.s_axi_aresetn.value = 0  # assert (active-low)
    for _ in range(cycles):
        await RisingEdge(dut.s_axi_aclk)
    dut.s_axi_aresetn.value = 1  # deassert
    for _ in range(2):
        await RisingEdge(dut.s_axi_aclk)

@cocotb.test()
async def test_axil_mem32_basic(dut):
    cocotb.start_soon(Clock(dut.s_axi_aclk, 10, units="ns").start())

    bus = AxiLiteBus.from_prefix(dut, "S_AXI")
    axi_master = AxiLiteMaster(
        bus,
        dut.s_axi_aclk,        # lower-case clock
        dut.s_axi_aresetn,     # lower-case reset
        reset_active_level=0   # because aresetn is active-low
    )
    await reset_dut(dut)
    
    data = bytearray([x + 10 for x in range(4)])
    
    await axi_master.write(0x10, data)

    tmp = await axi_master.read(0x10, 4)
    print(tmp)
    
    print("All done!!!!")
