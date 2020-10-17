package org.graalvm.vm.trcview.arch.arm.device;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.analysis.device.EnumFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FXFieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.arm.device.event.ge.G3SwapBuffers;
import org.graalvm.vm.trcview.arch.arm.device.event.ge.G3Viewport;
import org.graalvm.vm.trcview.arch.arm.io.ARMDeviceRegisterEvent;
import org.graalvm.vm.trcview.arch.io.DeviceDefinitionEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;

public class ARMDevices {
	public static final int CPU = 0;
	public static final int MEMCTL = 1;
	public static final int VIDEO = 2;
	public static final int VIDEO_CAPTURE = 3;
	public static final int RE = 4;
	public static final int GE = 5;

	// MEMCTL
	public static final int EXMEMCNT = 0;
	public static final int WRAMCNT = 1;
	public static final int WRAMSTAT = 2;
	public static final int VRAMCNT_A = 4;
	public static final int VRAMCNT_B = 5;
	public static final int VRAMCNT_C = 6;
	public static final int VRAMCNT_D = 7;
	public static final int VRAMCNT_E = 8;
	public static final int VRAMCNT_F = 9;
	public static final int VRAMCNT_G = 10;
	public static final int VRAMCNT_H = 11;
	public static final int VRAMCNT_I = 12;

	// VIDEO
	public static final int MASTER_BRIGHT = 0;
	public static final int DISPCNT = 1;

	// VIDEO_CAPTURE
	public static final int DISPCAPCNT = 0;
	public static final int DISP_MMEM_FIFO = 1;

	// RE
	public static final int DISP3DCNT = 0;
	public static final int RDLINES_COUNT = 1;
	public static final int EDGE_COLOR0 = 2;
	public static final int EDGE_COLOR1 = 3;
	public static final int EDGE_COLOR2 = 4;
	public static final int EDGE_COLOR3 = 5;
	public static final int EDGE_COLOR4 = 6;
	public static final int EDGE_COLOR5 = 7;
	public static final int EDGE_COLOR6 = 8;
	public static final int EDGE_COLOR7 = 9;
	public static final int ALPHA_TEST_REF = 10;
	public static final int CLEAR_COLOR = 11;
	public static final int CLEAR_DEPTH = 12;
	public static final int CLRIMAGE_OFFSET = 13;
	public static final int FOG_COLOR = 14;
	public static final int FOG_OFFSET = 15;
	public static final int FOG_TABLE = 16;
	public static final int TOON_TABLE = FOG_TABLE + 32;

	// GE
	public static final int GXSTAT = 0;
	public static final int RAM_COUNT = 1;
	public static final int DISP_1DOT_DEPTH = 2;
	public static final int NOP = 3;
	public static final int MTX_MODE = 4;
	public static final int MTX_PUSH = 5;
	public static final int MTX_POP = 6;
	public static final int MTX_STORE = 7;
	public static final int MTX_RESTORE = 8;
	public static final int MTX_IDENTITY = 9;
	public static final int MTX_LOAD_4x4 = 10;
	public static final int MTX_LOAD_4x3 = 11;
	public static final int MTX_MULT_4x4 = 12;
	public static final int MTX_MULT_4x3 = 13;
	public static final int MTX_MULT_3x3 = 14;
	public static final int MTX_SCALE = 15;
	public static final int MTX_TRANS = 16;
	public static final int COLOR = 17;
	public static final int NORMAL = 18;
	public static final int TEXCOORD = 19;
	public static final int VTX_16 = 20;
	public static final int VTX_10 = 21;
	public static final int VTX_XY = 22;
	public static final int VTX_XZ = 23;
	public static final int VTX_YZ = 24;
	public static final int VTX_DIFF = 25;
	public static final int POLYGON_ATTR = 26;
	public static final int TEXIMAGE_PARAM = 27;
	public static final int PLTT_BASE = 28;
	public static final int DIF_AMB = 29;
	public static final int SPE_EMI = 30;
	public static final int LIGHT_VECTOR = 31;
	public static final int SHININESS = 32;
	public static final int BEGIN_VTXS = 33;
	public static final int END_VTXS = 34;
	public static final int SWAP_BUFFERS = 35;
	public static final int VIEWPORT = 36;
	public static final int BOX_TEST = 37;
	public static final int POS_TEST = 38;
	public static final int VEC_TEST = 39;
	public static final int GXFIFO = 40;

	public static DeviceDefinitionEvent createDevices() {
		DeviceDefinitionEvent evt = new DeviceDefinitionEvent();
		Device cpu = new Device(CPU, "ARM946E-S", DeviceType.PROCESSOR);

		Device memctl = new Device(MEMCTL, "Memory Control", DeviceType.MEMORY);
		memctl.add(reg(EXMEMCNT, "EXMEMCNT", 0x4000204,
				enumf("GBA Slot SRAM Access Time", 1, 0, "10 cycles", "8 cycles", "6 cycles",
						"18 cycles"),
				enumf("GBA Slot ROM 1st Access Time", 3, 2, "10 cycles", "8 cycles", "6 cycles",
						"18 cycles"),
				enumf("GBA Slot ROM 2nd Access Time", 4, "6 cycles", "4 cycles"),
				enumf("GBA Slot PHI-pin out", 6, 5, "Low", "4.19MHz", "8.38MHz", "16.76MHz"),
				enumf("GBA Slot Access Rights", 7, "ARM9", "ARM7"),
				enumf("NDS Slot Access Rights", 11, "ARM9", "ARM7"),
				enumf("Main Memory Interface Mode Switch", 14, "Async/GBA/Reserved", "Synchronous"),
				enumf("Main Memory Access Priority", 15, "ARM9 Priority", "ARM7 Priority")));
		memctl.add(reg(WRAMCNT, "WRAMCNT", 0x4000247,
				enumf("ARM9/ARM7", 1, 0, "32K/0K", "2nd 16K/1st 16K", "1st 16K/2nd 16K", "0/32K")));
		memctl.add(reg(VRAMCNT_A, "VRAMCNT_A", 0x4000240, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_B, "VRAMCNT_B", 0x4000241, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_C, "VRAMCNT_C", 0x4000242, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_D, "VRAMCNT_D", 0x4000243, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_E, "VRAMCNT_E", 0x4000244, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_F, "VRAMCNT_F", 0x4000245, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_G, "VRAMCNT_G", 0x4000246, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_H, "VRAMCNT_H", 0x4000248, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));
		memctl.add(reg(VRAMCNT_I, "VRAMCNT_I", 0x4000249, intf("VRAM MST", 2, 0), intf("VRAM Offset", 4, 3),
				intf("VRAM Enable", 7)));

		Device video = new Device(VIDEO, "Video", DeviceType.DISPLAY);
		video.add(reg(MASTER_BRIGHT, "MASTER_BRIGHT", 0x400006C, intf("Factor", 4, 0),
				enumf("Mode", 15, 14, "Disable", "Up", "Down", "Reserved")));
		video.add(reg(DISPCNT, "DISPCNT", 0x4000000, intf("BG Mode", 2, 0),
				enumf("BG0 2D/3D Selection", 3, "2D", "3D"),
				enumf("Tile OBJ Mapping", 4, "2D [max 32KB]", "1D [max 32KB..256KB]"),
				enumf("Bitmap OBJ 2D-Dimension", 5, "128x512 dots", "256x256 dots"),
				enumf("Bitmap OBJ Mapping", 6, "2D [max 128KB]", "1D [max 128KB..256KB]"),
				intf("Forced Blank", 7),
				intf("Screen Display BG0", 8),
				intf("Screen Display BG1", 9),
				intf("Screen Display BG2", 10),
				intf("Screen Display BG3", 11),
				intf("Screen Display OBJ", 12),
				intf("Window 0 Display Flag", 13),
				intf("Window 10Display Flag", 14),
				intf("OBJ Window Display Flag", 15),
				enumf("Display Mode", 17, 16, "Display off", "Graphics Display",
						"Engine A only: VRAM Display", "Engine A only: Main Memory Display"),
				enumf("VRAM block", 19, 18, "VRAM A", "VRAM B", "VRAM C", "VRAM D"),
				intf("Tile OBJ 1D-Boundary", 21, 20),
				intf("Bitmap OBJ 1D-Boundary", 22),
				intf("OBJ Processing during H-Blank", 23),
				intf("Character Base [64K steps]", 26, 24),
				intf("Screen Base [64K steps]", 29, 27),
				intf("BG Extended Palettes", 30),
				intf("OBJ Extended Palettes", 31)));

		Device videoCapture = new Device(VIDEO_CAPTURE, "Video Capture", DeviceType.DISPLAY);
		videoCapture.add(reg(DISPCAPCNT, "DISPCAPCNT", 0x4000064,
				intf("EVA", 4, 0),
				intf("EVB", 12, 8),
				enumf("VRAM Write Block", 17, 16, "VRAM A", "VRAM B", "VRAM C", "VRAM D"),
				enumf("VRAM Write Offset", 19, 18, "0x00000", "0x08000", "0x10000", "0x18000"),
				enumf("Capture Size", 21, 20, "128x128 dots", "256x64 dots", "256x128 dots",
						"256x192 dots"),
				enumf("Source A", 24, "Graphics Screen BG+3D+OBJ", "3D Screen"),
				enumf("Source B", 25, "VRAM", "Main Memory Display FIFO"),
				enumf("VRAM Read Offset", 27, 26, "0x00000", "0x08000", "0x10000", "0x18000"),
				enumf("Capture Source", 30, 29, "Source A", "Source B", "Source A+B blended",
						"Source A+B blended"),
				enumf("Capture Enable", 31, "Disable/Ready", "Enable/Busy")));
		videoCapture.add(reg(DISP_MMEM_FIFO, "DISP_MMEM_FIFO", 0x4000068));

		Device re = new Device(RE, "Rendering Engine", DeviceType.GRAPHICS);
		re.add(reg(DISP3DCNT, "DISP3DCNT", 0x4000060, enumf("Texture Mapping", 0, "Disable", "Enable"),
				enumf("PolygonAttr Shading", 1, "Toon Shading", "Highlight Shading"),
				enumf("Alpha-Test", 2, "Disable", "Enable"),
				enumf("Alpha-Blending", 3, "Disable", "Enable"),
				enumf("Anti-Aliasing", 4, "Disable", "Enable"),
				enumf("Edge-Marking", 5, "Disable", "Enable"),
				enumf("Fog Color/Alpha Mode", 6, "Alpha and Color", "Only Alpha"),
				enumf("Fog Master Enable", 7, "Disable", "Enable"),
				intf("Fog Depth Shift", 11, 8),
				enumf("Color Buffer RDLINES Underflow", 12, "None", "Underflow/Acknowledge"),
				enumf("Polygon/Vertex RAM Overflow", 13, "None", "Overflow/Acknowledge"),
				enumf("Rear-Plane Mode", 14, "Blank", "Bitmap")));
		re.add(reg(DISP_1DOT_DEPTH, "DISP_1DOT_DEPTH", 0x4000610, new FXFieldFormat("W-Coordinate", 14, 0, 3)));
		re.add(reg(ALPHA_TEST_REF, "ALPHA_TEST_REF", 0x4000340, intf("Alpha-Test Comparison Value", 4, 0)));

		Device ge = new Device(GE, "Geometry Engine", DeviceType.GRAPHICS);
		ge.add(reg(GXFIFO, "GXFIFO", 0x4000400));
		ge.add(reg(SWAP_BUFFERS, "SWAP_BUFFERS", 0x4000540,
				enumf("Translucent polygon Y-sorting", 0, "Auto-sort", "Manual-sort"),
				enumf("Depth Buffering", 1, "With Z-value", "With W-value")));
		ge.add(reg(VIEWPORT, "VIEWPORT", 0x4000580, intf("X1", 7, 0), intf("Y1", 15, 8), intf("X2", 23, 16),
				intf("Y2", 31, 24)));

		evt.addDevice(cpu);
		evt.addDevice(memctl);
		evt.addDevice(video);
		evt.addDevice(videoCapture);
		evt.addDevice(re);
		evt.addDevice(ge);
		return evt;
	}

	public static Event getEvent(MemoryEvent mem) {
		int addr = (int) mem.getAddress();
		int value = (int) mem.getValue();
		boolean write = mem.isWrite();
		switch(addr) {
		case 0x4000204:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, EXMEMCNT, value, write);
		case 0x4000247:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, WRAMCNT, value, write);
		case 0x4000240:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_A, value, write);
		case 0x4000241:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_B, value, write);
		case 0x4000242:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_C, value, write);
		case 0x4000243:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_D, value, write);
		case 0x4000244:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_E, value, write);
		case 0x4000245:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_F, value, write);
		case 0x4000246:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_G, value, write);
		case 0x4000248:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_H, value, write);
		case 0x4000249:
			return new ARMDeviceRegisterEvent(mem.getTid(), MEMCTL, VRAMCNT_I, value, write);
		case 0x400006C:
			return new ARMDeviceRegisterEvent(mem.getTid(), VIDEO, MASTER_BRIGHT, value, write);
		case 0x4000000:
			return new ARMDeviceRegisterEvent(mem.getTid(), VIDEO, DISPCNT, value, write);
		case 0x4000060:
			return new ARMDeviceRegisterEvent(mem.getTid(), RE, DISP3DCNT, value, write);
		case 0x4000064:
			return new ARMDeviceRegisterEvent(mem.getTid(), VIDEO_CAPTURE, DISPCAPCNT, value, write);
		case 0x4000068:
			return new ARMDeviceRegisterEvent(mem.getTid(), VIDEO_CAPTURE, DISP_MMEM_FIFO, value, write);
		case 0x4000400:
			return new ARMDeviceRegisterEvent(mem.getTid(), GE, GXFIFO, value, write);
		}

		// write to IO reg
		if(write) {
			switch(addr) {
			case 0x4000540:
				return new G3SwapBuffers(mem.getTid(), value);
			case 0x4000580:
				return new G3Viewport(mem.getTid(), value);
			}
		}

		return null;
	}

	private static FieldFormat intf(String name, int bit) {
		return new IntegerFieldFormat(name, bit);
	}

	private static FieldFormat intf(String name, int hi, int lo) {
		return new IntegerFieldFormat(name, hi, lo, FieldNumberType.HEX);
	}

	private static FieldFormat enumf(String name, int hi, int lo, String... values) {
		return new EnumFieldFormat(name, hi, lo, values);
	}

	private static FieldFormat enumf(String name, int bit, String... values) {
		return new EnumFieldFormat(name, bit, values);
	}

	private static DeviceRegister reg(int id, String name, long addr, FieldFormat... fmt) {
		DeviceRegister reg = new DeviceRegister(id, name, addr, fmt);
		return reg;
	}
}
