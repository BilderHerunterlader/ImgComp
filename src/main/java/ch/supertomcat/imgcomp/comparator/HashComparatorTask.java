package ch.supertomcat.imgcomp.comparator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.hasher.Hash;
import ch.supertomcat.imgcomp.hasher.ImageHashList;
import ch.supertomcat.imgcomp.task.ImgCompTaskBase;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Comparater-Class which provides methods to find duplicates in a hash-list
 */
public class HashComparatorTask extends ImgCompTaskBase {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Image Hash Lists to compare
	 */
	private final List<ImageHashList> imageHashLists;

	/**
	 * Search Mode
	 */
	private final SearchMode searchMode;

	/**
	 * Reverse Flag
	 */
	private final boolean reverse;

	/**
	 * No Dupcliates Flag
	 */
	private final boolean noDuplicates;

	/**
	 * Filenames Flag
	 */
	private final boolean filenames;

	/**
	 * Folders only Flag
	 */
	private final boolean foldersOnly;

	/**
	 * Duplicates
	 */
	private List<Duplicate> duplicates = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param imageHashLists Image Hash Lists
	 * @param progress Progress
	 * @param searchMode Search Mode
	 * @param reverse Prints out only one of the duplicates and all non-Duplicates
	 * @param noDuplicates Prints out only non-Duplicates (Only used when reverse is true)
	 * @param filenames Compares the filenames also, not only the hashes
	 * @param foldersOnly Prints out only folders in which duplicates where found
	 */
	public HashComparatorTask(List<ImageHashList> imageHashLists, ProgressObserver progress, SearchMode searchMode, boolean reverse, boolean noDuplicates, boolean filenames, boolean foldersOnly) {
		super(progress);
		this.imageHashLists = imageHashLists;
		this.searchMode = searchMode;
		this.reverse = reverse;
		this.noDuplicates = noDuplicates;
		this.filenames = filenames;
		this.foldersOnly = foldersOnly;
	}

	@Override
	protected void startTask() {
		compareHashes();
	}

	/**
	 * Returns the duplicates
	 * 
	 * @return duplicates
	 */
	public List<Duplicate> getDuplicates() {
		return duplicates;
	}

	/**
	 * Sets the duplicates
	 * 
	 * @param duplicates duplicates
	 */
	public void setDuplicates(List<Duplicate> duplicates) {
		this.duplicates = duplicates;
	}

	/**
	 * Returns the imageHashLists
	 * 
	 * @return imageHashLists
	 */
	public List<ImageHashList> getImageHashLists() {
		return imageHashLists;
	}

	/**
	 * Returns the searchMode
	 * 
	 * @return searchMode
	 */
	public SearchMode getSearchMode() {
		return searchMode;
	}

	/**
	 * Returns the reverse
	 * 
	 * @return reverse
	 */
	public boolean isReverse() {
		return reverse;
	}

	/**
	 * Returns the noDuplicates
	 * 
	 * @return noDuplicates
	 */
	public boolean isNoDuplicates() {
		return noDuplicates;
	}

	/**
	 * Returns the filenames
	 * 
	 * @return filenames
	 */
	public boolean isFilenames() {
		return filenames;
	}

	/**
	 * Returns the foldersOnly
	 * 
	 * @return foldersOnly
	 */
	public boolean isFoldersOnly() {
		return foldersOnly;
	}

	/**
	 * Compare Hashes
	 */
	private void compareHashes() {
		progress.progressChanged("Searching for duplicates...");
		progress.progressModeChanged(false);

		List<Hash> currentHashes;
		for (int cl = 0; cl < imageHashLists.size(); cl++) {
			currentHashes = imageHashLists.get(cl).getHashes();
			progress.progressChanged(0, currentHashes.size() - 1, 0);

			for (int i = 0; i < currentHashes.size(); i++) {
				if (stop) {
					return;
				}

				boolean bNotFound = true;

				if (searchMode != SearchMode.LIST_EXCLUSIVE_SEARCH) {
					for (int j = i + 1; j < currentHashes.size(); j++) {
						if (stop) {
							return;
						}

						// Compare
						boolean isDuplicate = compareHash(currentHashes.get(i), currentHashes.get(j), duplicates);
						if (isDuplicate) {
							bNotFound = false;
						}
					}
				}

				if (searchMode != SearchMode.LIST_INCLUSIVE_SEARCH) {
					ImageHashList otherList;
					for (int ol = cl + 1; ol < imageHashLists.size(); ol++) {
						if (stop) {
							return;
						}

						otherList = imageHashLists.get(ol);
						for (int x = 0; x < otherList.getHashes().size(); x++) {
							// Compare
							boolean isDuplicate = compareHash(currentHashes.get(i), otherList.getHashes().get(x), duplicates);
							if (isDuplicate) {
								bNotFound = false;
								logger.info("Duplicate found: {}, {}", currentHashes.get(i).getFile(), otherList.getHashes().get(x).getFile());
							}
						}
					}
				}

				if (reverse && noDuplicates && bNotFound) {
					duplicates.add(new Duplicate(currentHashes.get(i).getHash(), currentHashes.get(i).getFile()));
				} else if (reverse && (noDuplicates == false)) {
					duplicates.add(new Duplicate(currentHashes.get(i).getHash(), currentHashes.get(i).getFile()));
				}
				progress.progressChanged(i);
			}
		}

		if (foldersOnly) {
			removeFolderDuplicates(duplicates);
		}

		writeResultToFile(duplicates, foldersOnly, reverse, "./Duplicates.txt");
		progress.progressCompleted();
		progress.progressChanged("Done");
	}

	private boolean compareHash(Hash hash1, Hash hash2, List<Duplicate> duplicates) {
		boolean result = false;

		if (hash1.getHash().equals(hash2.getHash())) {
			if (filenames) {
				if (hash1.getFilename().equals(hash2.getFilename()) == false) {
					return result;
				}
			}
			result = true;
			if (!reverse) {
				Duplicate duplicate = getDuplicateForHash(duplicates, hash1.getHash());
				if (duplicate == null) {
					duplicate = new Duplicate(hash1.getHash(), hash1.getFile());
					duplicates.add(duplicate);
				}
				duplicate.addFile(hash2.getFile());
			}
		}

		return result;
	}

	/**
	 * @param duplicates
	 */
	private void removeFolderDuplicates(List<Duplicate> duplicates) {
		for (int i = 0; i < duplicates.size(); i++) {
			for (int x = 0; x < duplicates.size(); x++) {
				if (i == x) {
					continue;
				}
				if (duplicates.get(i).equalsFolderDuplicate(duplicates.get(x))) {
					duplicates.remove(x);
					x--;
				}
			}
		}
	}

	/**
	 * Returns the duplicate for the hash or null if not found
	 * 
	 * @param duplicates Duplicates
	 * @param hash Hash
	 * @return the duplicate for the hash or null if not found
	 */
	private Duplicate getDuplicateForHash(List<Duplicate> duplicates, String hash) {
		for (int i = 0; i < duplicates.size(); i++) {
			if (duplicates.get(i).getHash().equals(hash)) {
				return duplicates.get(i);
			}
		}
		return null;
	}

	/**
	 * Writes Duplicates to File
	 * 
	 * @param duplicates Duplicates
	 * @param bFoldersOnly Folders Only
	 * @param bReverse Reverse
	 * @param file File
	 */
	private void writeResultToFile(List<Duplicate> duplicates, boolean bFoldersOnly, boolean bReverse, String file) {
		logger.info("Writing result to file: {}", file);
		try (FileOutputStream out = new FileOutputStream(file); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
			for (int i = 0; i < duplicates.size(); i++) {
				if (stop) {
					break;
				}
				Duplicate duplicate = duplicates.get(i);
				if (!bReverse) {
					bw.write("Duplicates Found (" + duplicate.getHash() + "):");
					if (duplicate.isSameRelativeFolder() == false) {
						bw.write(" RelativeFolderDifference!");
					}
					if (duplicate.isSameFilename() == false) {
						bw.write(" FilenameDifference!");
					}
					bw.write("\n");
				}

				if (bFoldersOnly) {
					for (int c = 0; c < duplicate.getFolders().size(); c++) {
						bw.write("\t" + duplicate.getFolders().get(c) + "\n");
					}
				} else if (bReverse) {
					bw.write(duplicate.getHash() + "\t" + duplicate.getFiles().get(0) + "\n");
				} else {
					for (int c = 0; c < duplicate.getFiles().size(); c++) {
						bw.write("\t\"" + duplicate.getFiles().get(c) + "\"\n");
					}
				}
				bw.flush();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
