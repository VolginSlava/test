package com.example.testapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileLoader {
	private static final int BUFFER_SIZE = 1024;

	public byte[] download(URL url) throws IOException {
		ByteArrayOutputStream file = new ByteArrayOutputStream();
		
		InputStream in = null;
		OutputStream out = file;
		try {
			in = url.openConnection().getInputStream();
			download(in, out);
		} finally {
			out.close();
			if (in != null) {
				in.close();
			}
		}
		return file.toByteArray();
	}

	private void download(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int readBytes;
		while ((readBytes = in.read(b)) > 0) {
			out.write(b, 0, readBytes);
		}
	}
}
