package vn.edu.ctu.cit.thesis;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class TrainModel {
    public static void main(String[] args) {
        String model_Type = args[0];
        String hdfs_url  = args[1]+"/"+args[0];
        String hdfs_data_set = args[2];
        SparkConf conf = new SparkConf().setAppName("Train");
        SparkSession spark = SparkSession.builder()
                .config(conf)
                .getOrCreate();
        Dataset<Row> data = spark.read().load(hdfs_data_set);
        data.printSchema();
        TrainFactory trainingfactory = new TrainFactory();
        trainingfactory.train(model_Type,hdfs_url,data);
        System.out.println("Hoanh thanh!!!!!!!!");
    }
}

