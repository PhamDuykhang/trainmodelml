package vn.edu.ctu.cit.thesis;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReciveData {
    public static final String APP_NAME="Dicom process";
    public static void main(String[] args) throws InterruptedException {
        String data_patch = args[0];
        String kafka_host_list = args[1];
        String kafka_topic= args[2];
        int durantion = Integer.valueOf(args[3]);
        SparkConf sparkConf = new SparkConf().setAppName(APP_NAME);
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(sparkConf,Durations.seconds(durantion));
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers",kafka_host_list);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "dicomprocess");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        Collection<String> topics = Arrays.asList(kafka_topic.split(","));
        JavaInputDStream<ConsumerRecord<String,String>> stream =KafkaUtils.createDirectStream(
                javaStreamingContext
                ,LocationStrategies.PreferConsistent()
                ,ConsumerStrategies.<String,String>Subscribe(topics,kafkaParams));
        JavaDStream<String> dStream=stream.map(line->line.value());
        dStream.foreachRDD(rdd->{
            SparkSession spark_session = JavaSparkSessionSingleton.getInstance(rdd.context().getConf());
            if (!rdd.isEmpty()){
                Dataset<Row> df = spark_session.read().schema(StrucTypeUtil.getStructype()).json(rdd);
                df.write().mode(SaveMode.Append).save(data_patch);
                System.out.println("Saved data!!!!!!!!");
            }
            System.out.println("No data!!!!!!!!");
        });
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }

}
