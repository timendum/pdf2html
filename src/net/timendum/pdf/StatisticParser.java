/*
	This file is part of pdf2html.

    pdf2html is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    pdf2html is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with pdf2html.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright by timendum.
    Based on org.apache.pdfbox.util.PDFTextStripper by Apache Software Foundation
 */
package net.timendum.pdf;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.TextPosition;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
	private Multiset<Float> leftMargin = HashMultiset.create();
	//	private float rightMargin = 0;
	private Multiset<Float> rightMargin = HashMultiset.create();
	private float averangeLine = 0;
	private float averangeLeftMargin;
	private float averangeRightMargin;
	private Multiset<Float> linesFontSize = HashMultiset.create();
	//private Map<PDFont, Integer> fonts = new HashMap<PDFont, Integer>();
	private Multiset<Float> lineSpacing = HashMultiset.create();
	private float averangeLineSpacing = 0;
	//private Map<Float, Integer> lastLine = new HashMap<Float, Integer>();
	private Multiset<Float> lastLine = HashMultiset.create();
	private Multiset<Float> fontWeight = HashMultiset.create();
	private float averangeLastLine = 0;
	private float averangeFontSize = 0;
	private float averangeFontWeight = 0;

	public StatisticParser() throws IOException {
	}

	private float prevLineY = -1f;

	@Override
	protected void startPage(PDPage page) throws IOException {
		pages++;
		prevLineY = -1f;
	}

	@Override
	protected void writeLineStart(List<TextPosition> line) {
		if (isLineEmpty(line)) {
			return;
		}
		lines++;

		float lineY = getFirstTrimmed(line).getY();
		if (prevLineY >= 0f) {
			incrementOrAdd(lineSpacing, lineY - prevLineY);
		}
		prevLineY = lineY;

		float start = getFirstTrimmed(line).getX();
		//		leftMargin += start;
		incrementOrAdd(leftMargin, start);
		TextPosition lastTrimmed = getLastTrimmed(line);
		float end = lastTrimmed.getX() + lastTrimmed.getWidth();
		//		rightMargin += end;
		incrementOrAdd(rightMargin, end);

		Float fontSize;
		for (TextPosition t : line) {
			PDFont font = t.getFont();
			if (font != null) {
				PDFontDescriptor fontDescriptor = font.getFontDescriptor();
				incrementOrAdd(fontWeight, fontDescriptor.getFontWeight());
			}
			fontSize = t.getFontSizeInPt();
			if (fontSize > 0) {
				incrementOrAdd(linesFontSize, fontSize);
			}
		}
	}

	@Override
	protected void endPage(PDPage page) throws IOException {
		if (prevLineY >= 0f) {
			incrementOrAdd(lastLine, prevLineY);
		}
	}


	private void incrementOrAdd(Multiset<Float> multiset, float key) {
		multiset.add(key);
	}

	@Override
	public void endDocument(PDDocument pdf) throws IOException {
		averangeLine = lines / pages;
		//		averangeLeftMargin = leftMargin / lines;
		averangeLeftMargin = findMax(leftMargin);
		//		averangeRightMargin = rightMargin / lines;
		averangeRightMargin = findMax(rightMargin);
		averangeFontSize = findMax(linesFontSize);
		averangeLineSpacing = findMax(lineSpacing);
		averangeLastLine = findMax(lastLine);
		averangeFontWeight = findMax(fontWeight);

	}

	private float findMax(Multiset<Float> multiset) {
		float actual = 0f;
		int max = -1;
		for (Float k : multiset) {
			int count = multiset.count(k);
			if (count > max) {
				max = count;
				actual = k;

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

	public float getAverangeLineSpacing() {
		return averangeLineSpacing;
	}

	public float getAverangeLastLine() {
		return averangeLastLine;
	}
	
	public float getAverangeFontWeight() {
		return averangeFontWeight;
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

	public boolean isItalic(PDFontDescriptor descriptor) {
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

	public boolean isBold(PDFontDescriptor descriptor) {
		if (descriptor.getFontWeight() > averangeFontWeight) {
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

	public boolean isItalic(TextPosition text) {
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
		builder.append("Pages=").append(pages);
		builder.append("\nlines=").append(lines);
		//builder.append("\n#fonts=");
		//builder.append(fonts.size());
		//@formatter:off
		builder.append("\naverangeLineSpacing=").append(averangeLineSpacing)
			.append(" #lineSpacing=").append(lineSpacing.size())
			.append('x').append(lineSpacing.count(averangeLineSpacing));
		builder.append("\naverangeLastLine=").append(averangeLastLine)
			.append(" #lastLine=").append(lastLine.size())
			.append('x').append(lastLine.count(averangeLastLine));
		builder.append("\naverangeLine=").append(averangeLine);
		builder.append("\naverangeLeftMargin=").append(averangeLeftMargin)
			.append(", #leftMargin=").append(leftMargin.size())
			.append('x').append(leftMargin.count(averangeLeftMargin));
		builder.append("\naverangeRightMargin=").append(averangeRightMargin)
			.append(" #rightMargin=").append(rightMargin.size())
			.append('x').append(rightMargin.count(averangeRightMargin));
		builder.append("\naverangeFontSize=").append(averangeFontSize)
			.append(" #linesFontSize=").append(linesFontSize.size())
			.append('x').append(linesFontSize.count(averangeFontSize));
		//@formatter:on
		return builder.toString();
	}

}
