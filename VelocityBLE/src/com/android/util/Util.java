package com.android.util;

import android.util.SparseArray;

public class Util {
	private final static byte SOT_LT = (byte) 0x7E;
	private final static byte SENDER = (byte) 0x00;
	private final static byte CRC = (byte) 0xEE;
	private final static byte EOT_LT = (byte) 0x7E;
	
	private SparseArray array;

	public static byte[] codeCmd(int node, byte[] cmd) {
		byte receiver = (byte) node;
		byte[] data = { SOT_LT, SENDER, receiver, (byte) 0x03, cmd[0], cmd[1], (byte) 0x00, CRC, CRC, EOT_LT };
		return data;
	}

	private void caseUpdate() {
		
	}
}
