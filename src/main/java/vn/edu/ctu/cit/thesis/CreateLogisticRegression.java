package vn.edu.ctu.cit.thesis;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionTrainingSummary;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

public class CreateLogisticRegression {
    private static final String APP_NAME = "ProcessData";
    private static final String JSON_FILE_PATH = "data/stable/*";
    private static final String HDFS_PATH = "hdfs://localhost:9000/data/";
    private static final String MODEL_NAME = "Kmean_model";
    private static final String VERSION = "1.0";

    public static void main(String[] args) throws IOException {
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

        System.out.println("Totall: " + input_data_raw.count());
        int stop_flag = 0;

        Dataset<Row>[] data_slipt = data.randomSplit(weights);
        Dataset<Row> train_data = data_slipt[0];
        Dataset<Row> test_data = data_slipt[1];
        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(100000)
                .setRegParam(0)
                .setElasticNetParam(0)
                .setFeaturesCol("vector_features")
                .setLabelCol("Label")
                .setPredictionCol("reuslt");
        LogisticRegressionModel lrModel = lr.fit(train_data);
        System.out.println("Coefficients: \n"
                + lrModel.coefficientMatrix() + " \nIntercept: " + lrModel.interceptVector());
        LogisticRegressionTrainingSummary trainingSummary = lrModel.summary();
        double[] objectiveHistory = trainingSummary.objectiveHistory();
        for (double lossPerIteration : objectiveHistory) {
            System.out.println(lossPerIteration);
        }

        System.out.println("False positive rate by label:");
        int i = 0;
        double[] fprLabel = trainingSummary.falsePositiveRateByLabel();
        for (double fpr : fprLabel) {
            System.out.println("label " + i + ": " + fpr);
            i++;
        }

        System.out.println("True positive rate by label:");
        i = 0;
        double[] tprLabel = trainingSummary.truePositiveRateByLabel();
        for (double tpr : tprLabel) {
            System.out.println("label " + i + ": " + tpr);
            i++;
        }

        System.out.println("Precision by label:");
        i = 0;
        double[] precLabel = trainingSummary.precisionByLabel();
        for (double prec : precLabel) {
            System.out.println("label " + i + ": " + prec);
            i++;
        }

        System.out.println("Recall by label:");
        i = 0;
        double[] recLabel = trainingSummary.recallByLabel();
        for (double rec : recLabel) {
            System.out.println("label " + i + ": " + rec);
            i++;
        }

        System.out.println("F-measure by label:");
        i = 0;
        double[] fLabel = trainingSummary.fMeasureByLabel();
        for (double f : fLabel) {
            System.out.println("label " + i + ": " + f);
            i++;
        }

        double accuracy = trainingSummary.accuracy();
        double falsePositiveRate = trainingSummary.weightedFalsePositiveRate();
        double truePositiveRate = trainingSummary.weightedTruePositiveRate();
        double fMeasure = trainingSummary.weightedFMeasure();
        double precision = trainingSummary.weightedPrecision();
        double recall = trainingSummary.weightedRecall();
        System.out.println("Accuracy: " + accuracy);
        System.out.println("FPR: " + falsePositiveRate);
        System.out.println("TPR: " + truePositiveRate);
        System.out.println("F-measure: " + fMeasure);
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("Reg" + lr.getRegParam());
        System.out.println("EL" + lr.getElasticNetParam());
        lrModel.save("data/lr" + String.format("%.3f", accuracy));

    }
}
