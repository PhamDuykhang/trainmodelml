package vn.edu.ctu.cit.thesis.TrainHU;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
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

public class MultilayerClass {
    private static final String APP_NAME = "ProcessData";
    private static final String JSON_FILE_PATH = "data/newdataset/*";
    private static final String HDFS_PATH = "hdfs://localhost:9000/data/";
    private static final String MODEL_NAME = "Kmean_model";
    private static final String VERSION = "1.0";

    public static void main(String[] args) {
        SparkConf sparkconf = new SparkConf()
                .setAppName(APP_NAME)
                .setMaster("local");
        SparkContext sc = new SparkContext(sparkconf);
        SparkSession sparksession = new SparkSession(sc);
        StructType DicomFileDataSchema = new StructType(new StructField[]{
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
        double[] weights = {0.8, 0.2};
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
        int[] layers = new int[]{17, 10, 7, 5, 4};
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(128)
                .setSeed(1234L)
                .setLabelCol("Label")
                .setFeaturesCol("vector_features")
                .setMaxIter(10000);
        MultilayerPerceptronClassificationModel model = trainer.fit(train_data);
        Dataset<Row> result = model.transform(test_data);
        Dataset<Row> predictionAndLabels = result.select("prediction", "Label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setMetricName("accuracy").setLabelCol("Label");
        System.out.println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels));
        double accc = evaluator.evaluate(predictionAndLabels);
        if (accc >= 0.9) {
            try {
                model.save("data/mlp_" + String.format("%.3f", accc));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
