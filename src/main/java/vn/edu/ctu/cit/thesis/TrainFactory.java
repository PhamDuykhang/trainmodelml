package vn.edu.ctu.cit.thesis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class TrainFactory {
    public void train(String model_type,String hdfs_url,Dataset<Row> data){
       if(model_type==null){
           System.out.println("Errorr");
           return;
       }
        if(model_type.equalsIgnoreCase("ONEVSALL")){
           TrainingModelIF training = new OneVsAllTrain();
           training.normalize_data(data);
           training.Train(hdfs_url);
        }
        if(model_type.equalsIgnoreCase("MPL")){
            TrainingModelIF training = new MPLTrain();
            training.normalize_data(data);
            training.Train(hdfs_url);
        }
        if(model_type.equalsIgnoreCase("KMEAN")){
            TrainingModelIF training = new KmeansTrain();
            training.normalize_data(data);
            training.Train(hdfs_url);
        }
    }
}
