package inver;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.StringReader;  
import org.apache.lucene.analysis.Analyzer;  
import org.apache.lucene.analysis.TokenStream;  
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;  
import org.wltea.analyzer.lucene.IKAnalyzer;  

public class wordco {

 public static class TokenizerMapper extends
     Mapper<Object, Text, Text, Text> {

   private final static IntWritable one = new IntWritable(1);
   private Text word = new Text();
   private Text word2 = new Text();
     
   public void map(Object key, Text value, Context context)
       throws IOException, InterruptedException {
	   String line=value.toString();
	   String line2=value.toString();
	   String[] strarray=line.split("\t"); 
	     if(strarray.length>5)
	     {
	    	 line=strarray[4];
	    	 //System.out.print(line);
	    	 line2=strarray[5];
	     }
     StringTokenizer itr = new StringTokenizer(line);
     while (itr.hasMoreTokens()) {
    	//创建分词对象  
         Analyzer anal=new IKAnalyzer(true);       
         StringReader reader=new StringReader(itr.nextToken());
       //分词  
         TokenStream ts=anal.tokenStream("", reader);  
         CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);  
         while(ts.incrementToken()){  
       	  word.set(term.toString()+":"+line2);
       	  word2.set("1");
             context.write(word, word2);
         }  
       //word.set(itr.nextToken());
       //context.write(word, one);
     }
   }
 }

 public static class IntSumCombiner extends
     Reducer<Text, Text, Text, Text> {
	 private Text info = new Text();
   //private IntWritable result = new IntWritable();

   public void reduce(Text key, Iterable<Text> values, Context context)
       throws IOException, InterruptedException {
	 //统计词频
       int sum = 0;
       for (Text value : values) {
    	   sum += Integer.parseInt(value.toString() );
       }

       int splitIndex = key.toString().indexOf(":");

       //重新设置value值由URI和词频组成
       info.set( key.toString().substring( splitIndex + 1) +":"+sum );

       //重新设置key值为单词
       key.set( key.toString().substring(0,splitIndex));

       if(sum>10)
       {
    	   context.write(key, info);
       }
//     int sum = 0;
//     for (IntWritable val : values) {
//       sum += val.get();
//     }
//     result.set(sum);
//     context.write(key, result);
   }
 }
 
 public static class IntSumReducer extends Reducer<Text, Text, Text, Text>{

     private Text result = new Text();

     @Override
     protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
             throws IOException, InterruptedException {

         //生成文档列表
         String urlList = new String();
         for (Text value : values) {
             urlList += value.toString()+";";
         }
         result.set(urlList);

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
   job.setJarByClass(wordco.class);
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
