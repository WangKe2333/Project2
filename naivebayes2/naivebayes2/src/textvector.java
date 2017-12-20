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


public class textvector {
	
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
	   String word1=strarray[0];
	   String line2=strarray[1];
	   String[] strarray2=line2.split(";",0);
	   String t=String.valueOf(strarray2.length);
	  // System.out.println(t);
	   index=index+1;
     for(String str:strarray2) {
    	 String[] a=str.split(":");
         { 
       	  word.set(word1+";"+str);
       	  word2.set(t);
             context.write(word, word2);
         }  
     }
   }
 }

 public static class IntSumCombiner extends
     Reducer<Text, Text, Text, Text> {
	 private Text info = new Text();

   public void reduce(Text key, Iterable<Text> values, Context context)
       throws IOException, InterruptedException {
	 //统计词频
       int sum = 0;
       for (Text value : values) {
    	   sum += Integer.parseInt(value.toString() );
       }

       int splitIndex = key.toString().indexOf(":");
       int splitIndex3=key.toString().indexOf(";");
       int s=Integer.parseInt(key.toString().substring(splitIndex+1));
       int tf=sum;
       double t=1500.0/(double)(s+1);
       double idf=Math.log(t);
       double tf_idf=tf*idf;
       System.out.println(Double.toString(idf));

       //重新设置value值由URI和词频组成
      // info.set( key.toString().substring( splitIndex + 1) +":"+sum );

       //重新设置key值为单词	
       //info.set( key.toString().substring(0,splitIndex)+"-"+key.toString().substring(splitIndex+1, splitIndex2));
       info.set( key.toString().substring(0,splitIndex));
       Text value2=new Text();
       value2.set(Double.toString(tf_idf));

       if(sum>0)
       {
    	   context.write(info, value2);
       }
   }
 }
 
 public static class IntSumReducer extends Reducer<Text, Text, Text, Text>{

     private Text result = new Text();

     @Override
     protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
             throws IOException, InterruptedException {

    	 int splitIndex=key.toString().indexOf(";");
    	 int splitIndex2=key.toString().indexOf("txt");
    	 String index=key.toString().substring(0,splitIndex);
    	 String doc=key.toString().substring(splitIndex+1,splitIndex2-1);
    	 key.set(doc);
         String List = new String();
         for (Text value : values) {
             List += value.toString();
         }
         result.set(index+":"+List);

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
   job.setJarByClass(textvector.class);
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
