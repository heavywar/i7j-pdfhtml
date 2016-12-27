/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.html2pdf.attach.util;


import java.util.ArrayList;

public class RowColHelper {
    private ArrayList<Integer> lastEmptyRow = new ArrayList<>();
    private int currRow = -1;
    private int currCol = 0;

    public void newRow() {
        ++currRow;
        currCol = 0;
    }

    public void updateCurrentPosition(int colspan, int rowspan) {
        ensureRowIsStarted();
        while (lastEmptyRow.size() < currCol) {
            lastEmptyRow.add(currRow);
        }
        int value = currRow + rowspan;
        int end = currCol + colspan;
        int middle = Math.min(lastEmptyRow.size(), end);
        for (int i = currCol; i < middle; ++i) {
            lastEmptyRow.set(i, Math.max(value, lastEmptyRow.get(i)));
        }
        while (lastEmptyRow.size() < end) {
            lastEmptyRow.add(value);
        }
        currCol = end;
    }

    public int moveToNextEmptyCol() {
        ensureRowIsStarted();
        while (!canPutCell(currCol)) {
            ++currCol;
        }
        return currCol;
    }

    public boolean canPutCell(int col) {
        ensureRowIsStarted();
        if (col >= lastEmptyRow.size()) {
            return true;
        } else {
            return lastEmptyRow.get(col) <= currRow;
        }
    }

    private void ensureRowIsStarted() {
        if (currRow == -1) {
            newRow();
        }
    }
}