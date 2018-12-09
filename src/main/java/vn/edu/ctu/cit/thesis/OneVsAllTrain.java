package vn.edu.ctu.cit.thesis;

import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.ml.classification.OneVsRestModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.IOException;

public class OneVsAllTrain implements TrainingModelIF {
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
            LogisticRegression classifier = new LogisticRegression()
                    .setMaxIter(10)
                    .setTol(1E-6)
                    .setLabelCol("Label")
                    .setFeaturesCol("vector_features")
                    .setFitIntercept(true);
            OneVsRest ovr = new OneVsRest().setClassifier(classifier).setFeaturesCol("vector_features")
                    .setLabelCol("Label");
            double[] weights = {0.8, 0.2};
            Dataset<Row>[] data_slipt = this.data.randomSplit(weights);
            Dataset<Row> train = data_slipt[0];
            Dataset<Row> test = data_slipt[1];
            System.out.println("Trainingggggg !!!!!!!!!!!!!!");
            System.out.println("--------------------------------");
            OneVsRestModel ovrModel = ovr.fit(this.data);
            Dataset<Row> predictions = ovrModel.transform(test)
                    .select("prediction", "Label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator().setLabelCol("Label")
                    .setMetricName("accuracy");
            double accuracy = evaluator.evaluate(predictions);
            if (accuracy >= 0.95) {
                try {
                    System.out.println("Saving model");
                    ovrModel.save(hdfs_url + String.format("%.4f", accuracy));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(accuracy>=0.98){
                stop_flag= 1;
            }
        } while(stop_flag == 0);
    }
}
