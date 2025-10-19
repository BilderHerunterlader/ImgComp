package ch.supertomcat.imgcomp;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.comparator.HashComparatorTask;
import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.imgcomp.gui.GuiMode;
import ch.supertomcat.imgcomp.gui.MainWindow;
import ch.supertomcat.imgcomp.hasher.HashTask;
import ch.supertomcat.imgcomp.hasher.ImageHashList;
import ch.supertomcat.imgcomp.hasher.ImageHashUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Class which contains the main-Method
 */
public class ImgComp {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationMain applicationMain = new ApplicationMain("ImgComp", null, true, false, ImgComp.class) {
			@Override
			protected void main(String[] args) {
				Logger logger = LoggerFactory.getLogger(ImgComp.class);

				Options options = createCommandLineOptions();
				CommandLineParser parser = new DefaultParser();
				CommandLine cmd;
				try {
					cmd = parser.parse(options, args);
				} catch (ParseException e) {
					logger.error("Could not parse command line arguments", e);
					printHelp(options);
					System.exit(1);
					return;
				}

				if (cmd.getOptions().length == 0 && cmd.getArgList().isEmpty()) {
					MainWindow mainWindow = new MainWindow(GuiMode.NORMAL, SearchMode.NORMAL_SEARCH);
					mainWindow.setVisible(true);
					return;
				}

				boolean help = cmd.hasOption("help");
				boolean gui = cmd.hasOption("gui");

				boolean hashMode = cmd.hasOption("hash");
				boolean recursive = cmd.hasOption("R");
				String filter = cmd.getOptionValue("filter", (String)null);

				boolean compMode = cmd.hasOption("comp");
				boolean reverse = cmd.hasOption("reverse");
				boolean noDuplicates = cmd.hasOption("noDups");
				boolean filenames = cmd.hasOption("filenames");
				boolean foldersOnly = cmd.hasOption("foldersOnly");
				String strSearchMode = cmd.getOptionValue("searchMode", SearchMode.NORMAL_SEARCH.getCommandLineName());
				SearchMode searchMode = SearchMode.getByCommandLineName(strSearchMode);
				if (searchMode == null) {
					logger.error("Invalid search mode: {}", strSearchMode);
					printHelp(options);
					System.exit(1);
					return;
				}

				List<String> remainingArguments = cmd.getArgList();

				if (help || (hashMode && compMode) || (!hashMode && !compMode)) {
					printHelp(options);
					System.exit(1);
					return;
				}

				ProgressObserver progress = new ProgressObserver();
				MainWindow mainWindow = null;
				if (gui) {
					mainWindow = new MainWindow(hashMode ? GuiMode.CLI_HASH : GuiMode.CLI_COMP, searchMode);
					progress.addProgressListener(mainWindow);
					mainWindow.setVisible(true);
				}

				if (hashMode) {
					String filePattern = prepareFilePattern(filter);
					if (filePattern.isEmpty()) {
						filePattern = HashTask.DEFAULT_FILENAME_PATTERN;
						logger.info("No FilePattern detected, using default: {}", filePattern);
					} else {
						logger.info("FilePattern detected: {}", filePattern);
					}

					List<ImageHashList> imageHashLists = new ArrayList<>();

					for (String inputFolder : remainingArguments) {
						if (!inputFolder.endsWith("\\") && !inputFolder.endsWith("/")) {
							inputFolder += FileUtil.FILE_SEPERATOR;
						}

						ImageHashList imageHashList = new ImageHashList(inputFolder, filePattern, recursive);
						imageHashLists.add(imageHashList);
						logger.info("Path detected: {}", imageHashList.getFolder());

						if (mainWindow != null) {
							mainWindow.addInputFile(Paths.get(inputFolder));
						}
					}

					HashTask tashTask = new HashTask(imageHashLists, progress);
					tashTask.start();
				} else if (compMode) {
					List<ImageHashList> imageHashLists = new ArrayList<>();

					for (String inputFile : remainingArguments) {
						ImageHashList imageHashList = ImageHashUtil.readHashList(inputFile);
						imageHashLists.add(imageHashList);
						logger.info("HashList detected: {}", imageHashList.getFolder());

						if (mainWindow != null) {
							mainWindow.addInputFile(Paths.get(inputFile));
						}
					}

					HashComparatorTask comp = new HashComparatorTask(imageHashLists, progress, searchMode, reverse, noDuplicates, filenames, foldersOnly);
					comp.start();
				}

				if (mainWindow != null) {
					mainWindow.dispose();
				}
			}
		};
		applicationMain.start(args);
	}

	private static String prepareFilePattern(String inputPattern) {
		String filePattern = inputPattern;
		filePattern = filePattern.replaceAll("\\[", "\\\\[");
		filePattern = filePattern.replaceAll("\\]", "\\\\]");
		filePattern = filePattern.replaceAll("\\(", "\\\\(");
		filePattern = filePattern.replaceAll("\\)", "\\\\)");
		filePattern = filePattern.replaceAll("\\{", "\\\\{");
		filePattern = filePattern.replaceAll("\\}", "\\\\}");
		filePattern = filePattern.replaceAll("\\|", "\\\\|");
		filePattern = filePattern.replaceAll("\\?", "\\\\?");
		filePattern = filePattern.replaceAll("\\+", "\\\\+");
		filePattern = filePattern.replaceAll("\\^", "\\\\^");
		filePattern = filePattern.replaceAll("\\$", "\\\\$");
		filePattern = filePattern.replaceAll("\\.", "\\\\.");
		filePattern = filePattern.replaceAll("!", "|");
		filePattern = filePattern.replaceAll("\\*", ".*");
		return "(?:" + filePattern + ")";
	}

	/**
	 * Create Command Line Options
	 * 
	 * @return Command Line Options
	 */
	private static Options createCommandLineOptions() {
		Options options = new Options();

		/*
		 * Hash Mode
		 */
		Option hashModeOption = new Option("hash", false, "Creates a Hash-List of Images");
		options.addOption(hashModeOption);

		Option recursiveOption = new Option("R", "recursive", false, "Recursive (Only with -hash or -sort)");
		options.addOption(recursiveOption);

		Option filterOption = Option.builder("filter").argName("filter").hasArg().desc("Filter for filenames").get();
		options.addOption(filterOption);

		/*
		 * Compare Mode
		 */
		Option compModeOption = new Option("comp", false, "Find Duplicates in the Hash-List");
		options.addOption(compModeOption);

		Option reverseOption = new Option("reverse", false, "Prints out only one of the duplicates and all non-Duplicates (Only with -comp)");
		options.addOption(reverseOption);

		Option noDuplicatesOption = new Option("noDups", false, "Prints out only non-Duplicates (Only with -reverse)");
		options.addOption(noDuplicatesOption);

		Option filenamesOption = new Option("filenames", false, "Compares the filenames also, not only the hashes (Only with -comp)");
		options.addOption(filenamesOption);

		Option foldersOnlyOption = new Option("foldersOnly", false, "Prints out only folders in which duplicates where found (Only with -comp)");
		options.addOption(foldersOnlyOption);

		String searchModeCommandLineNames = Arrays.stream(SearchMode.values()).map(x -> x.getCommandLineName()).collect(Collectors.joining(", "));
		Option searchModeOption = Option.builder("searchMode").argName("searchMode").hasArg().desc("Search Mode (Avaible modes: " + searchModeCommandLineNames + ")").get();
		options.addOption(searchModeOption);

		/*
		 * General Options
		 */
		Option guiOption = new Option("gui", false, "Show GUI");
		options.addOption(guiOption);

		Option helpOption = new Option("help", false, "Show this Help");
		options.addOption(helpOption);

		return options;
	}

	/**
	 * Print Help
	 * 
	 * @param options Command Line Options
	 */
	private static void printHelp(Options options) {
		try {
			HelpFormatter helpFormatter = HelpFormatter.builder().get();
			helpFormatter.printHelp(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_NAME) + " "
					+ ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION), "", options, "", false);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(ImgComp.class);
			logger.error("Could not print help", e);
		}
	}
}
