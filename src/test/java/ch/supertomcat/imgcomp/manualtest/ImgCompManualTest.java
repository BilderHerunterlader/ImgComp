package ch.supertomcat.imgcomp.manualtest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.ImgComp;

@SuppressWarnings("javadoc")
public class ImgCompManualTest {
	@Test
	public void testCompGaleria() {
		String danielleFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes_danielle.txt";
		String galeriaFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes-galeria.txt";
		String[] args = new String[] { "-comp", "-searchMode", "listexclusive", "-gui", galeriaFile, danielleFile };
		ImgComp.main(args);
	}

	@Test
	public void testCompGoddessTemple() {
		String danielleFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes_danielle.txt";
		String goddessTempleFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes-GoddessTemple.txt";
		String[] args = new String[] { "-comp", "-searchMode", "listexclusive", goddessTempleFile, danielleFile };
		ImgComp.main(args);
	}

	@Test
	public void testCompGoddessTemple2() {
		String galeriaFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes-galeria.txt";
		String goddessTempleFile = "F:\\8DaniellePanabaker\\_Private\\xxDani\\ImageHashes-GoddessTemple.txt";
		String[] args = new String[] { "-comp", "-searchMode", "listexclusive", goddessTempleFile, galeriaFile };
		ImgComp.main(args);
	}

	@Test
	public void testDelDuplicates() throws IOException {
		String file = "F:\\8DaniellePanabaker\\_Private\\xxDani\\DelDuplicates.txt";
		try (FileInputStream in = new FileInputStream(file); BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String filePath = line.substring(1, line.length() - 1);
				File f = new File(filePath);
				assertTrue(filePath.startsWith("F:\\BH\\daniellepanabaker.com.br__galeria\\"));
				if (f.exists()) {
					Files.delete(f.toPath());
				}
			}
		}
	}

	@Test
	public void testDelEmptyFolders() {
		String folder = "F:\\BH\\daniellepanabaker.com.br__galeria\\";
		deleteEmptyDir(new File(folder), new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				return false;
			}
		});
	}

	private void deleteEmptyDir(File folder, FileFilter fileFilter) {
		File[] subFolders = folder.listFiles(fileFilter);
		if (subFolders == null) {
			fail("Cloud not list folder: " + folder);
			return;
		}

		for (File subFolder : subFolders) {
			deleteEmptyDir(subFolder, fileFilter);
		}

		if (folder.list().length == 0) {
			LoggerFactory.getLogger(getClass()).info("Delete empty folder: {}", folder);
			folder.delete();
		}
	}
}
