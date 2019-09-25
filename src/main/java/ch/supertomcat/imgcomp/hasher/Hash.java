package ch.supertomcat.imgcomp.hasher;

import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Hash class which holds a hash and the corresponding file
 */
public class Hash {
	/**
	 * File Path
	 */
	private final String file;

	/**
	 * Folder Path
	 */
	private final String folder;

	/**
	 * Filename
	 */
	private final String filename;

	/**
	 * Hash
	 */
	private final String hash;

	/**
	 * Constructor
	 * 
	 * @param file
	 * @param hash
	 */
	public Hash(String file, String hash) {
		this.file = file;
		this.hash = hash;
		this.folder = FileUtil.getDirectory(this.file);
		this.filename = FileUtil.getFilename(this.file);
	}

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Returns the folder
	 * 
	 * @return folder
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * Returns the filename
	 * 
	 * @return filename
	 */
	public String getFilename() {
		return filename;
	}
}
