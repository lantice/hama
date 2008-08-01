package org.apache.hama.mapred;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hama.Constants;
import org.apache.hama.Matrix;
import org.apache.hama.io.VectorDatum;

@SuppressWarnings("unchecked")
public abstract class MatrixMap<K extends WritableComparable, V extends Writable>
    extends MapReduceBase implements
    Mapper<ImmutableBytesWritable, VectorDatum, K, V> {
  protected static Matrix B;

  public static void initJob(String matrixA, String matrixB,
      Class<? extends MatrixMap> mapper,
      Class<? extends WritableComparable> outputKeyClass,
      Class<? extends Writable> outputValueClass, JobConf job) {

    job.setInputFormat(MatrixInputFormat.class);
    job.setMapOutputValueClass(outputValueClass);
    job.setMapOutputKeyClass(outputKeyClass);
    job.setMapperClass(mapper);
    FileInputFormat.addInputPaths(job, matrixA);

    B = new Matrix(new HBaseConfiguration(), new Text(matrixB));
    job.set(MatrixInputFormat.COLUMN_LIST, Constants.COLUMN);
  }

  public abstract void map(ImmutableBytesWritable key, VectorDatum value,
      OutputCollector<K, V> output, Reporter reporter) throws IOException;
}
