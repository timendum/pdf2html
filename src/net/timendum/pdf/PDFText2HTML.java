package net.timendum.pdf;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.TextPosition;

public class PDFText2HTML extends LocalPDFTextStripper {

	private static final float DELTA = 2f;

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

	protected StatisticParser statisticParser;

	protected float averangeLeftMargin;
	//	protected double minLeftMargin;
	protected double maxLeftMargin;
	protected double minRightMargin;
	//	protected double maxRightMargin;
	protected float averangeFontSize;

	protected float minBoxMean;
	protected float maxBoxMean;
	private String align = null;
	private boolean startP = false;
	private boolean endP = false;

	public PDFText2HTML(String encoding) throws IOException {
		super(encoding);
		setPageStart("");
		setPageEnd("");
		setArticleStart("");
		setArticleEnd("");
		setParagraphStart("");
		setParagraphEnd(systemLineSeparator);
	}

	@Override
	public void writeText(PDDocument doc, Writer outputStream) throws IOException {
		statisticParser = new StatisticParser();

		statisticParser.writeText(doc, output = NULL_WRITER);
		System.err.println(statisticParser.toString());

		averangeFontSize = statisticParser.getAverangeFontSize();

		averangeLeftMargin = statisticParser.getAverangeLeftMargin();
		float marginDelta = averangeFontSize * DELTA;
		//		minLeftMargin = averangeLeftMargin - marginDelta;
		maxLeftMargin = averangeLeftMargin + marginDelta;

		minRightMargin = statisticParser.getAverangeRightMargin() - marginDelta;
		//		maxRightMargin = statisticParser.getAverangeRightMargin() + marginDelta;


		//outputStream = new PrintWriter(System.out);

		super.writeText(doc, outputStream);
	}

	@Override
	protected void startPage(PDPage page) throws IOException {
		PDRectangle currentMediaBox = page.findMediaBox();
		float mediaBoxWidth = currentMediaBox.getWidth();
		float boxMean = mediaBoxWidth / 2;
		minBoxMean = boxMean - averangeFontSize * DELTA;
		maxBoxMean = boxMean + averangeFontSize * DELTA;
	}


	@Override
	protected void writeLineStart(List<TextPosition> line) throws IOException {
		fontSizes = new ArrayList<Integer>(line.size());
		align = null;
		endP = false;
		super.writeLineStart(line);
		parseAlign(line);
		String tag = writeStartTag();
		if (tag != null) {
			output.append(tag);
		}
	}

	protected void normalizeStart(TextPosition text) {
		int font = parseFont(text);
		fontSizes.add(font);
	}

	@Override
	protected void normalizePost(List<TextPosition> word, String lineStr, LinkedList<String> normalized)
			throws IOException {

	}

	private List<Integer> fontSizes = null;

	@Override
	protected void normalizePre(List<TextPosition> word, String lineStr, LinkedList<String> normalized)
			throws IOException {
		if (word.size() < 1) {
			fontSizes.add(Integer.valueOf(-1));
			return;
		}
		TextPosition text = getFirstTrimmed(word);
		float start = text.getFontSizeInPt();
		float end = getLastTrimmed(word).getFontSizeInPt();

		if (start != end) {
			System.err.println("Parola non uniforme " + word);
		}

		int font = parseFont(text);
		fontSizes.add(font);
	}

	int lastFont = 0;

	@Override
	protected void writeLineStringBefore(int i, List<String> line) throws IOException {
		int current = fontSizes.get(i).intValue();
		if (current > 0) {
			if (lastFont != 0) {
				output.write("</span>");
			}
			if (current > 0) {
				output.write("<span style='font-size: " + current + "%'>");
			}
			lastFont = current;
		}

	}

	private int parseFont(TextPosition text) {
		int fontSize = -1;
		if (text instanceof WordSeparator) {
			//	fontSize = -1;
		} else if (text.getFontSizeInPt() != averangeFontSize) {
			fontSize = Math.round(text.getFontSizeInPt() * 100 / averangeFontSize);
		} else {
			//	fontSize = -1;
		}
		return fontSize;

	}

	@Override
	protected void writeLineEnd(List<TextPosition> line) throws IOException {
		super.writeLineEnd(line);

		if (lastFont > 0) {
			output.append("</span>");
			lastFont = 0;
		}


		String tag = writeEndTag();
		if (tag != null) {
			output.append(tag);
		}
	}

	protected String writeStartTag() throws IOException {
		if (align != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div style='");
			if (align != null) {
				sb.append("text-align: ");
				sb.append(align);
				sb.append(';');
			}
			sb.append("'>");
			return sb.toString();
		}

		if (startP == false) {
			startP = true;
			return "<p>";
		}
		return null;

	}

	protected String writeEndTag() throws IOException {
		if (align != null) {
			return "</div>";
		}

		if (endP && startP) {
			startP = false;
			return "</p>";
		}
		return null;
	}

	@Override
	protected void writeLine(List<String> line, boolean isRtlDominant) throws IOException {
		super.writeLine(line, isRtlDominant);
	}

	private static TextPosition getFirstTrimmed(List<TextPosition> line) {
		String c;
		for (int i = 0; i < line.size(); i++) {
			if (line.get(i) == null) {
				continue;
			}
			c = line.get(i).getCharacter();
			if (c != null && c.trim().length() > 0) {
				return line.get(i);
			}
		}
		return line.get(0);
	}

	private static TextPosition getLastTrimmed(List<TextPosition> line) {
		String c;
		for (int i = line.size() - 1; i >= 0; i--) {
			if (line.get(i) == null) {
				continue;
			}
			c = line.get(i).getCharacter();
			if (c != null && c.trim().length() > 0) {
				return line.get(i);
			}
		}
		return line.get(line.size() - 1);
	}

	protected void parseAlign(List<TextPosition> line) {

		if (line.size() < 1) {
			return;
		}

		float start = -1;
		TextPosition firstText = getFirstTrimmed(line);
		start = firstText.getX();
		if (start == -1 || firstText.getCharacter().trim().isEmpty()) {
			return;
		}

		float end = -1;
		TextPosition lastText = getLastTrimmed(line);
		end = lastText.getX();
		if (end == -1 || lastText.getCharacter().trim().isEmpty()) {
			return;
		}

		if (start > maxLeftMargin /*&& end < minRightMargin*/) {
			// too much margin
			float lineMean = (end + start) / 2;
			if (lineMean > minBoxMean && lineMean < maxBoxMean) {
				// centered
				align = "center";
			} else if (end > minRightMargin) {
				// right
				align = "right";
			} else {
				System.err.println("Linea strana: " + line);
			}
		}

		if (align == null) {
			if (start > averangeLeftMargin) {
				// intent
				startP = false;
			}

			if (end < minRightMargin) {
				// small line
				endP = true;
			}
		}
	}

	@Override
	protected void startArticle(boolean isltr) throws IOException {

	}

	@Override
	protected void endArticle() throws IOException {
	}
}
