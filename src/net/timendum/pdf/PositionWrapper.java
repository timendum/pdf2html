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
    Based on org.apache.pdfbox.util.PositionWrapper by Apache Software Foundation
 */
package net.timendum.pdf;

import org.apache.pdfbox.util.TextPosition;

public class PositionWrapper extends org.apache.pdfbox.util.PositionWrapper {
	private boolean isLineStart = false;
	private boolean isParagraphStart = false;
	private boolean isPageBreak = false;
	private boolean isHangingIndent = false;
	private boolean isArticleStart = false;
	private TextPosition position = null;

	@Override
	protected TextPosition getTextPosition() {
		return position;
	}

	@Override
	public boolean isLineStart() {
		return isLineStart;
	}

	@Override
	public void setLineStart() {
		isLineStart = true;
	}

	@Override
	public boolean isParagraphStart() {
		return isParagraphStart;
	}

	@Override
	public void setParagraphStart() {
		isParagraphStart = true;
	}

	public void setLineStart(boolean isLineStart) {
		this.isLineStart = isLineStart;
	}

	public void setParagraphStart(boolean isParagraphStart) {
		this.isParagraphStart = isParagraphStart;
	}

	public void setPageBreak(boolean isPageBreak) {
		this.isPageBreak = isPageBreak;
	}

	public void setHangingIndent(boolean isHangingIndent) {
		this.isHangingIndent = isHangingIndent;
	}

	public void setArticleStart(boolean isArticleStart) {
		this.isArticleStart = isArticleStart;
	}

	@Override
	public boolean isArticleStart() {
		return isArticleStart;
	}

	@Override
	public void setArticleStart() {
		isArticleStart = true;
	}

	@Override
	public boolean isPageBreak() {
		return isPageBreak;
	}

	@Override
	public void setPageBreak() {
		isPageBreak = true;
	}

	@Override
	public boolean isHangingIndent() {
		return isHangingIndent;
	}

	@Override
	public void setHangingIndent() {
		isHangingIndent = true;
	}

	public PositionWrapper(TextPosition position) {
		super(position);
		this.position = position;
	}
}
