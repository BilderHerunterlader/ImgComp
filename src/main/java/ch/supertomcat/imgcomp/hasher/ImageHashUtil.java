package ch.supertomcat.imgcomp.hasher;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class for Image Hashing
 */
public final class ImageHashUtil {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(ImageHashUtil.class);

	/**
	 * Pattern for splitting lines
	 */
	private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\t");

	private static MessageDigest hashAlgorithm;

	static {
		// Hash-Algorithmus Instanz
		try {
			hashAlgorithm = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	/**
	 * Constructor
	 */
	private ImageHashUtil() {
	}

	/**
	 * Calculates the hash of the uncompressed image data
	 * 
	 * @param f
	 * @return Hash
	 */
	public static synchronized String getImageHash(Path f) {
		try {
			BufferedImage img = ImageIO.read(f.toFile());
			if (img == null) {
				logger.error("{} is not an Image", f.toAbsolutePath());
				return "";
			} else {
				// Calculate hash of raw image data, without any file headers
				byte[] data = null;

				DataBuffer dataBuffer = img.getData().getDataBuffer();
				if (dataBuffer instanceof DataBufferByte dataBufferByte) {
					data = dataBufferByte.getData();
				} else if (dataBuffer instanceof DataBufferDouble dataBufferDouble) {
					double[] doubleData = dataBufferDouble.getData();
					Function<ByteBuffer, Void> putValuesFunction = buffer -> {
						for (double value : doubleData) {
							buffer.putDouble(value);
						}
						return null;
					};
					data = converToByteArray(doubleData.length, 8, putValuesFunction);
				} else if (dataBuffer instanceof DataBufferFloat dataBufferFloat) {
					float[] floatData = dataBufferFloat.getData();
					Function<ByteBuffer, Void> putValuesFunction = buffer -> {
						for (float value : floatData) {
							buffer.putFloat(value);
						}
						return null;
					};
					data = converToByteArray(floatData.length, 4, putValuesFunction);
				} else if (dataBuffer instanceof DataBufferInt dataBufferInt) {
					int[] intData = dataBufferInt.getData();
					Function<ByteBuffer, Void> putValuesFunction = buffer -> {
						for (int value : intData) {
							buffer.putInt(value);
						}
						return null;
					};
					data = converToByteArray(intData.length, 4, putValuesFunction);
				} else if (dataBuffer instanceof DataBufferShort dataBufferShort) {
					short[] shortData = dataBufferShort.getData();
					Function<ByteBuffer, Void> putValuesFunction = buffer -> {
						for (short value : shortData) {
							buffer.putShort(value);
						}
						return null;
					};
					data = converToByteArray(shortData.length, 2, putValuesFunction);
				} else if (dataBuffer instanceof DataBufferUShort dataBufferUShort) {
					short[] shortData = dataBufferUShort.getData();
					Function<ByteBuffer, Void> putValuesFunction = buffer -> {
						for (short value : shortData) {
							buffer.putShort(value);
						}
						return null;
					};
					data = converToByteArray(shortData.length, 2, putValuesFunction);
				}

				if (data == null) {
					return "";
				}

				hashAlgorithm.update(data, 0, data.length);
				byte[] digest = hashAlgorithm.digest();

				StringBuilder sb = new StringBuilder();
				for (byte b : digest) {
					sb.append(toHexString(b));
				}
				return sb.toString();
			}
		} catch (Exception e) {
			logger.error("Could not generate hash for file: {}", f, e);
			return "";
		}
	}

	/**
	 * Convert int, float and so no arrays to byte array
	 * 
	 * @param <T> Type
	 * @param arrayLength Length of the array
	 * @param valueByteSize Size of bytes of a single value
	 * @param putValuesFunction Function to put values into ByteBuffer
	 * @return Byte Array
	 */
	private static <T> byte[] converToByteArray(int arrayLength, int valueByteSize, Function<ByteBuffer, Void> putValuesFunction) {
		ByteBuffer buffer = ByteBuffer.allocate(valueByteSize * arrayLength);
		buffer.order(ByteOrder.BIG_ENDIAN);
		putValuesFunction.apply(buffer);
		return buffer.array();
	}

	private static String toHexString(byte b) {
		int value = (b & 0x7F) + (b < 0 ? 128 : 0);
		String retval = (value < 16 ? "0" : "");
		retval += Integer.toHexString(value).toUpperCase();
		return retval;
	}

	/**
	 * Read Hash List from File
	 * TODO Maybe throw exception
	 * 
	 * @param inputFile Input File
	 * @return Hashlist
	 */
	public static ImageHashList readHashList(String inputFile) {
		String folder = null;
		String filenamePattern = null;
		boolean recursive = false;
		List<Hash> hashes = new ArrayList<>();

		try (FileInputStream in = new FileInputStream(inputFile); BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line = null;
			int lineNumber = 1;
			while ((line = br.readLine()) != null) {
				if (lineNumber == 1) {
					folder = line;
					lineNumber++;
					continue;
				} else if (lineNumber == 2) {
					filenamePattern = line;
					lineNumber++;
					continue;
				} else if (lineNumber == 3) {
					recursive = Boolean.parseBoolean(line);
					lineNumber++;
					continue;
				}

				String parts[] = LINE_SPLIT_PATTERN.split(line);
				if (parts.length != 2) {
					logger.error("Incorrect Line in '{}': {} -> {}", inputFile, lineNumber, line);
					continue;
				}
				hashes.add(new Hash(parts[1], parts[0]));
				lineNumber++;
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		if (folder == null || filenamePattern == null) {
			throw new IllegalArgumentException("Missing folder or filenamePattern in '" + inputFile + "'");
		}
		return new ImageHashList(hashes, folder, filenamePattern, recursive);
	}

	/**
	 * Write Hash List to File
	 * TODO Maybe throw exception
	 * 
	 * @param imageHashList Image Hash List
	 * @param outputFile Output File
	 */
	public static void writeHashList(ImageHashList imageHashList, String outputFile) {
		try (FileOutputStream out = new FileOutputStream(outputFile); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
			bw.write(imageHashList.getFolder() + "\n");
			bw.write(imageHashList.getFilenamePattern() + "\n");
			bw.write(imageHashList.isRecursive() + "\n");
			bw.flush();
			for (Hash hash : imageHashList.getHashes()) {
				bw.write(hash.getHash() + "\t" + hash.getFile() + "\n");
				bw.flush();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
