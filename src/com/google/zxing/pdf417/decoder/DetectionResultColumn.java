/*
 * Copyright 2013 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.decoder;

/**
 * @author Guenther Grau
 */
class DetectionResultColumn {

	private static final int MAX_NEARBY_DISTANCE = 5;

	private final BoundingBox boundingBox;
	private final Codeword[] codewords;

	DetectionResultColumn(BoundingBox boundingBox) {
		this.boundingBox = new BoundingBox(boundingBox);
		codewords = new Codeword[boundingBox.getMaxY() - boundingBox.getMinY() + 1];
	}

	final Codeword getCodewordNearby(int imageRow) {
		Codeword codeword = getCodeword(imageRow);
		if (codeword != null) {
			return codeword;
		}
		for (int i = 1; i < MAX_NEARBY_DISTANCE; i++) {
			int nearImageRow = imageRowToCodewordIndex(imageRow) - i;
			if (nearImageRow >= 0) {
				codeword = codewords[nearImageRow];
				if (codeword != null) {
					return codeword;
				}
			}
			nearImageRow = imageRowToCodewordIndex(imageRow) + i;
			if (nearImageRow < codewords.length) {
				codeword = codewords[nearImageRow];
				if (codeword != null) {
					return codeword;
				}
			}
		}
		return null;
	}

	final int imageRowToCodewordIndex(int imageRow) {
		return imageRow - boundingBox.getMinY();
	}

	final void setCodeword(int imageRow, Codeword codeword) {
		codewords[imageRowToCodewordIndex(imageRow)] = codeword;
	}

	final Codeword getCodeword(int imageRow) {
		return codewords[imageRowToCodewordIndex(imageRow)];
	}

	final BoundingBox getBoundingBox() {
		return boundingBox;
	}

	final Codeword[] getCodewords() {
		return codewords;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int row = 0;
		for (Codeword codeword : codewords) {
			if (codeword == null) {
				// formatter.format("%3d: | %n", row++);

				sb.append(row >= 1000 ? "" + row : row >= 100 ? " " + row : row >= 10 ? "  " + row : "   " + row);
				sb.append(":    |   \n");
				row++;
				continue;
			}
			int i = codeword.getRowNumber();
			int j = codeword.getValue();
			sb.append(row >= 1000 ? "" + row : row >= 100 ? " " + row : row >= 10 ? "  " + row : "   " + row);
			sb.append(": ");
			sb.append(i >= 1000 ? "" + i : i >= 100 ? " " + i : i >= 10 ? "  " + i : "   " + i);
			sb.append("|");
			sb.append(j >= 1000 ? "" + j : j >= 100 ? " " + j : j >= 10 ? "  " + j : "   " + j);
			sb.append("\n");
			row++;
			// formatter.format("%3d: %3d|%3d%n", row++, codeword.getRowNumber(),
			// codeword.getValue());
		}
		return sb.toString();
	}

}
