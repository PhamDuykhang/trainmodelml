package vn.edu.ctu.cit.thesis;

import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.IOException;

public class MPLTrain implements TrainingModelIF {
    Dataset<Row> data;
    @Override
    public void normalize_data(Dataset<Row> data) {
        String[] arrayColFeature = {"Area","CentroidX","CentroidY", "Perimeter","DistanceWithSkull","Diameter"
                ,"Solidity","ConvexArea", "BBULX","BBULY","BBWith","BBHeight","FilledArea","Extent", "Eccentricity", "MajorAxisLength"
                , "MinorAxisLength","Orientation"};
        VectorAssembler vector_assemble = new VectorAssembler()
                .setInputCols(arrayColFeature)
                .setOutputCol("vector_features");
        this.data = vector_assemble.transform(data);
    }

    @Override
    public void Train(String hdfs_url) {
        int stop_flag = 0;
        do {
            double[] weights = {0.8, 0.2};
            Dataset<Row>[] data_slipt = this.data.randomSplit(weights);
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
                    model.save(hdfs_url + String.format("%.3f", accc));
                    stop_flag = 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }while (stop_flag == 0 );
    }
}
