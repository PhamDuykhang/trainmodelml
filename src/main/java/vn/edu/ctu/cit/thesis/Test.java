package vn.edu.ctu.cit.thesis;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Test {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Train").setMaster("local");
        SparkSession spark = SparkSession.builder()
                .config(conf)
                .getOrCreate();
        Dataset<Row> data = spark.read().load("hdfs://localhost:9000/data/braindataset");
        data.printSchema();
        data.show();
        data.groupBy("Label").count().show();
    }
}
