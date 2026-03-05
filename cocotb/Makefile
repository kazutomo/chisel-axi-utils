#SIM ?= icarus
SIM ?= verilator
TOPLEVEL_LANG ?= verilog
TOPNAME=user_accel_bd_wrapper
SRCDIR = $(PWD)/../generated/Axi4Lite32RevMem
VERILOG_SOURCES += $(SRCDIR)/user_accel_bd_wrapper.v $(SRCDIR)/Axi4Lite32RevMem.sv $(SRCDIR)/mem_128x32.sv
WAVES = 1
EXTRA_ARGS += --trace --trace-structs

test:
	echo $(VERILOG_SOURCES)
	$(MAKE) sim MODULE=test TOPLEVEL=$(TOPNAME)

include $(shell cocotb-config --makefiles)/Makefile.sim
