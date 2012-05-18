package net.timendum.pdf;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.TextPosition;

public class PDFText2HTML extends LocalPDFTextStripper {

	private static final float DELTA = 2f;

	private final StatisticParser statisticParser;

	protected float averangeLeftMargin;
	//	protected double minLeftMargin;
	protected double maxLeftMargin;
	protected double minRightMargin;
	//	protected double maxRightMargin;
	protected float averangeFontSize;
	private float averangeLastLine;
	private float averangeLineSpacing;

	protected float minBoxMean;
	protected float maxBoxMean;


	public PDFText2HTML(String encoding, StatisticParser statisticParser) throws IOException {
		super(encoding);
		this.statisticParser = statisticParser;
		setPageStart("");
		setPageEnd("");
		setArticleStart("");
		setArticleEnd("");
		setParagraphStart("");
		setParagraphEnd(systemLineSeparator);
	}

	@Override
	protected void writeHeader() throws IOException {
		StringBuilder buf = new StringBuilder();
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n\"http://www.w3.org/TR/html4/loose.dtd\">\n");

		buf.append("<html><head>");
		buf.append("<title>" + escape(getTitle()) + "</title>\n");
		if (outputEncoding != null) {
			buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + outputEncoding + "\">\n");
		}
		String author = getAuthor();
		if (author != null && !author.isEmpty()) {
			buf.append("<meta name=\"Author\" content=\"");
			buf.append(escape(author));
			buf.append("\">");
		}

		buf.append("</head>\n");
		buf.append("<body>\n");
		output.write(buf.toString());
	}

	@Override
	protected String getTitle() {
		String titleGuess = document.getDocumentInformation().getTitle();
		if ((titleGuess != null) && (titleGuess.length() > 0)) {
			return titleGuess;
		}
		return "";
	}

	protected String getAuthor() {
		String authorGuess = document.getDocumentInformation().getAuthor();
		if ((authorGuess != null) && (authorGuess.length() > 0)) {
			return authorGuess;
		}
		return "";
	}

	@Override
	public void writeText(PDDocument doc, Writer outputStream) throws IOException {
		averangeFontSize = statisticParser.getAverangeFontSize();

		averangeLeftMargin = statisticParser.getAverangeLeftMargin();
		float marginDelta = averangeFontSize * DELTA;
		maxLeftMargin = averangeLeftMargin + marginDelta;
		minRightMargin = statisticParser.getAverangeRightMargin() - marginDelta;

		averangeLastLine = statisticParser.getAverangeLastLine();
		averangeLineSpacing = statisticParser.getAverangeLineSpacing();

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
		prevLineY = -1f;
	}


	private String align = null;
	private String lineSpacing = null;
	private boolean startP = false;
	private boolean endP = false;
	private String lastStyle = null;
	private float prevLineY = -1f;
	private boolean pageBreak = false;

	@Override
	protected void writeStringBefore(TextPosition text, String c, String normalized) throws IOException {
		String style = null;
		if (text.getCharacter() == null) {
			style = lastStyle;
		} else {
			style = parseStyle(text);
		}


		if (lastStyle == null || !lastStyle.equals(style)) {
			if (lastStyle != null) {
				output.write("</span>");
			}
			if (style != null) {
				output.write("<span style='" + style + "'>");
			}
			lastStyle = style;
		}

	}

	private String parseStyle(TextPosition text) {
		StringBuilder sb = new StringBuilder();
		int fontSizes = parseFont(text);
		if (fontSizes > 0) {
			sb.append("font-size: ");
			sb.append(fontSizes);
			sb.append("%;");
		}
		if (StatisticParser.isBold(text.getFont().getFontDescriptor())) {
			sb.append("font-weight: bold;");
		}
		if (StatisticParser.isItalic(text)) {
			sb.append("font-style: italic;");
		}

		if (sb.length() > 0) {
			return sb.toString();
		}
		return null;
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
	protected void writeLineStart(List<TextPosition> line) throws IOException {
		if (isLineEmpty(line)) {
			return;
		}
		align = null;
		lineSpacing = null;
		endP = false;
		super.writeLineStart(line);
		parseAlign(line);
		parseLineSpace(line);
		String tag = writeStartTag();
		if (tag != null) {
			output.append(tag);
		}
	}

	@Override
	protected void writeLineEnd(List<TextPosition> line) throws IOException {
		if (isLineEmpty(line)) {
			return;
		}
		super.writeLineEnd(line);

		if (lastStyle != null) {
			output.append("</span>");
			lastStyle = null;
		}

		String tag = writeEndTag();
		if (tag != null) {
			output.append(tag);
		}
	}

	protected String writeStartTag() throws IOException {
		if (align != null || lineSpacing != null) {
			StringBuilder sb = new StringBuilder();
			if (startP) {
				sb.append("</p>");
				startP = false;
			}
			sb.append("<div style='");
			if (lineSpacing != null) {
				sb.append("margin-top: ");
				sb.append(lineSpacing);
				sb.append(';');
			}
			addPageBreak(sb);
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
			StringBuilder sb = new StringBuilder();
			sb.append("<p");
			if (pageBreak) {
				sb.append(" style='");
				addPageBreak(sb);
				sb.append('\'');
			}
			sb.append('>');
			return sb.toString();
		}
		return null;

	}

	private void addPageBreak(StringBuilder sb) {
		if (pageBreak) {
			sb.append("page-break-before: always;");
			pageBreak = false;
		}
	}

	protected String writeEndTag() throws IOException {
		if (align != null || lineSpacing != null) {
			return "</div>";
		}

		if (endP && startP) {
			startP = false;
			return "</p>";
		}
		return null;
	}

	protected void parseLineSpace(List<TextPosition> line) {
		float lineY = getFirstTrimmed(line).getY();
		if (prevLineY >= 0f && lineY - prevLineY > averangeLineSpacing) {
			float perc = (lineY - prevLineY - averangeLineSpacing) / averangeFontSize;
			if (perc > 0.2f) {
				lineSpacing = perc + "em";
			}
		}
		prevLineY = lineY;
	}

	@Override
	protected void endPage(PDPage page) throws IOException {
		if (prevLineY > -1f && ((averangeLastLine - prevLineY) > averangeFontSize)) {
			pageBreak = true;
		}
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
		end = lastText.getX() + lastText.getWidth();
		if (end == -1 || lastText.getCharacter().trim().isEmpty()) {
			return;
		}

		if (start > maxLeftMargin /*&& end < minRightMargin*/) {
			// too much lineSpacing
			float lineMean = (end + start) / 2;
			if (lineMean > minBoxMean && lineMean < maxBoxMean) {
				// centered
				align = "center";
			} else if (end > minRightMargin) {
				// right
				align = "right";
			} else {
				// System.err.println("Strange line: " + line);
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
