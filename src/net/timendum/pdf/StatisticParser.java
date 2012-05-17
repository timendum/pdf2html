package net.timendum.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.TextPosition;

public class StatisticParser extends LocalPDFTextStripper {
	private static final int FLAG_FIXED_PITCH = 1;
	private static final int FLAG_SERIF = 2;
	private static final int FLAG_SYMBOLIC = 4;
	private static final int FLAG_SCRIPT = 8;
	private static final int FLAG_NON_SYMBOLIC = 32;
	private static final int FLAG_ITALIC = 64;
	private static final int FLAG_ALL_CAP = 65536;
	private static final int FLAG_SMALL_CAP = 131072;
	private static final int FLAG_FORCE_BOLD = 262144;

	private float pages = 0;
	private float lines = 0;
	//	private float leftMargin = 0;
	private Map<Float, Integer> leftMargin = new HashMap<Float, Integer>();
	//	private float rightMargin = 0;
	private Map<Float, Integer> rightMargin = new HashMap<Float, Integer>();
	private float averangeLine = 0;
	private float averangeLeftMargin;
	private float averangeRightMargin;
	private Map<Float, Integer> linesFontSize = new HashMap<Float, Integer>();
	//private Map<PDFont, Integer> fonts = new HashMap<PDFont, Integer>();
	private float averangeFontSize = 0;

	public StatisticParser() throws IOException {
	}

	@Override
	protected void startPage(PDPage page) throws IOException {
		pages++;
	}

	@Override
	protected void writeLineStart(List<TextPosition> line) {
		lines++;
		float start = getFirstTrimmed(line).getX();
		//		leftMargin += start;
		incrementOrAdd(leftMargin, start);
		float end = getLastTrimmed(line).getX();
		//		rightMargin += end;
		incrementOrAdd(rightMargin, end);

		Float fontSize;
		for (TextPosition t : line) {
			/*PDFont font = t.getFont();
			if (font != null) {
				incrementOrAdd(fonts, font);
			}*/
			fontSize = t.getFontSizeInPt();
			if (fontSize > 0) {
				incrementOrAdd(linesFontSize, fontSize);
			}
		}
	}

	private static <T> void incrementOrAdd(Map<T, Integer> map, T key) {
		Integer count;
		count = map.get(key);
		if (count == null) {
			count = Integer.valueOf(1);
		} else {
			count = Integer.valueOf(count.intValue() + 1);
		}
		map.put(key, count);
	}

	@Override
	public void endDocument(PDDocument pdf) throws IOException {
		averangeLine = lines / pages;
		//		averangeLeftMargin = leftMargin / lines;
		averangeLeftMargin = findMax(leftMargin);
		//		averangeRightMargin = rightMargin / lines;
		averangeRightMargin = findMax(rightMargin);
		averangeFontSize = findMax(linesFontSize);

	}

	private static Float findMax(Map<Float, Integer> map) {
		Float actual = null;
		int max = -1;
		for (Entry<Float, Integer> entry : map.entrySet()) {
			if (entry.getValue().intValue() > max) {
				max = entry.getValue().intValue();
				actual = entry.getKey();
			}
		}
		return actual;
	}

	public float getPages() {
		return pages;
	}

	public float getLines() {
		return lines;
	}

	public float getAverangeLines() {
		return averangeLine;
	}

	public float getAverangeLeftMargin() {
		return averangeLeftMargin;
	}

	public float getAverangeRightMargin() {
		return averangeRightMargin;
	}

	public float getAverangeFontSize() {
		return averangeFontSize;
	}

	@Override
	protected void startArticle() throws IOException {
	}

	@Override
	protected void startArticle(boolean isltr) throws IOException {
	}

	@Override
	protected void endArticle() throws IOException {
	}

	@Override
	protected void endPage(PDPage page) throws IOException {
	}

	@Override
	protected void writeCharacters(TextPosition text) throws IOException {
	}

	@Override
	protected void writeHeader() throws IOException {
	}

	@Override
	protected void writeLineSeparator() throws IOException {
	}

	@Override
	protected void writePageEnd() throws IOException {
	}

	@Override
	protected void writePageSeperator() throws IOException {
	}

	@Override
	protected void writePageStart() throws IOException {
	}

	@Override
	protected void writeParagraphEnd() throws IOException {
	}

	@Override
	protected void writeParagraphSeparator() throws IOException {
	}

	@Override
	protected void writeParagraphStart() throws IOException {
	}

	@Override
	protected void writeString(String chars) throws IOException {
	}

	@Override
	protected void writeWordSeparator() throws IOException {
	}

	public static boolean isItalic(PDFontDescriptor descriptor) {
		if (descriptor.getItalicAngle() != 0f) {
			return true;
		}
		if ((descriptor.getFlags() & FLAG_ITALIC) == FLAG_ITALIC) {
			return true;
		}
		if (descriptor.getFontName() != null && descriptor.getFontName().indexOf("Italic") > -1) {
			return true;
		}
		return false;
	}

	public static boolean isBold(PDFontDescriptor descriptor) {
		if (descriptor.getFontWeight() > 0f) {
			return true;
		}
		if ((descriptor.getFlags() & FLAG_FORCE_BOLD) == FLAG_FORCE_BOLD) {
			return true;
		}
		if (descriptor.getFontName() != null && descriptor.getFontName().indexOf("Bold") > -1) {
			return true;
		}
		return false;
	}

	public static boolean isItalic(TextPosition text) {
		if (isItalic(text.getFont().getFontDescriptor())) {
			return true;
		}
		Matrix textPos = text.getTextPos();
		if (textPos != null && textPos.getXScale() < textPos.getYScale()) {
			return true;
		}
		if (textPos != null && textPos.getXScale() > textPos.getYScale()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pages=");
		builder.append(pages);
		builder.append("\nlines=");
		builder.append(lines);
		//builder.append("\n#fonts=");
		//builder.append(fonts.size());
		builder.append("\naverangeLine=");
		builder.append(averangeLine);
		builder.append("\naverangeLeftMargin=");
		builder.append(averangeLeftMargin);
		builder.append(", #leftMargin=");
		builder.append(leftMargin.size());
		builder.append("\naverangeRightMargin=");
		builder.append(averangeRightMargin);
		builder.append(" #rightMargin=");
		builder.append(rightMargin.size());
		builder.append("\naverangeFontSize=");
		builder.append(averangeFontSize);
		builder.append(" #linesFontSize=");
		builder.append(linesFontSize.size());
		return builder.toString();
	}


}
