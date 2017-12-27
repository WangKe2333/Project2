---
title: "Project1 Mapreduce高级编程"
author: "wangke"
date: "2017年12月16日"
output:
  html_document: default
  pdf_document: default
---

创新：比较Python和Java的MapReduce(更新 Python版本的实现)</br>
------------------------------
+ 在完成基本的要求之后,看到很多同学之前使用Python版本的MapReduce，我也想要尝试学习一下，于是我又将KNN和朴素贝叶斯用Python重新实现了一遍，相关代码已经放在了project2 的Python文件夹下，基本的实现思路与Java相同</br>
+ 在Java和Python两个版本的比较当中，Java的Key-value对使用更加方便，Python利用标准化的sys.stdin和streamming jar包实现，在key-value对的实现上，需要自己写迭代器group相同的key值对相应的value值进行加总等等处理。</br>
+ 在速度上两者区别也很明显，Java明显运行的比Python要快许多（在我的电脑上），越是调用Python相应的第三方库等等就越是慢~，比如Python的jieba分词，虽然方便，分的却比较慢</br>
+ 但Python仍然是有很多比较好用的字符串处理接口，在写程序的时候很是方便，也极大的减少了代码量，python的代码量大概只在Java的1/2左右</br>
### 下面是具体实现说明
+ 迭代器实现相应的key-value对group
```{}
from itertools import groupby  
from operator import itemgetter  
import sys  
  
def read_mapper_output(file, separator='\t'):  
    for line in file:  
        yield line.rstrip().split(separator, 1)  
  
def main(separator='\t'):   
    data = read_mapper_output(sys.stdin, separator=separator)  
    for current_word, group in groupby(data, itemgetter(0)):  
        try: 
            total_count=0
            doclist=""
            for current_word,count in group:
                doclist=doclist+count+";"
            print("%s%s%s" % (current_word, separator, doclist))  
        except ValueError:  
            pass  

```

+ python得到文件夹名字得到相应的类型标记
```{}
filepath = os.environ["map_input_file"]
fileparent=os.path.split(filepath)[0]
filename1 = os.path.split(fileparent)[1]
filename2=os.path.split(filepath)[1]
```
+ 筛选中文字符
```{}
if(word >= u'\u4e00' and word <= u'\u9fa5'):
            print('%s\t%s' % (word+":"+filename1+filename2, "1"))
```

+ python 的max和sort接口都更加方便
```{}
mymax=num.index(max(num))
train[mymax].find("negative")
num2=sorted(num)
    for i in range(0,k):
        for j in range(0,len(num)):
            if(num2[i]==num[j]):
                max.append(train[j])
```
![output1](https://github.com/WangKe2333/Project2/raw/master/picture/output1.png)</br>
![output2](https://github.com/WangKe2333/Project2/raw/master/picture/output2.png)</br>
![output3](https://github.com/WangKe2333/Project2/raw/master/picture/output3.png)</br>


Java:实验设计说明</br>
----------------------------
+ 主要设计思路</br>
</br>
1. KNN方法分类<br>
       + (1)首先，要对数据进行文本向量化，寻找工具将输入文件的格式转换为utf-8，解决乱码问题。而后将三个文件夹下的文件传入java文件(knn/inver/knn.java)当中进行类似文档倒排索引的处理。第一，mapper节点进行分词，并得到当前文件的文件名和父文件夹名，发送（word+filePath+fileName，1）的key-value对。第二，combiner汇总后将key设为单词，value设为fileName+词频。第三，reducer节点汇总value中的属于同一个单词的fileName+词频，生成文档列表(knn/stage1)，形如:</br>不无关系	negative67.txt:1;positive368.txt:1;neutral144.txt:1;positive332.txt:1;negative183.txt:1;neutral463.txt:1;positive378.txt:1;</br>
</br>
    * (2)生成文档列表后，将其(knn/stage1)作为输入文件输入(knn/knn/src/knn3.java)计算每个单词--文档的TF-IDF值。第一，mapper节点对于输入文件进行处理,维护全局变量index，将每个单词替换为index;利用字符串split函数将单词后的文档拆分出来发送(单词index+filename,词频)的key-value对。第二，combiner节点汇总计算具体的TF-IDF值,发送(单词index+filename,tf_idf值)的key-value对。第三，reducer节点汇总并按照固定格式输出(knn/tf_idf),形如:</br>negative86	17705:761.308418750991</br>
</br>
    * (3)计算出tf_idf值之后，需要汇总形成文本向量化向量格式，将(knn/tf_idf)作为输入(knn/knn/src/knn4.java)汇总形成文本向量化。第一，mapper节点发送(filename,单词index+tf_idf值)的key-value对。第二，相同key值会被发送到同一个combiner节点，故在combiner当中进行汇总，将同一个filename的value值都连接起来，发送连接后的key-value对。第三，reducer输出，并根据tf-idf值直接选择特征(knn/text vector)</br>
</br>
    * (4)完成文本向量化后，将(knn/text vector)输入到(knn/knn/src/knn5.java)进行knn计算。第一，main函数读入训练集数据，并通过conf.set函数将读入的训练集数据传给mapper节点。第二，mapper按行读入需要预测的数据（文本向量化处理后的）,遍历训练集的数据与所有数据计算距离，并发送(filename,train类型+距离)的key-value对。第二，combiner节点，提取距离值，根据距离该节点最近的n个节点投票决定该节点的类型，输出(filename,类型)的key-value对。第三，reducer节点，汇总输出(knn/final)。完成分类预测任务。</br>
![程序流程图1](https://github.com/WangKe2333/Project2/raw/master/picture/程序流程图1.png)</br>
![程序流程图2](https://github.com/WangKe2333/Project2/raw/master/picture/程序流程图2.png)</br>
![程序伪代码](https://github.com/WangKe2333/Project2/raw/master/picture/伪代码1.png)</br>
</br>
+ 朴素贝叶斯分类</br>
  - (1)首先根据已有数据训练(naive_bayes/inver/bayes.java)。第一，mapper节点进行分词，并得到当前文件的文件名和父文件夹名，发送（word+filePath+fileName，1）的key-value对。第二，combiner汇总后将key设为单词，value设为fileName+词频。第三，reducer节点汇总，统计该词在出现过的文档总数，该词出现条件下，文档为positive negative netural的概率，并输出(naive_bayes/train)。</br>
</br>
  - (2)而后同样进行文本向量化(naive_bayes/bayes/src/textvector.java和knn/knn/src/knn4.java)，此时不可继续使用word的index，因为无法确保该index与训练集当中的照应，故还是维持文本的形式，比照单词字符串得出结论。结果形如</br>negative0	网站:1085.6920058709784,值:926.8102489142499,估:483.26534407671596,尘埃落定:33.10036603265178,并于:244.94270864162317,股份:3236.101610179844,规:213.36933693493478,1月:887.0898096750677,内部:540.6709045627307,2014-01-12:6.620073206530356,报了:39.720439239182134,市场:3455.678213808846,合:285.8719725274208,报:1487.6584325185731,10:1568.9573499476944,要求:953.2905417403713,质疑:271.4230014677446,放卫星:13.240146413060712,21:317.7635139134571,证监会:503.12556369630704,导致:940.0503953273105,125.:13.240146413060712,中:3726.986119557016,发布:1999.2621083721676,研究报告:278.043074674275,新闻网:297.903294293866,零七:302.27322732449096,过高:33.10036603265178,此前:913.5701025011891,增加:1045.9715666317963,介入:238.32263543509282,投合:6.620073206530356,研:634.1810847788339,方案:509.7456369028374,增:1244.573762827707,监管部门:93.21912147633287,引发:244.94270864162317,31日:615.6668082073231,证券:2293.720372050549,去年:1045.9715666317963,调查:390.584319185291,投:948.3081641552657,证监局:111.86294577159944,责令:46.34051244571249,05:311.14344070692675,强推:19.860219619591067,深圳:938.405822861751,加对:13.240146413060712,次数:26.480292826121424,2014:6.620073206530356,对此:609.0467350007928,日前:589.1865153812017,提交:66.20073206530355,针对:609.0467350007928,事件:459.88099928324215,之前:536.2259297289588,关注:2171.384011741957,热点:244.94270864162317,头条:33.10036603265178,通:522.9857833158982,因在:13.240146413060712,消费:913.5701025011891,具体:549.4660761420196,检查:130.39237257134903,备受:172.12190336978927,处理结果:6.214608098422191,</br>
</br>
  - (3)而后读入文件进行朴素贝叶斯分类(naive_bayes/bayes/src/naivebayes.java)。第一，main函数读入训练集数据，并通过conf.set函数将读入的训练集数据传给mapper节点。第二，mapper按行读入需要预测的数据（文本向量化处理后的）,遍历训练集的数据与所有数据计算三个贝叶斯值，贝叶斯值最大的那个表示该数据集是该类的可能性最大，mapper发送key-value对(filename,类型)。第三，combiner和reducer进行汇总，按照要求输出(naive_bayes/final2)</br>
  - (4)我最初实现了一个简单版的naivebayes 属性值只是以单词是否出现作为标准，后又实现了正常版本的naivebayes，以单词的出现次数作为属性</br>
![程序流程图3](https://github.com/WangKe2333/Project2/raw/master/picture/程序流程图3.png)</br>
![程序伪代码](https://github.com/WangKe2333/Project2/raw/master/picture/伪代码2.png)</br>
+ 算法设计</br>
  - (1)Java 中文分词依然沿用Project1的方法，使用IKAnalyzer分词器进行分词，这里不再赘述。</br>
  - (2)文本向量化的设计,首先读入父文件夹名加在本文件名前作为属性标记，将文件名作为最终的key值输出。先做文档倒排索引，再利用文档倒排索引将其分为一个一个的单词--文档key-value对，利用同一个key会被发送到同一个combiner节点这一特点，计算tf-idf值，而后将其整理为矩阵形式（链表表示）</br>
  - (3)knn距离计算，遍历训练集，判断index是否相同，相同index相减平方，不同index直接平方计算距离，计算出所有距离，维护min列表找最近的n个点</br>
  - (4)训练与预测，训练集直接又main函数读入，传给mapper，这样使得每一个mapper都可以接触到所有训练集以便于计算距离和贝叶斯值；预测集正常一行一行读入</br>
  - (5)朴素贝叶斯，对于每一个单词统计该单词出现时，文档为正负中性的概率，将概率作为训练集，将文本向量化后的文档作为预测集，每个预测集计算自己为正负中性的概率，概率最大的为它的属性值</br>
  - (6)以单词出现频率作为属性的，我将单位从单个单词更新为单词--次数，实现从以单词是否出现改为以单词出现次数作为属性</br>
+ 程序</br>
  - (1)设置读取子文件夹</br>
```{}
    FileInputFormat.setInputDirRecursive(job, true);
```
  - (2)分词</br>
```{}
 while (itr.hasMoreTokens()) {
   	//创建分词对象  
        Analyzer anal=new IKAnalyzer(true);       
        StringReader reader=new StringReader(itr.nextToken());
      //分词  
        TokenStream ts=anal.tokenStream("", reader);  
        CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);  
        while(ts.incrementToken()){  
      	  word.set(term.toString()+":"+parent+fileName);
      	  word2.set("1");
            context.write(word, word2);
      }

```
  - (3)获得文件名</br>
```{}
     FileSplit fileSplit = (FileSplit) context.getInputSplit();
	   String fileName = fileSplit.getPath().getName(); 
	   String parent=fileSplit.getPath().getParent().getName();
```
  - (4)TF-IDF计算</br>
```{}
       int splitIndex = key.toString().indexOf(":");
       int s=Integer.parseInt(key.toString().substring(splitIndex+1));
       int tf=sum;
       double t=1500.0/(double)(s+1);
       double idf=Math.log(t);
       double tf_idf=tf*idf;
       System.out.println(Double.toString(idf));

```
 - (4)主程序读取测试文件</br>
```{}
       //读取训练集文件
		   File file = new File(otherArgs[2]);
		   BufferedReader reader = null;  
		   reader = new BufferedReader(new FileReader(file));
		   String mystr=null;
		   String tempString = null;
		   while ((tempString = reader.readLine()) != null) {  
               // 相加
               mystr=mystr+tempString+"\n";
           } 
		   reader.close(); 
```
 - (5)传参</br>
```{}
    conf.set("k", mystr);
    -------------------------------------------------
    String train1=context.getConfiguration().get("k");
    String[] train=train1.split("\n");

```
 - (6)计算距离</br>
```{}
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
```
 - (7)朴素贝叶斯</br>
```{}
     double sum_negative=0;
	   double sum_positive=0;
	   double sum_neutral=0;
	   for(String y:train){
		   String[] train3=y.split("\t");
		   String[] train2=train3[1].split(":",0);
		   int j=0;
		   for(int v=0;v<myindex.length;v=v+1){
			   if(train3[0].indexOf(myindex[v])!=-1){
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
```

程序类的说明</br>
-----------------------
+ inver/knn.java文件中</br>
  - mapper类：mapper节点进行分词，并得到当前文件的文件名和父文件夹名，发送(word+filePath+fileName,1)的key-value对</br>
  - combiner类：combiner汇总后将key设为单词，value设为fileName+词频</br>
  - reducer类：reducer节点汇总value中的属于同一个单词的fileName+词频，生成文档列表(knn/stage1)</br>
  - main:仅基础功能</br>
![类1](https://github.com/WangKe2333/Project2/raw/master/picture/类1.png)
+ inver/bayes.java文件中</br>
  - mapper类：mapper节点进行分词，并得到当前文件的文件名和父文件夹名，发送(word+filePath+fileName,1)的key-value对</br>
  - combiner类：combiner汇总后将key设为单词，value设为fileName+词频</br>
  - reducer类：reducer节点汇总，统计该词在出现过的文档总数，该词出现条件下，文档为positive negative netural的概率，并输出(naive_bayes/train)</br>
  - main:仅基础功能</br>
![类2](https://github.com/WangKe2333/Project2/raw/master/picture/类2.png)
+ knn/knn3.java文件中</br>
  - mapper类：mapper节点对于输入文件进行处理,维护全局变量index，将每个单词替换为index;利用字符串split函数将单词后的文档拆分出来发送(单词index+filename,词频)的key-value对</br>
  - combiner类：combiner节点汇总计算具体的TF-IDF值,发送(单词index+filename,tf_idf值)的key-value对
出</br>
  - reducer类：reducer节点汇总并按照固定格式输出(knn/tf_idf)</br>
  - main:仅基础功能</br>
![类3](https://github.com/WangKe2333/Project2/raw/master/picture/类3.png)
+ knn/knn4.java文件中</br>
  - mapper类：mapper节点发送(filename,单词index+tf_idf值)的key-value对</br>
  - combiner类：相同key值会被发送到同一个combiner节点，故在combiner当中进行汇总，将同一个filename的value值都连接起来，发送连接后的key-value对</br>
  - reducer类：reducer输出，并根据tf-idf值直接选择特征(knn/text vector)</br>
  - main:仅基础功能</br>
![类4](https://github.com/WangKe2333/Project2/raw/master/picture/类4.png)
+ knn/knn5.java文件中</br>
  - mapper类：mapper按行读入需要预测的数据（文本向量化处理后的）,遍历训练集的数据与所有数据计算距离，发送(filename,train类型+距离)key-value对</br>
  - combiner类：combiner节点，提取距离值，根据距离该节点最近的n个节点投票决定该节点的类型，输出(filename,类型)的key-value对</br>
  - reducer类：reducer节点，汇总输出(knn/final)</br>
  - main:main函数读入训练集数据，并通过conf.set函数将读入的训练集数据传给mapper节点</br>
![类5](https://github.com/WangKe2333/Project2/raw/master/picture/类5.png)
+ naive_bayes/textvector.java文件中</br>
  - mapper类：mapper节点依然维持单词，利用字符串split函数将单词后的文档拆分出来发送(单词index+filename,词频)的key-value对</br>
  - combiner类：combiner节点汇总计算具体的TF-IDF值,发送(单词index+filename,tf_idf值)的key-value对</br>
  - reducer类：reducer节点汇总并按照固定格式输出(knn/tf_idf)出</br>
  - main:仅基础功能</br>
![类6](https://github.com/WangKe2333/Project2/raw/master/picture/类6.png)
+ naive_bayes/naivebayes.java</br>
  - mapper类：mapper按行读入需要预测的数据（文本向量化处理后的）,遍历训练集的数据与所有数据计算三个贝叶斯值，贝叶斯值最大的那个表示该数据集是该类的可能性最大，mapper发送key-value对(filename,类型)
</br>
  - combiner类：combiner和reducer进行汇总</br>
  - reducer类：combiner和reducer进行汇总，按照要求输出(naive_bayes/final2)</br>
  - main:main函数读入训练集数据，并通过conf.set函数将读入的训练集数据传给mapper节点</br>
![类7](https://github.com/WangKe2333/Project2/raw/master/picture/类7.png)


运行结果及分析</br>
--------------------
+ 文本向量化(knn)</br>
![text1](https://github.com/WangKe2333/Project2/raw/master/picture/textknn.png)
+ 文本向量化(bayes)</br>
![text2](https://github.com/WangKe2333/Project2/raw/master/picture/textbayes.png)
+ 预测集(knn)</br>
![pro1](https://github.com/WangKe2333/Project2/raw/master/picture/pro1.png)
+ 预测结果(knn)</br>
![final1](https://github.com/WangKe2333/Project2/raw/master/picture/final1.png)
+ 预测集(bayes)</br>
![pro2](https://github.com/WangKe2333/Project2/raw/master/picture/pro2.png)
+ 预测结果(bayes)</br>
![final2](https://github.com/WangKe2333/Project2/raw/master/picture/final2.png)
+ 训练结果1(bayes2)</br>
![pro3](https://github.com/WangKe2333/Project2/raw/master/picture/text1.png)
+ 训练结果2(bayes2)</br>
![pro3](https://github.com/WangKe2333/Project2/raw/master/picture/text2.png)
+ 预测集(bayes2)</br>
![pro3](https://github.com/WangKe2333/Project2/raw/master/picture/pro3.png)
+ 预测结果(bayes2)</br>
![bayes2](https://github.com/WangKe2333/Project2/raw/master/picture/final3.png)
</br>12.程序运行</br>
![text1](https://github.com/WangKe2333/Project2/raw/master/picture/1.png)
</br>13.程序输出</br>
![text1](https://github.com/WangKe2333/Project2/raw/master/picture/2.png)

</br>
#project1过程中遇到的问题及思考</br>
1.本次实验中最有趣也是最有挑战性的地方在于算法和程序思路需要自己设计，我也在这个过程当中体会到了大数据并行化MapReduce的思想和方法，对于所学的知识和key-value对的设计有了更加深刻的理解，感觉自己通过此次思考和训练收获很大，Java编程也越来越熟练了</br>
2.首先的问题即是需要批量处理文本文件成utf-8格式，通过搜索我很快在网上找到了合适的工具进行了批量转换；而后一个挑战就是要标记文本原本的属性值：negative positive还是neural，这个标记以什么样的形式添加在哪里就成为一个很值得思考的问题，最后我决定将其与文件名连在一起进行一个标记，那么要进行这个标记就要分成三个文件夹进行读取，并且获取父文件夹的名称，作为自己的属性。通过filesplit和设置参数我实现了这一点</br>
3.而后便是文本向量化应当如何处理的问题，由于该矩阵为一个稀疏矩阵，我决定用链表的方式进行存储，链表的标记使用单词的index来实现，通过维护一个全局的index实现单词与index之间的一一替换，不过后来我发现其实也不需要进行这个替换，反正是匹配查值，使用单词本身也是可以的。</br>
4.而后就是如何处理预测集和训练集的问题，经过思考我决定，训练集直接又main函数读入，而后以参数的形式传递给mapper节点，这样便可以保证mapper节点有所有的训练集信息，可以进行全部的距离计算。</br>
5.在传值和类型转换的时候必须多加注意，不然很容易出bug，比如我最初读入文件到main函数，直接进行了行于行之间的字符串连接，忘记添加换行符，导致后面字符串分割出现问题等等。</br>
6.为了传值的灵活性和方便，我将key-value的传值方式均设为了text格式</br>

</br>
#性能扩展性不足及可能的改进之处</br>
1.程序功能较为单一，在分词方面直接调用了分词器和开源程序的内容，没有做更加细致的处理</br>
2.只实现了最基础的朴素贝叶斯，思想较为简单，未来还可以有所提升</br>
3.训练集一次性读入的方式有待改善，若训练集也能够并行读入更好</br>

</br>
</br>
</br>








