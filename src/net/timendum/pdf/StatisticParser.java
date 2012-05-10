package net.timendum.pdf;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.TextPosition;

public class StatisticParser extends LocalPDFTextStripper {
	private float pages = 0;
	private float lines = 0;
	//	private float leftMargin = 0;
	private HashMap<Float, Integer> leftMargin = new HashMap<Float, Integer>();
	//	private float rightMargin = 0;
	private HashMap<Float, Integer> rightMargin = new HashMap<Float, Integer>();
	private float averangeLine = 0;
	private float averangeLeftMargin;
	private float averangeRightMargin;
	private HashMap<Float, Integer> linesFontSize = new HashMap<Float, Integer>();
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
		float start = line.get(0).getX();
		//		leftMargin += start;
		incrementOrAdd(leftMargin, start);
		float end = line.get(line.size() - 1).getX();
		//		rightMargin += end;
		incrementOrAdd(rightMargin, end);

		Float fontSize;
		for (TextPosition t : line) {
			fontSize = t.getFontSizeInPt();
			if (fontSize > 0) {
				incrementOrAdd(linesFontSize, fontSize);
			}
		}
	}

	private static void incrementOrAdd(HashMap<Float, Integer> map, Float key) {
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
	protected PositionWrapper handleLineSeparation(PositionWrapper current, PositionWrapper lastPosition,
			PositionWrapper lastLineStartPosition, float maxHeightForLine) throws IOException {
		return lastLineStartPosition;
	}

	@Override
	protected void isParagraphSeparation(PositionWrapper position, PositionWrapper lastPosition,
			PositionWrapper lastLineStartPosition, float maxHeightForLine) {
		super.isParagraphSeparation(position, lastPosition, lastLineStartPosition, maxHeightForLine);
	}

	@Override
	protected List<String> normalize(List<TextPosition> line, boolean isRtlDominant, boolean hasRtl) throws IOException {
		return Collections.emptyList();
	}

	@Override
	protected void writeCharacters(TextPosition text) throws IOException {
	}

	@Override
	protected void writeHeader() throws IOException {
	}

	@Override
	protected void writeLine(List<String> line, boolean isRtlDominant) throws IOException {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatisticParser [pages=");
		builder.append(pages);
		builder.append(", lines=");
		builder.append(lines);
		builder.append(", averangeLine=");
		builder.append(averangeLine);
		builder.append(", averangeLeftMargin=");
		builder.append(averangeLeftMargin);
		builder.append(", averangeRightMargin=");
		builder.append(averangeRightMargin);
		builder.append(", averangeFontSize=");
		builder.append(averangeFontSize);
		builder.append(", leftMargin=");
		builder.append(leftMargin);
		builder.append(", rightMargin=");
		builder.append(rightMargin);
		builder.append(", linesFontSize=");
		builder.append(linesFontSize);
		builder.append("]");
		return builder.toString();
	}


}
