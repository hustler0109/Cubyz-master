package cubyz.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

public final class Utils {
	private Utils() {} // No instances allowed.

	public static boolean existsResourceFile(String fileName) {
		boolean result;
		try (InputStream is = Utils.class.getResourceAsStream(fileName)) {
			result = is != null;
		} catch (Exception excp) {
			result = false;
		}
		return result;
	}

	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		Path path = Paths.get(resource);
		if (Files.isReadable(path)) {
			try (SeekableByteChannel fc = Files.newByteChannel(path)) {
				buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
				while (fc.read(buffer) != -1)
					;
			}
		} else {
			try (InputStream source = new FileInputStream(resource);
					ReadableByteChannel rbc = Channels.newChannel(source)) {
				buffer = BufferUtils.createByteBuffer(bufferSize);

				while (true) {
					int bytes = rbc.read(buffer);
					if (bytes == -1) {
						break;
					}
					if (buffer.remaining() == 0) {
						buffer = resizeBuffer(buffer, buffer.capacity() << 1);
					}
				}
			}
		}

		buffer.flip();
		
		return buffer;
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

	public static String loadResource(String path) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path), 4096);
		StringBuilder b = new StringBuilder();
		while (bis.available() != 0) {
			b.append((char) bis.read());
		}
		b.append('\0');
		bis.close();
		return b.toString();
	}

	public static float[] listToArray(List<Float> list) {
		if (list == null)
			return new float[0];
		int size = list.size();
		float[] floatArr = new float[size];
		for (int i = 0; i < size; i++) {
			floatArr[i] = list.get(i);
		}
		return floatArr;
	}

	public static List<String> readAllLines(String fileName) throws Exception {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		return list;
	}

	public static String escapeFolderName(String name) {
		StringBuilder result = new StringBuilder(name.length()*3/2);
		for(char ch : name.toCharArray()) {
			if(Character.isLetterOrDigit(ch)) {
				result.append(ch);
			} else { // Escape all other characters using underscore and the hex code:
				result.append('_');
				result.append(String.format("%04x", (int)ch));
			}
		}
		return result.toString();
	}

	public static String parseEscapedFolderName(String name) {
		StringBuilder result = new StringBuilder(name.length());
		char[] characters = name.toCharArray();
		for(int i = 0; i < characters.length; i++) {
			if(characters[i] == '_') {
				int val = 0;
				for(int j = 0; j < 4; j++) {
					i++;
					if(i < characters.length) {
						val = val*16 + Character.digit(characters[i], 16);
					}
				}
				result.append((char)val);
			} else {
				result.append(characters[i]);
			}
		}
		return result.toString();
	}

	public static void deleteDirectory(Path path) {
		if(!path.toFile().exists()) return;
		try {
			Files.walkFileTree(path, new FileVisitor<>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					Logger.error(e);
					return FileVisitResult.TERMINATE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
