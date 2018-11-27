package vn.edu.ctu.cit.thesis;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

public class OnevsRestModel {
    private static final String APP_NAME = "ProcessData";
    private static final String JSON_FILE_PATH = "data/newdataset/*";
    private static final String HDFS_PATH = "hdfs://localhost:9000/data/";
    private static final String MODEL_NAME = "Kmean_model";
    private static final String VERSION = "1.0";

    private static final StructType DicomFileDataSchema = new StructType(new StructField[]{
            new StructField("fileName", DataTypes.StringType, true, Metadata.empty()),
            new StructField("PatientID", DataTypes.StringType, true, Metadata.empty()),
            new StructField("PatientName", DataTypes.StringType, true, Metadata.empty()),
            new StructField("PatientAge", DataTypes.StringType, true, Metadata.empty()),
            new StructField("PatientSex", DataTypes.StringType, true, Metadata.empty()),
            new StructField("InstitutionName", DataTypes.StringType, true, Metadata.empty()),
            new StructField("institutionAddress", DataTypes.StringType, true, Metadata.empty()),
            new StructField("AccessionNumber", DataTypes.StringType, true, Metadata.empty()),
            new StructField("Manufacturer", DataTypes.StringType, true, Metadata.empty()),
            new StructField("Modality", DataTypes.StringType, true, Metadata.empty()),
            new StructField("Area", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("CentroidX", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("CentroidY", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Perimeter", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("DistanceWithSkull", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Diameter", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Solidity", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("BBULX", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("BBULY", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("BBWith", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("BBHeight", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("FilledArea", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Extent", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Eccentricity", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("MajorAxisLength", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("MinorAxisLength", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Orientation", DataTypes.FloatType, true, Metadata.empty()),
            new StructField("Label", DataTypes.IntegerType, true, Metadata.empty())
    });

    public static void main(String[] args) {
        SparkConf sparkconf = new SparkConf()
                .setAppName(APP_NAME)
                .setMaster("local");
        SparkContext sc = new SparkContext(sparkconf);
        SparkSession sparksession = new SparkSession(sc);
        double[] weights = {0.8, 0.2};
        int stop_flag = 0;
//        do {
        Dataset<Row> input_data_raw = sparksession.read()
                .option("mode", "PERMISSIVE")
                .option("Charset", "utf-8")
                .schema(DicomFileDataSchema)
                .json(JSON_FILE_PATH);
        String[] arrayColFeatures = {"Area", "CentroidX", "CentroidY", "Perimeter", "DistanceWithSkull", "Diameter"
                , "Solidity", "BBULX", "BBULY", "BBWith", "BBHeight", "FilledArea", "Extent", "Eccentricity", "MajorAxisLength"
                , "MinorAxisLength", "Orientation"};
        VectorAssembler vector_assembler_chose_feature = new VectorAssembler()
                .setInputCols(arrayColFeatures)
                .setOutputCol("vector_features");
        Dataset<Row> data = vector_assembler_chose_feature.transform(input_data_raw);
        Dataset<Row>[] data_slipt = data.randomSplit(weights);
        Dataset<Row> train_data = data_slipt[0];
        Dataset<Row> test_data = data_slipt[1];
//        NaiveBayes nb = new NaiveBayes().setLabelCol("Label").setFeaturesCol("vector_features");
//        NaiveBayesModel model = nb.fit(train_data);
//        Dataset<Row> predictions = model.transform(test_data);
//        predictions.show();
        LogisticRegression classifier = new LogisticRegression()
                .setMaxIter(10)
                .setTol(1E-6)
                .setLabelCol("Label")
                .setFeaturesCol("vector_features")
                .setFitIntercept(true);
        OneVsRest ovr = new OneVsRest().setClassifier(classifier).setFeaturesCol("vector_features")
                .setLabelCol("Label");
        OneVsRestModel ovrModel = ovr.fit(train_data);
        Dataset<Row> predictions = ovrModel.transform(test_data)
                .select("prediction", "Label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator().setLabelCol("Label")
                .setMetricName("accuracy");
        double accuracy = evaluator.evaluate(predictions);
        System.out.println("Test Error = " + (1 - accuracy));
//            if (accuracy >= 0.95) {
//                try {
//                    ovrModel.save("data/newonevsall" + String.format("%.4f", accuracy));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                stop_flag= 1;
//            }
//        }while(stop_flag == 0);

    }
}
