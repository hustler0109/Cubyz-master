package cubyz.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cubyz.utils.math.Bits;
import cubyz.world.save.ChunkIO;

public final class Zipper {
	private Zipper() {} // No instances allowed.

	public static void pack(String sourceDirPath, OutputStream outputstream){
		try {
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			Path path = Paths.get(sourceDirPath);
			Files.walk(path)
					.filter(p -> !Files.isDirectory(p)) // potential bug
					.forEach(p -> {
						String relPath = path.relativize(p).toString();
						try {
							byte[] strData = relPath.getBytes(StandardCharsets.UTF_8);
							byte[] len = new byte[4];
							Bits.putInt(len, 0, strData.length);
							arrayOutputStream.write(len);
							arrayOutputStream.write(strData);
							byte[] file = Files.readAllBytes(p);
							Bits.putInt(len, 0, file.length);
							arrayOutputStream.write(len);
							arrayOutputStream.write(file);
						} catch (IOException e) {
							Logger.error(e);
						}
					});
			byte[] uncompressedData = arrayOutputStream.toByteArray();
			byte[] compressedData = ChunkIO.compressChunk(uncompressedData);
			outputstream.write(compressedData);
		}catch(IOException exception){
			Logger.error(exception);
		}
	}
	public static void unpack(String outputFolderPath, InputStream inputStream){
		try {
			File outputFolder = new File(outputFolderPath);
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}
			byte[] fullData = inputStream.readAllBytes();
			byte[] decompressedBytes = ChunkIO.decompressChunk(fullData, 0, fullData.length);
			ByteArrayInputStream in = new ByteArrayInputStream(decompressedBytes);
			while(in.available() != 0) {
				byte[] len = in.readNBytes(4);
				byte[] pathBytes = in.readNBytes(Bits.getInt(len ,0));
				String path = new String(pathBytes, StandardCharsets.UTF_8);
				String filePath = outputFolder.getAbsolutePath() + File.separator + path;
				len = in.readNBytes(4);
				byte[] fileBytes = in.readNBytes(Bits.getInt(len ,0));
				new File(filePath).getParentFile().mkdirs();
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
				bos.write(fileBytes, 0, fileBytes.length);
				bos.close();
			}
			in.close();
		}catch (Exception e){
			Logger.error(e);
		}
	}
}
