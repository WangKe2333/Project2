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


public class knn5 {
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
	   for(String y:train){
		   String[] train3=y.split("\t");
		   String[] train2=train3[1].split(",",0);
		   String[] trainindex=new String[train2.length];
		   String[] traintfidf=new String[train2.length];
		   int j=0;
		   for(String z:train2){
			   String[] tmp=z.split(":");
			   trainindex[j]=tmp[0];
			   traintfidf[j]=tmp[1];
		   }
		   double sum=0;
		   for(int v=0;v<myindex.length;v=v+1){
			   int flag=0;
			   for(int u=0;u<trainindex.length;u=u+1){
				   if(myindex[v].equals(trainindex[u])){
					   sum=sum+Math.pow((Double.valueOf(traintfidf[u])-Double.valueOf(mytfidf[v])),2);
				   }
			   }
			   if(flag==1){
				   sum=sum+Math.pow(Double.valueOf(mytfidf[v]),2);
			   }
		   }
		   word.set(doc);
		   word2.set(train3[0]+":"+Double.toString(sum));
		   context.write(word, word2); 
	   }
//       word.set(doc);
//       word2.set(line2);
//       context.write(word, word2); 
 }
}
	public static class IntSumCombiner extends
    Reducer<Text, Text, Text, Text> {
	// private Text info = new Text();

  public void reduce(Text key, Iterable<Text> values, Context context)
      throws IOException, InterruptedException {
	 //连接起来
	   String List = new String();
	   double[] sum=new double[2];
	   String[] train=new String[2];
	   int i=0;
      for (Text value : values) {
    	  String[] t=value.toString().split(":");
          sum[i]=Double.valueOf(t[1]);
          train[i]=t[0];
          if(i==1){
        	  if(sum[1]<sum[0]){
        		  sum[0]=sum[1];
        		  train[0]=train[1];
        	  }
          }else if(i==0){
        	  i++;
          }
      }
      Text value2=new Text();
      value2.set(train[0]);
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
	         String doc=List.toString().substring(0,8);    	 
	         result.set(doc);

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
		   job.setJarByClass(knn5.class);
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
