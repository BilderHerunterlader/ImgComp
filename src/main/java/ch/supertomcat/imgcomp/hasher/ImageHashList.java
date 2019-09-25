package ch.supertomcat.imgcomp.hasher;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing image hashes
 */
public class ImageHashList {
	/**
	 * Folder
	 */
	private final String folder;

	/**
	 * Filename Pattern
	 */
	private final String filenamePattern;

	/**
	 * Hashes
	 */
	private List<Hash> hashes = new ArrayList<>();

	/**
	 * Recursive Flag
	 */
	private boolean recursive = false;

	/**
	 * Constructor
	 * 
	 * @param folder Folder
	 * @param filenamePattern Filename Pattern
	 * @param recursive True if recursive, false otherwise
	 */
	public ImageHashList(String folder, String filenamePattern, boolean recursive) {
		this.folder = folder;
		this.filenamePattern = filenamePattern;
		this.recursive = recursive;
	}

	/**
	 * Constructor
	 * 
	 * @param hashes Hashes
	 * @param folder Folder
	 * @param filenamePattern Filename Pattern
	 * @param recursive True if recursive, false otherwise
	 */
	public ImageHashList(List<Hash> hashes, String folder, String filenamePattern, boolean recursive) {
		this.hashes = hashes;
		this.folder = folder;
		this.filenamePattern = filenamePattern;
		this.recursive = recursive;
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
	 * Returns the hashes
	 * 
	 * @return hashes
	 */
	public List<Hash> getHashes() {
		return hashes;
	}

	/**
	 * Returns the filenamePattern
	 * 
	 * @return filenamePattern
	 */
	public String getFilenamePattern() {
		return filenamePattern;
	}

	/**
	 * Returns the recursive
	 * 
	 * @return recursive
	 */
	public boolean isRecursive() {
		return recursive;
	}
}
