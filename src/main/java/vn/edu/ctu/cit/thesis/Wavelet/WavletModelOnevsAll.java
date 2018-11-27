package vn.edu.ctu.cit.thesis.Wavelet;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.ml.classification.OneVsRestModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;

public class WavletModelOnevsAll {
    private static final String APP_NAME = "ProcessData";
    private static final String JSON_FILE_PATH = "data/jpg/*";
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
        System.out.println("Acc= "+accuracy*100);
        System.out.println("Test Error = " + (1 - accuracy));

//        try {
//            ovrModel.save("data/Wavelet/onevsall");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
