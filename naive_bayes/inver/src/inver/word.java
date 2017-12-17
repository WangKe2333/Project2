package inver;

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class word {
	public static class InvertedIndexMapper extends Mapper<Object, Text, Text, Text>{

        private Text keyInfo = new Text();  // 存储单词和URI的组合
        private Text valueInfo = new Text(); //存储词频
        private FileSplit split;  // 存储split对象。
        @Override
        protected void map(Object key, Text value, Mapper<Object, Text, Text, Text>.Context context)
                throws IOException, InterruptedException {
            //获得<key,value>对所属的FileSplit对象。
            split = (FileSplit) context.getInputSplit();
            System.out.println("偏移量"+key);
            System.out.println("值"+value);
            //StringTokenizer是用来把字符串截取成一个个标记或单词的，默认是空格或多个空格(\t\n\r等等)截取
            StringTokenizer itr = new StringTokenizer( value.toString());
            while( itr.hasMoreTokens() ){
                // key值由单词和URI组成。
                keyInfo.set( itr.nextToken()+":"+split.getPath().toString());
                //词频初始为1
                valueInfo.set("1");
                context.write(keyInfo, valueInfo);
            }
            System.out.println("key"+keyInfo);
            System.out.println("value"+valueInfo);
        }
    }
	 public static class InvertedIndexCombiner extends Reducer<Text, Text, Text, Text>{
	        private Text info = new Text();
	        @Override
	        protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
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

	            context.write(key, info);
	            System.out.println("key"+key);
	            System.out.println("value"+info);
	        }
	    }
	 public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text>{

	        private Text result = new Text();

	        @Override
	        protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context)
	                throws IOException, InterruptedException {

	            //生成文档列表
	            String fileList = new String();
	            for (Text value : values) {
	                fileList += value.toString()+";";
	            }
	            result.set(fileList);

	            context.write(key, result);
	        }

	    }

	    public static void main(String[] args) {
	        try {
	            Configuration conf = new Configuration();

	            Job job = Job.getInstance(conf,"InvertedIndex");
	            job.setJarByClass(word.class);

	            //实现map函数，根据输入的<key,value>对生成中间结果。
	            job.setMapperClass(InvertedIndexMapper.class);

	            job.setMapOutputKeyClass(Text.class);
	            job.setMapOutputValueClass(Text.class);

	            job.setCombinerClass(InvertedIndexCombiner.class);
	            job.setReducerClass(InvertedIndexReducer.class);

	            job.setOutputKeyClass(Text.class);
	            job.setOutputValueClass(Text.class);

	            //我把那两个文件上传到这个index目录下了
	            FileInputFormat.addInputPath(job, new Path("hdfs://192.168.52.140:9000/index/"));
	            //把结果输出到out_index+时间戳的目录下
	            FileOutputFormat.setOutputPath(job, new Path("hdfs://192.168.52.140:9000/out_index"+System.currentTimeMillis()+"/"));

	            System.exit(job.waitForCompletion(true) ? 0 : 1);
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	    }

}
