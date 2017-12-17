---
title: "金融大数据第五次作业"
author: "151278033 王珂"
date: "2017年11月21日"
output:
  html_document: default
  pdf_document: default
---
（更新）11月22日</br>
1.上次交作业错误的理解了题意，我重新首先用matlab函数random出150个二维点，而后将它们导出为txt文件，在MapReduce上运行计算结果；然后将MapReduce程序的输出导入MATLAB当中画图</br>
首先，用MATLABrandom150个二维点并导出
```{}
%随机获取150个点
X = [randn(50,2)+ones(50,2);randn(50,2)-ones(50,2);randn(50,2)+[ones(50,1),-ones(50,1)]];
fid = fopen('tt.txt','wt');
for i = 1:size(X,1)
    fprintf(fid,'%f,%f\n',X(i,1),X(i,2));
end
fclose(fid);
plot(X(1),X(2),'k')

```
导出为txt文件</br>
![txt文件](https://github.com/WangKe2333/Kmeans/raw/master/picture/随机生成150个点.png)
</br>MapReduce当中运行</br>
```{}
bin/hdfs dfs -put data/tt.txt /user
bin/hdfs dfs -ls /user
bin/hadoop jar share/hadoop/mapreduce/kmeans.jar KMeansDriver 2 2 /user/tt.txt output5
bin/hdfs dfs -get output5 output3
cat output3/clusteredInstances/part-m-00000

bin/hdfs dfs -put data/tt.txt /user
bin/hdfs dfs -ls /user
bin/hadoop jar share/hadoop/mapreduce/kmeans.jar KMeansDriver 3 10 /user/tt.txt output6
bin/hdfs dfs -get output6 output4
cat output4/clusteredInstances/part-m-00000

bin/hdfs dfs -put data/tt.txt /user
bin/hdfs dfs -ls /user
bin/hadoop jar share/hadoop/mapreduce/kmeans.jar KMeansDriver 3 2 /user/tt.txt output7
bin/hdfs dfs -get output7 output5
cat output5/clusteredInstances/part-m-00000
```
结果：</br>
![结果](https://github.com/WangKe2333/Kmeans/raw/master/picture/MapReduce输出.png)
</br>然后导入MATLAB当中</br>
```{}
load part-m-00000.txt %导入数据
M=part_m_00000; 
M1=M(:,1:2); %前两列为点
M2=M(:,3);%最后一列为类别

%画出聚类为1的点。X(Idx==1,1),为第一类的样本的第一个坐标；X(Idx==1,2)为第二类的样本的第二个坐标
plot(M1(M2==1,1),M1(M2==1,2),'r.','MarkerSize',14)
hold on
plot(M1(M2==2,1),M1(M2==2,2),'b.','MarkerSize',14)
hold on
plot(M1(M2==3,1),M1(M2==3,2),'g.','MarkerSize',14)

legend('Cluster 1','Cluster 2','Cluster3','Centroids','Location','NW')
```
画图</br>
聚类（3，10）</br>
![聚类3 10](https://github.com/WangKe2333/Kmeans/raw/master/picture/聚类图(3%2C10).png)

聚类（3，2）</br>
![聚类3 2](https://github.com/WangKe2333/Kmeans/raw/master/picture/聚类图(3%2C2).png)

聚类（2，2）</br>
![聚类2 2](https://github.com/WangKe2333/Kmeans/raw/master/picture/聚类图(2%2C2).png)

结论：从图形上看MapReduce对于点的聚类结果与MATLAB自带函数的聚类结果相同，迭代次数越多 效果越好</br>

2.在MapReduce上运行6维数据集
运行KMeans的Java代码和小测试集（Java代码较多，已附件，此处不再贴Java代码）
输入参数：聚为两类，迭代10次

```{}
bin/hdfs dfs -mkdir /user
bin/hdfs dfs -put data/instance.txt /user
bin/hdfs dfs -ls /user
bin/hadoop jar share/hadoop/mapreduce/kmeans.jar KMeansDriver 2 10 /user/instance.txt output3
bin/hdfs dfs -get output3 output2
cat output2/clusteredInstances/part-m-00000
```
结果：</br>
2,1,3,4,1,4	1</br>
3,2,5,2,3,5	1</br>
4,4,4,3,1,5	1</br>
2,3,1,2,0,3	1</br>
4,0,1,1,1,5	1</br>
1,2,3,5,0,1	2</br>
5,3,2,2,1,3	1</br>
3,4,1,1,2,1	2</br>
0,2,3,3,1,4	1</br>
0,2,5,0,2,2	2</br>
2,1,4,5,4,3	2</br>
4,1,4,3,3,2	2</br>
0,3,2,2,0,1	1</br>
1,3,1,0,3,0	2</br>
3,3,4,2,1,3	1</br>
3,5,3,5,3,2	2</br>
2,3,2,3,0,1	1</br>
4,3,3,2,2,3	1</br>
1,4,3,4,3,1	2</br>
3,2,3,0,2,5	1</br>
1,0,2,1,0,4	1</br>
4,4,3,5,5,4	2</br>
5,1,4,3,5,2	2</br>
3,4,4,4,1,1	2</br>
2,2,4,4,5,5	2</br>
5,2,0,3,1,3	1</br>
1,1,3,1,1,3	1</br>
2,4,2,0,3,5	1</br>
1,1,1,1,0,4	1</br>
1,1,4,1,3,0	2</br>
遇到的问题</br>
我之前运行hadoop忘记sbin/stop-dfs.sh关闭……结果再次运行的时候已经无法关闭，DataNode和NameNode也都启不启来，报错说local/172.25.169.27正在使用 please shutting down；后来要找到进程号强制杀掉</br>
http://blog.csdn.net/asia_kobe/article/details/51067769</br>
一些截图：</br>
运行程序截图：
![运行程序](https://github.com/WangKe2333/Kmeans/raw/master/运行程序.png)
![运行输出](https://github.com/WangKe2333/Kmeans/raw/master/程序运行输出.png)
</br>运行结果：</br>
![运行结果](https://github.com/WangKe2333/Kmeans/raw/master/程序运行结果.png)


3.MATLAB自带kmeans函数聚类
以下是MATLAB的代码
```{}
%随机获取150个点
X = [randn(50,2)+ones(50,2);randn(50,2)-ones(50,2);randn(50,2)+[ones(50,1),-ones(50,1)]];
opts = statset('Display','final');

%调用Matlab内部的Kmeans函数

[Idx,Ctrs,SumD,D] = kmeans(X,3,'Replicates',1,'Options',opts);

%画出聚类为1，2，3，4的点
plot(X(Idx==1,1),X(Idx==1,2),'r.','MarkerSize',14)
hold on
plot(X(Idx==2,1),X(Idx==2,2),'b.','MarkerSize',14)
hold on
plot(X(Idx==3,1),X(Idx==3,2),'g.','MarkerSize',14)
%hold on
%plot(X(Idx==4,1),X(Idx==4,2),'y.','MarkerSize',14)

%绘出聚类中心点
plot(Ctrs(:,1),Ctrs(:,2),'kx','MarkerSize',14,'LineWidth',4)
plot(Ctrs(:,1),Ctrs(:,2),'kx','MarkerSize',14,'LineWidth',4)
plot(Ctrs(:,1),Ctrs(:,2),'kx','MarkerSize',14,'LineWidth',4)
%plot(Ctrs(:,1),Ctrs(:,2),'kx','MarkerSize',14,'LineWidth',4)

legend('Cluster 1','Cluster 2','Cluster3','Centroids','Location','NW')

```

运行截图：
</br>2个类：</br>
![两个类](https://github.com/WangKe2333/Kmeans/raw/master/2个类.png)
</br>三个类：</br>
![三个类](https://github.com/WangKe2333/Kmeans/raw/master/3个类.png)
</br>四个类：</br>
![四个类](https://github.com/WangKe2333/Kmeans/raw/master/4个类.png)

</br>3个类迭代十次：（我觉得好像迭代次数的影响不大）</br>
![3个类迭代十次](https://github.com/WangKe2333/Kmeans/raw/master/3个类%20迭代10次.png)





