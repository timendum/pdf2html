package net.timendum.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFTextStripper;


public class Extract {
	private static final String FORCE = "-force"; //enables pdfbox to skip corrupt objects
	private static final String PASSWORD = "-password";
	private static final String DEBUG = "-debug";
	private static final Writer NULL_WRITER = new Writer() {

		@Override
		public void write(char[] paramArrayOfChar, int paramInt1, int paramInt2) throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
		}
	};

	private boolean debug = true;
	private boolean force;

	public static void main(String[] args) throws Exception {
		Extract extractor = new Extract();
		extractor.startExtraction(args);
	}

	private void startExtraction(String[] args) throws Exception {

		String pdfFile = null;
		String outputFile = null;
		String password = "";
		String ext = ".html";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(PASSWORD)) {
				i++;
				password = args[i];
			} else if (args[i].equals(FORCE)) {
				force = true;
			} else if (args[i].equals(DEBUG)) {
				debug = true;
			} else if (pdfFile == null) {
				pdfFile = args[i];
			} else {
				outputFile = args[i];
			}
		}

		if (pdfFile == null) {
			usage();
			System.exit(1);
		}

		Writer output = null;
		PDDocument document = null;
		try {
			long startTime = startProcessing("Loading PDF " + pdfFile);
			try {
				//basically try to load it from a url first and if the URL
				//is not recognized then try to load it from the file system.
				URL url = new URL(pdfFile);
				document = PDDocument.load(url, force);
				String fileName = url.getFile();
				if (outputFile == null && fileName.length() > 4) {
					outputFile = new File(fileName.substring(0, fileName.length() - 4) + ext).getName();
				}
			} catch (MalformedURLException e) {
				document = PDDocument.load(pdfFile, force);
				if (outputFile == null && pdfFile.length() > 4) {
					outputFile = pdfFile.substring(0, pdfFile.length() - 4) + ext;
				}
			}
			stopProcessing("Time for loading: ", startTime);


			if (document.isEncrypted()) {
				StandardDecryptionMaterial sdm = new StandardDecryptionMaterial(password);
				document.openProtection(sdm);
				AccessPermission ap = document.getCurrentAccessPermission();

				if (!ap.canExtractContent()) {
					throw new IOException("You do not have permission to extract text");
				}
			}


			output = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");

			StatisticParser statisticParser = new StatisticParser();
			startTime = startProcessing("Starting text statistics");
			statisticParser.writeText(document, NULL_WRITER);
			stopProcessing("Time for statistics: ", startTime);

			if (debug) {
				System.err.println(statisticParser.toString());
			}

			PDFTextStripper stripper = new PDFText2HTML("UTF-8", statisticParser);

			startTime = startProcessing("Starting text extraction");
			stripper.writeText(document, output);
			stopProcessing("Time for extraction: ", startTime);

		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
				}
			}
		}
	}


	private long startProcessing(String message) {
		if (debug) {
			System.err.println(message);
		}
		return System.currentTimeMillis();
	}

	private void stopProcessing(String message, long startTime) {
		if (debug) {
			long stopTime = System.currentTimeMillis();
			float elapsedTime = ((float) (stopTime - startTime)) / 1000;
			System.err.println(message + elapsedTime + " seconds");
		}
	}

	private void usage() {
		System.err.println("Usage: java -jar jar Extract <PDF file> [Text File]\n"
			+ "  -password  <password>        Password to decrypt document\n"
			+ "  -encoding  <output encoding> (ISO-8859-1,UTF-16BE,UTF-16LE,...)\n"
			+ "  -console                     Send text to console instead of file\n"
			+ "  -html                        Output in HTML format instead of raw text\n"
			+ "  -sort                        Sort the text before writing\n"
			+ "  -ignoreBeads                 Disables the separation by beads\n"
			+ "  -force                       Enables pdfbox to ignore corrupt objects\n"
			+ "  -debug                       Enables debug output about the time consumption of every stage\n"
			+ "  -startPage <number>          The first page to start extraction(1 based)\n"
			+ "  -endPage <number>            The last page to extract(inclusive)\n"
			+ "  <PDF file>                   The PDF document to use\n"
			+ "  [Text File]                  The file to write the text to\n");
		System.exit(1);

	}
}
