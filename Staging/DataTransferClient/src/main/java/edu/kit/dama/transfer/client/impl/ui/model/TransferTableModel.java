/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.transfer.client.impl.ui.model;

import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.staging.interfaces.ITransferStatus;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jejkal
 */
public class TransferTableModel extends AbstractTableModel {

  private String[] columnNames = new String[]{
    "Status", "TransferId", "Progress", ""
  };
  Class[] types = new Class[]{
    ITransferStatus.class, String.class, Float.class, String.class
  };
  private List<ITransferInformation> tasks = new LinkedList<ITransferInformation>();

  public void addRow(ITransferInformation pTask) {
    tasks.add(pTask);
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return tasks.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return types[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 3;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    ITransferInformation task = tasks.get(rowIndex);

    switch (columnIndex) {
      case 0:
        return task.getStatusEnum();
      case 1:
        return task.getTransferId();
      case 2:
        return 0f;
      default:
        return "Actions";
    }

  }
}
