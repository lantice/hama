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
package org.apache.hama;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hama.io.VectorEntry;
import org.apache.log4j.Logger;

/**
 * Matrix test
 */
public class TestDenseMatrix extends HamaTestCase {
  static final Logger LOG = Logger.getLogger(TestDenseMatrix.class);
  
  private Matrix m1;
  private Matrix m2;
  
  public void setUp() throws Exception{
    super.setUp();
    m1 = DenseMatrix.random(conf, SIZE, SIZE);
    m2 = DenseMatrix.random(conf, SIZE, SIZE);
  }
  
  /**
   * Column vector test.
   * 
   * @param rand
   * @throws IOException
   */
  public void testGetColumn() throws IOException {
    Vector v = m1.getColumn(0);
    Iterator<VectorEntry> it = v.iterator();
    int x = 0;
    while (it.hasNext()) {
      assertEquals(m1.get(x, 0), it.next().getValue());
      x++;
    }
  }

  /**
   * Test matrices addition
   */
  public void testMatrixAdd() {
    Matrix result = m1.add(m2);

    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        assertEquals(result.get(i, j), m1.get(i, j) + m2.get(i, j));
      }
    }
  }

  /**
   * Test matrices multiplication
   */
  public void testMatrixMult() {
    Matrix result = m1.mult(m2);

    verifyMultResult(SIZE, m1, m2, result);
  }

  /**
   * Verifying multiplication result
   * 
   * @param size
   * @param m1
   * @param m2
   * @param result
   */
  private void verifyMultResult(int size, Matrix m1, Matrix m2, Matrix result) {
    double[][] C = new double[SIZE][SIZE];

    for (int i = 0; i < SIZE; i++)
      for (int j = 0; j < SIZE; j++)
        for (int k = 0; k < SIZE; k++)
          C[i][k] += m1.get(i, j) * m2.get(j, k);

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        LOG.info("result: " + result.get(i, j) + ", C: " + C[i][j]);
        assertEquals(result.get(i, j), C[i][j]);
      }
    }
  }
}
