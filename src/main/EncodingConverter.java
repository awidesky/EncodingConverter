package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class EncodingConverter {

	public static File saveDir;
	public static Charset from;
	public static Charset to;
	public static int BUFFERSIZEINCHAR = 1024;

	private static void printUsage() {
		System.out.println("usage : java -jar EncodingConverter.jar <encodingOfOriginal> <encodingOfResult>");
		System.out.println("write \"defalt\" to indecate system defalt charset (" + Charset.defaultCharset().name() + ")");
		System.out.println("\nAvailable charsets : ");
		Charset.availableCharsets().entrySet().stream().map(Map.Entry<String, Charset>::getValue).map(Charset::name).forEach(System.out::println);;
	}

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {

		if (args.length == 2) {
			try {
				from = (args[0].equals("defalt")) ? Charset.defaultCharset() : Charset.forName(args[0]);
				to = (args[1].equals("defalt")) ? Charset.defaultCharset() : Charset.forName(args[1]);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.flush();
				System.out.println();
				printUsage();
				return;
			}
		} else {
			printUsage();
			return;
		}

		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setMultiSelectionEnabled(true);
		jfc.addChoosableFileFilter(new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().endsWith(".txt") || f.getName().endsWith(".jpg")
						|| f.getName().endsWith(".md") || f.getName().endsWith(".java") || f.getName().endsWith(".c")
						|| f.getName().endsWith(".h") || f.getName().endsWith(".cpp") || f.getName().endsWith(".hpp")
						|| f.getName().endsWith(".js") || f.getName().endsWith(".html") || f.getName().endsWith(".css"))
					return true;
				else
					return false;
			}

			public String getDescription() {
				return "Text files (*.txt, *.md, *.java, *.c, *.h, *.cpp, *.hpp, *.js, *.html, *.css)";
			}

		});
		jfc.setDialogTitle("Choose files to change encoding!");

		if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File[] flist = jfc.getSelectedFiles();

		JFileChooser jfc1 = new JFileChooser();
		jfc1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc1.setCurrentDirectory(flist[0].getParentFile());
		jfc1.setDialogTitle("Choose directory to put result file(s)!");

		if (jfc1.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		saveDir = jfc1.getSelectedFile();

		if (flist.length == 0) {
			System.err.println("Yout chose no file!");
			return;
		}

		Arrays.stream(flist).parallel().forEach(EncodingConverter::convert);

		SwingUtilities.invokeAndWait(() -> {
			final JDialog dialog = new JDialog();
			dialog.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(dialog,
					"Changed files are in following folder :\n" + saveDir.getAbsolutePath(), "done!",
					JOptionPane.INFORMATION_MESSAGE);
			dialog.dispose();
		});

	}

	public static void convert(File f) {
		try (BufferedReader br = new BufferedReader(new FileReader(f, from));
				PrintWriter pw = new PrintWriter(new FileWriter(saveDir.getAbsolutePath() + File.separator + f.getName(), to))) {
			br.lines().forEach(pw::println);
		} catch (Exception e) {
			System.err.println();
			System.err.println("Failed to convert file " + f.getAbsolutePath() + f.getName());
			e.printStackTrace();
			System.err.println();
		}
	}

}
