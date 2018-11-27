package vn.edu.ctu.cit.thesis.Wavelet;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionTrainingSummary;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class waveletLogics {
    private static final String APP_NAME = "ProcessData";
    private static final String JSON_FILE_PATH = "data/500hu/*";

    public static void main(String[] args) {
        SparkConf sparkconf = new SparkConf()
                .setAppName(APP_NAME)
                .setMaster("local");
        SparkContext sc = new SparkContext(sparkconf);
        SparkSession sparksession = new SparkSession(sc);
        double[] weights = {0.8,0.2};
        Dataset<Row> input_data_raw = sparksession.read()
                .option("mode","PERMISSIVE")
                .json(JSON_FILE_PATH);
        input_data_raw.printSchema();
        input_data_raw.show();
        System.out.println(input_data_raw.count());
        String[] arrayColFeatures = {"ContrastH2","ContrastV2","CorrelationH2", "CorrelationV2","EnergyH2","EnergyV2"
                ,"EntropyH2", "EntropyV2","HomogeneityH2","HomogeneityV2"};
        VectorAssembler vector_assembler_chose_feature = new VectorAssembler()
                .setInputCols(arrayColFeatures)
                .setOutputCol("vector_features");
        Dataset<Row> data = vector_assembler_chose_feature.transform(input_data_raw);
        Dataset<Row>[] data_slipt=data.randomSplit(weights);
        Dataset<Row> train_data = data_slipt[0];
        Dataset<Row> test_data = data_slipt[1];
        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(100000)
                .setRegParam(0.3)
                .setElasticNetParam(0.8)
                .setFeaturesCol("vector_features")
                .setLabelCol("Label")
                .setPredictionCol("reuslt");
        LogisticRegressionModel lrModel =lr.fit(train_data);
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
    }
}
