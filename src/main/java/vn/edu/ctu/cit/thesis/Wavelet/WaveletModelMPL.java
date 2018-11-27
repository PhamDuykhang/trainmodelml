package vn.edu.ctu.cit.thesis.Wavelet;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class WaveletModelMPL {
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
        int[] layers = new int[] {10, 10, 9,8, 6,6, 4};
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
        double accc= evaluator.evaluate(predictionAndLabels);
    }
}
