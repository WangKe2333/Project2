import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


public class knn4 {
	
	public static int index=0; 
	public static class TokenizerMapper extends
     Mapper<Object, Text, Text, Text> {

   private final static IntWritable one = new IntWritable(1);
   private Text word = new Text();
   private Text word2 = new Text();
     
   public void map(Object key, Text value, Context context)
       throws IOException, InterruptedException {
	   String line=value.toString();
	   String[] strarray=line.split("\t"); 
	   String doc=strarray[0];
	   String line2=strarray[1];
       word.set(doc);
       word2.set(line2);
       context.write(word, word2); 
 }
}
	 public static class IntSumCombiner extends
     Reducer<Text, Text, Text, Text> {
	// private Text info = new Text();

   public void reduce(Text key, Iterable<Text> values, Context context)
       throws IOException, InterruptedException {
	 //连接起来
	   String List = new String();
       for (Text value : values) {
           List += value.toString()+",";
       }
       Text value2=new Text();
       value2.set(List);
       context.write(key, value2);
   }
 }
	 public static class IntSumReducer extends Reducer<Text, Text, Text, Text>{

	     private Text result = new Text();

	     @Override
	     protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
	             throws IOException, InterruptedException {

//	    	 int splitIndex=key.toString().indexOf(";");
//	    	 int splitIndex2=key.toString().indexOf("txt");
//	    	 String index=key.toString().substring(0,splitIndex);
//	    	 String doc=key.toString().substring(splitIndex+1,splitIndex2-1);
//	    	 key.set(doc);
	         String List = new String();
	         for (Text value : values) {
	             List += value.toString();
	         }
//	         String []a=List.split(",");
//	         for(String attribute:a){
//	        	 String []t=attribute.split(":");
//	         }
//	        	 
	         result.set(List);

	         context.write(key, result);
	     }

	 }
	 public static void main(String[] args) throws Exception {
		   Configuration conf = new Configuration();
		   String[] otherArgs =
		       new GenericOptionsParser(conf, args).getRemainingArgs();
		   if (otherArgs.length != 2) {
		     System.err.println("Usage: wordcount <in> <out>");
		     System.exit(2);
		   }
		   Job job = new Job(conf, "word count");
		   //System.out.println("Hello World!");
		   job.setJarByClass(knn4.class);
		   job.setMapperClass(TokenizerMapper.class);
		   job.setCombinerClass(IntSumCombiner.class);
		   job.setReducerClass(IntSumReducer.class);
		   job.setOutputKeyClass(Text.class);
		   job.setOutputValueClass(Text.class);
		   FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		   FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		   System.exit(job.waitForCompletion(true) ? 0 : 1);
		 }
}
