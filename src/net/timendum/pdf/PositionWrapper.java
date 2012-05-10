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
