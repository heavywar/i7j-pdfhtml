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
package com.itextpdf.html2pdf.attach.wrapelement;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.util.ArrayList;
import java.util.List;

public class TableWrapper implements IWrapElement {

    private List<List<Cell> > rows;
    private List<List<Cell> > headerRows;
    private List<List<Cell> > footerRows;

    public int getRowsSize() {
        return rows.size();
    }

    public void newRow() {
        if (rows == null) {
            rows = new ArrayList<>();
        }
        rows.add(new ArrayList<Cell>());
    }

    public void newHeaderRow() {
        if (headerRows == null) {
            headerRows = new ArrayList<>();
        }
        headerRows.add(new ArrayList<Cell>());
    }

    public void newFooterRow() {
        if (footerRows == null) {
            footerRows = new ArrayList<>();
        }
        footerRows.add(new ArrayList<Cell>());
    }

    public void addHeaderCell(Cell cell) {
        if (headerRows == null) {
            headerRows = new ArrayList<>();
        }
        if (headerRows.size() == 0) {
            newHeaderRow();
        }
        headerRows.get(headerRows.size() - 1).add(cell);
    }

    public void addFooterCell(Cell cell) {
        if (footerRows == null) {
            footerRows = new ArrayList<>();
        }
        if (footerRows.size() == 0) {
            newFooterRow();
        }
        footerRows.get(footerRows.size() - 1).add(cell);
    }

    public void addCell(Cell cell) {
        if (rows == null) {
            rows = new ArrayList<>();
        }
        if (rows.size() == 0) {
            newRow();
        }
        rows.get(rows.size() - 1).add(cell);
    }

    public Table toTable() {
        UnitValue[] widths = recalculateWidths();
        Table table;
        if (widths.length > 0) {
            table = new Table(widths);
        } else {
            // if table is empty, create empty table with single column
            table = new Table(1);
        }
        if (headerRows != null) {
            for (List<Cell> headerRow : headerRows) {
                for (Cell headerCell : headerRow) {
                    table.addHeaderCell((Cell) headerCell);
                }
            }
        }
        if (footerRows != null) {
            for (List<Cell> footerRow : footerRows) {
                for (Cell footerCell : footerRow) {
                    table.addFooterCell((Cell) footerCell);
                }
            }
        }
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                for (int j = 0; j < rows.get(i).size(); j++) {
                    table.addCell((Cell) (rows.get(i).get(j)));
                }
                if (i != rows.size() - 1) {
                    table.startNewRow();
                }
            }
        }

        return table;
    }

    private UnitValue[] recalculateWidths() {
        List<UnitValue> maxWidths = new ArrayList<>();
        if (rows != null) {
            calculateMaxWidths(rows, maxWidths);
        }
        if (headerRows != null) {
            calculateMaxWidths(headerRows, maxWidths);
        }
        if (footerRows != null) {
            calculateMaxWidths(footerRows, maxWidths);
        }

        UnitValue[] arr = new UnitValue[maxWidths.size()];
        int nullWidth = 0;
        float totalPercentSum = 0;
        for (UnitValue width : maxWidths) {
            if (width == null) {
                nullWidth++;
            } else if (width.isPercentValue()) {
                totalPercentSum += width.getValue();
            }
        }
        if (totalPercentSum >= 100 && nullWidth != 0 && nullWidth < maxWidths.size()) {
            // TODO In this case, the rest of the column should be assigned to min-width. This is currently unsupported,
            // so we fall back to just division of the available place uniformly.
            for (int i = 0; i < maxWidths.size(); i++) {
                arr[i] = UnitValue.createPercentValue(100 / maxWidths.size());
            }
        } else {
            for (int k = 0; k < maxWidths.size(); k++) {
                UnitValue width = maxWidths.get(k);
                if (width == null && nullWidth > 0) {
                    width = UnitValue.createPercentValue((100 - totalPercentSum) / nullWidth);
                }
                arr[k] = width;
            }
        }
        return arr;
    }

    private void calculateMaxWidths(List<List<Cell>> rows, List<UnitValue> maxWidths) {
        int maxRowSize = 1;
        for (List<Cell> row : rows) {
            maxRowSize = Math.max(maxRowSize, row.size());
            int colspanSum = 0;
            for (int j = 0; j < row.size(); j++) {
                if (maxWidths.size() <= j + colspanSum) {
                    Cell cell = row.get(j);
                    UnitValue width = cell.getWidth();
                    if (cell.getColspan() > 1) {
                        for (int i = 0; i < cell.getColspan(); i++) {
                            if (width == null) {
                                maxWidths.add(null);
                            } else {
                                maxWidths.add(new UnitValue(width.getUnitType(), width.getValue() / cell.getColspan()));
                            }
                            colspanSum++;
                        }
                    } else {
                        if (width == null) {
                            maxWidths.add(null);
                        } else {
                            maxWidths.add(cell.getWidth());
                        }
                    }
                } else {
                    UnitValue maxWidth = maxWidths.get(j);
                    UnitValue width = row.get(j).getWidth();
                    if (width != null && (maxWidth == null || width.getValue() > maxWidth.getValue())) {
                        maxWidths.set(j, width);
                    }
                }
            }
        }
    }
}
