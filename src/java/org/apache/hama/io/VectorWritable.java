/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hama.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.HbaseMapWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Writables;
import org.apache.hadoop.io.Writable;
import org.apache.hama.Vector;
import org.apache.hama.util.Numeric;

public class VectorWritable implements Writable, Map<byte[], Cell> {

  public byte[] row;
  public HbaseMapWritable<byte[], Cell> cells;

  public Cell put(byte[] key, Cell value) {
    throw new UnsupportedOperationException("VectorDatum is read-only!");
  }

  public Cell get(Object key) {
    return this.cells.get(key);
  }

  public Cell remove(Object key) {
    throw new UnsupportedOperationException("VectorDatum is read-only!");
  }

  public boolean containsKey(Object key) {
    return cells.containsKey(key);
  }

  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("Don't support containsValue!");
  }

  public boolean isEmpty() {
    return cells.isEmpty();
  }

  public void clear() {
    throw new UnsupportedOperationException("VectorDatum is read-only!");
  }

  public Set<byte[]> keySet() {
    Set<byte[]> result = new TreeSet<byte[]>(Bytes.BYTES_COMPARATOR);
    for (byte[] w : cells.keySet()) {
      result.add(w);
    }
    return result;
  }

  public Set<Map.Entry<byte[], Cell>> entrySet() {
    return Collections.unmodifiableSet(this.cells.entrySet());
  }

  public Collection<Cell> values() {
    ArrayList<Cell> result = new ArrayList<Cell>();
    for (Writable w : cells.values()) {
      result.add((Cell) w);
    }
    return result;
  }

  public void readFields(final DataInput in) throws IOException {
    this.row = Bytes.readByteArray(in);
    this.cells.readFields(in);
  }

  public void write(final DataOutput out) throws IOException {
    Bytes.writeByteArray(out, this.row);
    this.cells.write(out);
  }

  public VectorWritable addition(byte[] bs, Vector v2) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public void putAll(Map<? extends byte[], ? extends Cell> m) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  /**
   * Get the Cell that corresponds to column
   */
  public Cell get(byte[] column) {
    return this.cells.get(column);
  }

  /**
   * Get the Cell that corresponds to column, using a String key
   */
  public Cell get(String key) {
    return get(Bytes.toBytes(key));
  }

  /**
   * Get the double value without timestamp
   */
  public double get(int key) {
    return Numeric.bytesToDouble(get(Numeric.intToBytes(key)).getValue());
  }

  public int size() {
    return this.cells.size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("row=");
    sb.append(Bytes.toString(this.row));
    sb.append(", cells={");
    boolean moreThanOne = false;
    for (Map.Entry<byte[], Cell> e : this.cells.entrySet()) {
      if (moreThanOne) {
        sb.append(", ");
      } else {
        moreThanOne = true;
      }
      sb.append("(column=");
      sb.append(Bytes.toString(e.getKey()));
      sb.append(", timestamp=");
      sb.append(Long.toString(e.getValue().getTimestamp()));
      sb.append(", value=");
      byte[] value = e.getValue().getValue();
      if (Bytes.equals(e.getKey(), HConstants.COL_REGIONINFO)) {
        try {
          sb.append(Writables.getHRegionInfo(value).toString());
        } catch (IOException ioe) {
          sb.append(ioe.toString());
        }
      } else {
        sb.append(value);
      }
      sb.append(")");
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * 
   * The inner class for an entry of row.
   * 
   */
  public class Entries implements Map.Entry<byte[], Cell> {

    private final byte[] column;
    private final Cell cell;

    Entries(byte[] column, Cell cell) {
      this.column = column;
      this.cell = cell;
    }

    public Cell setValue(Cell c) {
      throw new UnsupportedOperationException("VectorDatum is read-only!");
    }

    public byte[] getKey() {
      return column;
    }

    public Cell getValue() {
      return cell;
    }

  }

}