package vn.edu.ctu.cit.thesis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface TrainingModelIF {
    public void normalize_data(Dataset<Row> data);
    public void Train(String hdfs_url);
}
