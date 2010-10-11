/**
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
package org.apache.hama.bsp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.StringUtils;
import org.apache.hama.Constants;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.util.ClusterUtil;

public class LocalBSPCluster {
  public static final Log LOG = LogFactory.getLog(LocalBSPCluster.class);
  
  private final BSPMaster master;
  private final List<ClusterUtil.GroomServerThread> groomThreads;
  private final Configuration conf;
  private Class<? extends GroomServer> groomServerClass;
  private final static int DEFAULT_NO = 3;

  public LocalBSPCluster(final Configuration conf) throws IOException, InterruptedException {
    this(conf, DEFAULT_NO);
  }

  public LocalBSPCluster(final Configuration conf, final int noGroomServers)
      throws IOException, InterruptedException {
    this(conf, noGroomServers, BSPMaster.class,
        getGroomServerImplementation(conf));
  }

  @SuppressWarnings("unchecked")
  public LocalBSPCluster(final Configuration conf, final int noGroomServers,
      final Class<? extends BSPMaster> masterClass,
      final Class<? extends GroomServer> groomServerClass) throws IOException, InterruptedException {
    conf.set("bsp.master.port", "40000");
    conf.set("bsp.peer.port", "61000");
    conf.set("bsp.local.dir", conf.get("hadoop.tmp.dir") + "/bsp/local");
    conf.set("bsp.system.dir", conf.get("hadoop.tmp.dir") + "/bsp/system");
    this.conf = conf;
    
    // Create the master
    this.master = BSPMaster.constructMaster(masterClass, conf);
    this.groomThreads = new CopyOnWriteArrayList<ClusterUtil.GroomServerThread>();
    this.groomServerClass = (Class<? extends GroomServer>) conf.getClass(
        Constants.GROOM_SERVER_IMPL, groomServerClass);
    for (int i = 0; i < noGroomServers; i++) {
      addGroomServer(i);
    }
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends GroomServer> getGroomServerImplementation(
      final Configuration conf) {
    return (Class<? extends GroomServer>) conf.getClass(
        Constants.GROOM_SERVER_IMPL, GroomServer.class);
  }

  public ClusterUtil.GroomServerThread addGroomServer(final int index)
      throws IOException {
    LOG.info("Adding Groom Server");
    ClusterUtil.GroomServerThread rst = ClusterUtil
        .createGroomServerThread(this.conf, this.groomServerClass, index);
    this.groomThreads.add(rst);
    return rst;
  }

  public void startup() {
    try {
      ClusterUtil.startup(this.master, this.groomThreads, conf);
    } catch (IOException e) {
      LOG.info(e);
    } catch (InterruptedException e) {
      LOG.info(e);
    }
  }

  public void shutdown() {
      ClusterUtil.shutdown(this.master, this.groomThreads, conf);
  }
  
  /**
   * Test things basically work.
   * 
   * @param args
   * @throws IOException
   * @throws InterruptedException 
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    StringUtils.startupShutdownMessage(LocalBSPCluster.class, args, LOG);
    HamaConfiguration conf = new HamaConfiguration();
    LocalBSPCluster cluster = new LocalBSPCluster(conf);
    cluster.startup();
  }
}