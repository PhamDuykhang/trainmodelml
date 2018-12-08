package vn.edu.ctu.cit.thesis;

import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.evaluation.ClusteringEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.IOException;

public class KmeansTrain implements TrainingModelIF {
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
        double[] weights = {0.8, 0.2};
        Dataset<Row>[] data_slipt = this.data.randomSplit(weights);
        Dataset<Row> train_data = data_slipt[0];
        Dataset<Row> test_data = data_slipt[1];
        KMeans kMeans = new KMeans()
                .setK(4)
                .setFeaturesCol("vector_features")
                .setPredictionCol("Cluster");
        KMeansModel kmean_model = kMeans.fit(train_data);
        Dataset<Row> predictiondata = kmean_model.transform(test_data);
        ClusteringEvaluator evaluator = new ClusteringEvaluator().setPredictionCol("Cluster").setFeaturesCol("vector_features");
        double silhouette = evaluator.evaluate(predictiondata);
        System.out.println("Silhouette with squared euclidean distance = " + silhouette);
        try {
            kmean_model.save(hdfs_url+"kmeans");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
