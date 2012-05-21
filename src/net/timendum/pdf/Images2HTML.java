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
    Based on org.apache.pdfbox.examples.util.PrintImageLocations by Apache Software Foundation
 */
package net.timendum.pdf;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.timendum.pdf.beans.Image;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Images2HTML extends PDFStreamEngine {

	private static final String INVOKE_OPERATOR = "Do";

	public Images2HTML() throws IOException {
		super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PDFTextStripper.properties", true));
	}

	public void processDocument(PDDocument document) throws Exception {
		List allPages = document.getDocumentCatalog().getAllPages();
		for (int i = 0; i < allPages.size(); i++) {
			PDPage page = (PDPage) allPages.get(i);
			processStream(page, page.findResources(), page.getContents().getStream());
		}
	}


	private Table<PDPage, Float, Image> images = HashBasedTable.create();
	private boolean lazyImages = false;
	private File basePath = null;
	private String prefix = "image";

	public void setLazyImages(boolean lazyImages) {
		this.lazyImages = lazyImages;
	}

	public void setBasePath(File basePath) {
		this.basePath = basePath;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected void processOperator(PDFOperator operator, List arguments) throws IOException {
		String operation = operator.getOperation();
		if (INVOKE_OPERATOR.equals(operation)) {
			COSName objectName = (COSName) arguments.get(0);
			Map<String, PDXObject> xobjects = getResources().getXObjects();
			PDXObject xobject = xobjects.get(objectName.getName());
			if (xobject instanceof PDXObjectImage) {
				PDXObjectImage image = (PDXObjectImage) xobject;
				PDPage page = getCurrentPage();
				int imageWidth = image.getWidth();
				int imageHeight = image.getHeight();
				double pageHeight = page.getMediaBox().getHeight();

				Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
				float yScaling = ctmNew.getYScale();
				float angle = (float) Math.acos(ctmNew.getValue(0, 0) / ctmNew.getXScale());
				if (ctmNew.getValue(0, 1) < 0 && ctmNew.getValue(1, 0) > 0) {
					angle = (-1) * angle;
				}
				ctmNew.setValue(2, 1, (float) (pageHeight - ctmNew.getYPosition() - Math.cos(angle) * yScaling));
				ctmNew.setValue(2, 0, (float) (ctmNew.getXPosition() - Math.sin(angle) * yScaling));
				// because of the moved 0,0-reference, we have to shear in the opposite direction
				ctmNew.setValue(0, 1, (-1) * ctmNew.getValue(0, 1));
				ctmNew.setValue(1, 0, (-1) * ctmNew.getValue(1, 0));
				AffineTransform ctmAT = ctmNew.createAffineTransform();
				ctmAT.scale(1f / imageWidth, 1f / imageHeight);

				Image entry = new Image();
				entry.x = ctmNew.getXPosition();
				entry.image = image;
				entry.name = objectName.getName();
				images.put(page, ctmNew.getYPosition(), entry);

			} else if (xobject instanceof PDXObjectForm) {
				// save the graphics state
				getGraphicsStack().push((PDGraphicsState) getGraphicsState().clone());
				PDPage page = getCurrentPage();

				PDXObjectForm form = (PDXObjectForm) xobject;
				COSStream invoke = (COSStream) form.getCOSObject();
				PDResources pdResources = form.getResources();
				if (pdResources == null) {
					pdResources = page.findResources();
				}
				// if there is an optional form matrix, we have to
				// map the form space to the user space
				Matrix matrix = form.getMatrix();
				if (matrix != null) {
					Matrix xobjectCTM = matrix.multiply(getGraphicsState().getCurrentTransformationMatrix());
					getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
				}
				processSubStream(page, pdResources, invoke);

				// restore the graphics state
				setGraphicsState(getGraphicsStack().pop());
			}

		} else {
			super.processOperator(operator, arguments);
		}
	}

	public Table<PDPage, Float, Image> getImages() {
		return images;
	}

	protected Map<String, String> imageCache = new HashMap<String, String>();

	public String printImage(Image image) throws IOException {
		PDXObjectImage pdfImage = image.image;
		String key = image.name;

		if (lazyImages) {
			String name = imageCache.get(key);
			if (name != null) {
				return name;
			}
		}

		String suffix = pdfImage.getSuffix();
		String name = getUniqueFileName(prefix + "_" + key, suffix);
		if (!"jpg".equals(suffix) && !"png".equals(suffix)) {
			System.err.println("Image format: " + suffix);
		}
		pdfImage.write2file(basePath.getAbsolutePath() + File.separator + name);
		String path = name + "." + suffix;

		if (lazyImages) {
			imageCache.put(key, path);
		}
		return path;
	}

	private int imageCounter = 1;

	protected String getUniqueFileName(String prefix, String suffix) {
		String uniqueName = null;
		File f = null;
		while (f == null || f.exists()) {
			uniqueName = prefix + "-" + imageCounter;
			f = new File(basePath, uniqueName + "." + suffix);
			imageCounter++;
		}
		return uniqueName;
	}

}
