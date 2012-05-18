package net.timendum.pdf;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.examples.util.PrintImageLocations;
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

public class Images2HTML extends PDFStreamEngine {

	private static final String INVOKE_OPERATOR = "Do";

	public Images2HTML() throws IOException {
		super(ResourceLoader.loadProperties("org/apache/pdfbox/resources/PDFTextStripper.properties", true));
	}

	public void processDocument(PDDocument document) throws Exception {
		PrintImageLocations printer = new PrintImageLocations();
		List allPages = document.getDocumentCatalog().getAllPages();
		for (int i = 0; i < allPages.size(); i++) {
			PDPage page = (PDPage) allPages.get(i);
			printer.processStream(page, page.findResources(), page.getContents().getStream());
		}
	}

	
	
	private Map<PDPage, Object> images;
	
	/**
	 * This is used to handle an operation.
	 *
	 * @param operator The operation to perform.
	 * @param arguments The list of arguments.
	 *
	 * @throws IOException If there is an error processing the operation.
	 */
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
				System.out.println("*******************************************************************");
				System.out.println("Found image [" + objectName.getName() + "]");

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

				System.out.println("position = " + ctmNew.getXPosition() + ", " + ctmNew.getYPosition());
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

}
