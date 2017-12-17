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

import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileReader;   
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.Reader;


public class naivebayes {
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
	   String train1=context.getConfiguration().get("k");
	   String[] train=train1.split("\n");
	   String[] a=line2.split(",");
	   String[] myindex=new String[a.length];
	   String[] mytfidf=new String[a.length];
	   int i=0;
	   for(String x : a){
		   myindex[i]=x.split(":")[0];
		   mytfidf[i]=x.split(":")[1];
		   i++;
	   }
	   double sum_negative=0;
	   double sum_positive=0;
	   double sum_neutral=0;
	   for(String y:train){
		   String[] train3=y.split("\t");
		   String[] train2=train3[1].split(":",0);
		   int j=0;
		   for(int v=0;v<myindex.length;v=v+1){
			   if(train3[0].indexOf(myindex[v])!=0){
				   sum_negative=sum_negative+Double.valueOf(mytfidf[v])*Double.valueOf(train2[0]);
				   sum_neutral=sum_neutral+Double.valueOf(mytfidf[v])*Double.valueOf(train2[1]);
				   sum_positive=sum_positive+Double.valueOf(mytfidf[v])*Double.valueOf(train2[2]);
			   }
			   
		   }
	   }
	   String type=new String();
	   double max=sum_negative;
	   type="negative";
	   if(sum_negative<sum_neutral){
		   type="neutral";
		   max=sum_neutral;
	   }
	   if(sum_positive>max){
		   type="positive";
	   }
       word.set(doc);
       word2.set(type);
       context.write(word, word2); 
 }
}
	public static class IntSumCombiner extends
    Reducer<Text, Text, Text, Text> {
	// private Text info = new Text();

  public void reduce(Text key, Iterable<Text> values, Context context)
      throws IOException, InterruptedException {
	  String List = new String();
      for (Text value : values) {
          List += value.toString();
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

	         String List = new String();
	         for (Text value : values) {
	             List += value.toString();
	         }   	 
	         result.set(List);

	         context.write(key, result);
	     }

	 }
	public static void main(String[] args) throws Exception {
		   Configuration conf = new Configuration();
		   String[] otherArgs =
		       new GenericOptionsParser(conf, args).getRemainingArgs();
		   if (otherArgs.length != 3) {
		     System.err.println("Usage: wordcount <in> <out>");
		     System.exit(2);
		   }
		   
		   //读取训练集文件
		   File file = new File(otherArgs[2]);
		   BufferedReader reader = null;  
		   reader = new BufferedReader(new FileReader(file));
		   String mystr="1";
		   String tempString = null;
		   while ((tempString = reader.readLine()) != null) {  
               // 相加
               mystr=mystr+tempString+"\n";
           } 
		   reader.close(); 
		   	   
		   conf.set("k", mystr);
		   Job job = new Job(conf, "word count");
		   //System.out.println("Hello World!");
		   job.setJarByClass(naivebayes.class);
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
