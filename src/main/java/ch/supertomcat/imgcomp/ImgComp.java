package ch.supertomcat.imgcomp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.comparator.HashComparatorTask;
import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.imgcomp.gui.GuiMode;
import ch.supertomcat.imgcomp.gui.MainWindow;
import ch.supertomcat.imgcomp.hasher.HashTask;
import ch.supertomcat.imgcomp.hasher.ImageHashList;
import ch.supertomcat.imgcomp.hasher.ImageHashUtil;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.application.ApplicationUtil;
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
		try {
			ApplicationProperties.initProperties(ImgComp.class.getResourceAsStream("/Application_Config.properties"));
		} catch (IOException e) {
			// Logger is not initialized at this point
			System.err.println("Could not initialize application properties");
			e.printStackTrace();
			ApplicationUtil.writeBasicErrorLogfile(new File("ImgComp-Error.log"), "Could not initialize application properties:\n" + formatStackTrace(e));
			System.exit(1);
		}

		String jarFilename = ApplicationUtil.getThisApplicationsJarFilename(ImgComp.class);
		ApplicationProperties.setProperty("JarFilename", jarFilename);

		// Geth the program directory
		String appPath = ApplicationUtil.getThisApplicationsPath(!jarFilename.isEmpty() ? jarFilename : ApplicationProperties.getProperty("ApplicationShortName") + ".jar");
		ApplicationProperties.setProperty("ApplicationPath", appPath);

		String programUserDir = System.getProperty("user.home") + FileUtil.FILE_SEPERATOR + "." + ApplicationProperties.getProperty("ApplicationShortName") + FileUtil.FILE_SEPERATOR;
		ApplicationProperties.setProperty("ProfilePath", programUserDir);
		ApplicationProperties.setProperty("LogsPath", programUserDir);

		String logFilename = ApplicationProperties.getProperty("ApplicationShortName") + ".log";
		// Loggers can be created after this point
		System.setProperty("bhlog4jlogfile", programUserDir + FileUtil.FILE_SEPERATOR + logFilename);

		ApplicationUtil.initializeSLF4JUncaughtExceptionHandler();

		// Write some useful info to the logfile
		ApplicationUtil.logApplicationInfo();

		// Delete old log files
		ApplicationUtil.deleteOldLogFiles(7, logFilename, ApplicationProperties.getProperty("LogsPath"));

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
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				logger.error("Could not set SystemLookAndFeel", e);
			}

			MainWindow mainWindow = new MainWindow(GuiMode.NORMAL, SearchMode.NORMAL_SEARCH);
			mainWindow.setVisible(true);
			return;
		}

		boolean help = cmd.hasOption("help");
		boolean gui = cmd.hasOption("gui");

		boolean hashMode = cmd.hasOption("hash");
		boolean recursive = cmd.hasOption("R");
		String filter = cmd.getOptionValue("filter", null);

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
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				logger.error("Could not set SystemLookAndFeel", e);
			}

			mainWindow = new MainWindow(hashMode ? GuiMode.CLI_HASH : GuiMode.CLI_COMP, searchMode);
			progress.addProgressListener(mainWindow);
			mainWindow.setVisible(true);
		}

		if (hashMode) {
			String filePattern = prepareFilePattern(filter);
			if (filePattern.isEmpty()) {
				filePattern = HashTask.DEFAULT_FILENAME_PATTERN;
				logger.info("No FilePattern detected, using default: " + filePattern);
			} else {
				logger.info("FilePattern detected: " + filePattern);
			}

			List<ImageHashList> imageHashLists = new ArrayList<>();

			for (String inputFolder : remainingArguments) {
				if (!inputFolder.endsWith("\\") && !inputFolder.endsWith("/")) {
					inputFolder += FileUtil.FILE_SEPERATOR;
				}

				ImageHashList imageHashList = new ImageHashList(inputFolder, filePattern, recursive);
				imageHashLists.add(imageHashList);
				logger.info("Path detected: " + imageHashList.getFolder());

				if (mainWindow != null) {
					mainWindow.addInputFile(new File(inputFolder));
				}
			}

			HashTask tashTask = new HashTask(imageHashLists, progress);
			tashTask.start();
		} else if (compMode) {
			List<ImageHashList> imageHashLists = new ArrayList<>();

			for (String inputFile : remainingArguments) {
				ImageHashList imageHashList = ImageHashUtil.readHashList(inputFile);
				imageHashLists.add(imageHashList);
				logger.info("HashList detected: " + imageHashList.getFolder());

				if (mainWindow != null) {
					mainWindow.addInputFile(new File(inputFile));
				}
			}

			HashComparatorTask comp = new HashComparatorTask(imageHashLists, progress, searchMode, reverse, noDuplicates, filenames, foldersOnly);
			comp.start();
		}

		if (mainWindow != null) {
			mainWindow.dispose();
		}
	}

	/**
	 * Format Stacktrace to String
	 * 
	 * @param throwable Throwable
	 * @return Stacktrace as String
	 */
	private static String formatStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
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

		Option filterOption = Option.builder("filter").argName("filter").hasArg().desc("Filter for filenames").build();
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
		Option searchModeOption = Option.builder("searchMode").argName("searchMode").hasArg().desc("Search Mode (Avaible modes: " + searchModeCommandLineNames + ")").build();
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
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(ApplicationProperties.getProperty("ApplicationName") + " " + ApplicationProperties.getProperty("ApplicationVersion"), options);
	}
}
