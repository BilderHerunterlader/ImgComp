package ch.supertomcat.imgcomp.comparator;

/**
 * Search Mode
 */
public enum SearchMode {
	/**
	 * NormalSearch <BR>
	 * Compares all images against each other
	 */
	NORMAL_SEARCH("normal"),

	/**
	 * InclusiveSearch <BR>
	 * Compares only images in the same directory
	 */
	INCLUSIVE_SEARCH("inclusive"),

	/**
	 * ExclusiveSearch <BR>
	 * Compares only images which are not in the same directory
	 */
	EXCLUSIVE_SEARCH("exclusive"),

	/**
	 * ListInclusiveSearch <BR>
	 * Compares only images in the same input directory and it's subdirectories
	 */
	LIST_INCLUSIVE_SEARCH("listinclusive"),

	/**
	 * ListExclusiveSearch <BR>
	 * Compares only images which are not in the same input directory and it's subdirectories
	 */
	LIST_EXCLUSIVE_SEARCH("listexclusive");

	/**
	 * Command Line Name
	 */
	private final String commandLineName;

	/**
	 * Constructor
	 * 
	 * @param commandLineName Command Line Name
	 */
	private SearchMode(String commandLineName) {
		this.commandLineName = commandLineName;
	}

	/**
	 * Returns the commandLineName
	 * 
	 * @return commandLineName
	 */
	public String getCommandLineName() {
		return commandLineName;
	}

	/**
	 * Get Search Mode by Command Line Name
	 * 
	 * @param name Command Line Name
	 * @return Search Mode or null
	 */
	public static SearchMode getByCommandLineName(String name) {
		for (SearchMode searchMode : SearchMode.values()) {
			if (searchMode.getCommandLineName().equals(name)) {
				return searchMode;
			}
		}
		return null;
	}
}
