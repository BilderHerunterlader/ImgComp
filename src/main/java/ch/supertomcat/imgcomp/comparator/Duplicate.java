package ch.supertomcat.imgcomp.comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Duplicate class which holds the hash and the files
 */
public class Duplicate {
	/**
	 * hash
	 */
	private String hash = "";

	/**
	 * files
	 */
	private List<String> files = new ArrayList<>();

	private List<String> folders = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param hash Hash
	 * @param file File
	 */
	public Duplicate(String hash, String file) {
		this.hash = hash;
		this.files.add(file);

		String folder = FileUtil.getDirectory(file);
		folders.add(folder);
	}

	/**
	 * Adds a file
	 * 
	 * @param file File
	 */
	public void addFile(String file) {
		if (this.files.contains(file) == false) {
			this.files.add(file);

			String folder = FileUtil.getDirectory(file);
			if (folders.contains(folder) == false) {
				folders.add(folder);
				Collections.sort(folders);
			}
		}
	}

	/**
	 * Returns the hash
	 * 
	 * @return hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Returns the files
	 * 
	 * @return files
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * @return isSameRelativeFolder
	 */
	public boolean isSameRelativeFolder() {
		if (files.size() <= 0) {
			return false;
		}

		String baseFolder1 = "F:\\BH\\pix.d-panabaker.org_\\Home\\";
		String baseFolder2 = "F:\\8DaniellePanabaker\\_pix.d-panabaker.org_\\Galerie\\";

		String relativeFolder1 = files.get(0);
		relativeFolder1 = FileUtil.getDirectory(relativeFolder1);
		relativeFolder1 = relativeFolder1.replace(baseFolder1, "");
		relativeFolder1 = relativeFolder1.replace(baseFolder2, "");

		for (int i = 1; i < files.size(); i++) {
			String relativeFolder2 = files.get(i);
			relativeFolder2 = FileUtil.getDirectory(relativeFolder2);
			relativeFolder2 = relativeFolder2.replace(baseFolder1, "");
			relativeFolder2 = relativeFolder2.replace(baseFolder2, "");

			if (relativeFolder1.equals(relativeFolder2) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return isSameFilename
	 */
	public boolean isSameFilename() {
		if (files.size() <= 0) {
			return false;
		}

		String filename1 = files.get(0);
		filename1 = FileUtil.getFilename(filename1);

		for (int i = 1; i < files.size(); i++) {
			String filename2 = files.get(i);
			filename2 = FileUtil.getFilename(filename2);

			if (filename1.equals(filename2) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if other duplicate equals folders
	 * 
	 * @param other Other Duplicate
	 * @return True if equals folder duplicate, false otherwise
	 */
	public boolean equalsFolderDuplicate(Duplicate other) {
		List<String> otherFolders = other.getFolders();
		int foundCount = 0;
		for (String otherFolder : otherFolders) {
			for (String folder : folders) {
				if (otherFolder.equals(folder)) {
					foundCount++;
					break;
				}
			}
		}

		return foundCount == otherFolders.size() && folders.size() >= foundCount;
	}

	/**
	 * @return Folders
	 */
	public List<String> getFolders() {
		return folders;
	}
}
