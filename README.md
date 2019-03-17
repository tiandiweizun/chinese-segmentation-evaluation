目录
- <a href="#java">java</a>
- <a href="#python">python</a>
- <a href="#总结">总结</a>
## java
#### Requirement
    java8
    
#### 步骤

1. `git clone https://github.com/tiandiweizun/nlp-evaluation.git`
2. `cd nlp-evaluation/java`
3. (windows) &nbsp;`.\gradlew.bat build`  &nbsp;&nbsp;(linux) &nbsp;`./gradlew build`
4. `java -Dfile.encoding=utf-8 -jar build/libs/nlp-evaluation-java-1.0.0.jar`


#### 说明
1. java -jar nlp-evaluation-java-1.0.0.jar 有3个参数，可以执行 java -jar nlp-evaluation-java-1.0.0.jar -h 查看
</br> -i 分词文件，默认为data/seg.data_big文件,每行一个句子，每个词用空格分开，可以指定自己的测试集
</br> -o 分词结果存储路径，默认不存储
</br> -n 最大读取分词文件行数
</br> -c 需要评估的分词器名称，用英文逗号隔开，默认HanLP，jieba，thulac

2. 由于[斯坦福分词](https://github.com/stanfordnlp/CoreNLP)效果一般，速度极慢，且模型巨大，在打包的时候已经排除（不影响在IDE里面测试），
      打包如果要包含斯坦福分词，修改build.gradle，注释掉exclude(dependency('edu.stanford.nlp:stanford-corenlp'))
3. 由于[Word](https://github.com/ysc/word)、[Ansj](https://github.com/NLPchina/ansj_seg)、[Jcseg](https://github.com/lionsoul2014/jcseg)、[MMSeg4j](https://github.com/chenlb/mmseg4j-core)存在bug（把词语拼接起来和原始句子不一样），在代码里面已经注释掉了，不进行测试。
4. 依赖的库均存在于maven中心仓库，像庖丁、复旦分词等找不到的，这里没有测试

    
#### 测试效果

总行数:2533709  总字符数:28374490

 |segmentor|precision| recall | f1   |  speed(字符/ms)_windows   | speed(字符/ms)_linux   |
 | --| -- | ------ | --- | --- | --- |
 |[HanLP](https://github.com/hankcs/HanLP)          |  0.900433  |  0.910614   |  0.905495  | 1034.470451  | 797.596346 |
 |[jieba](https://github.com/huaban/jieba-analysis) |  0.852657  |  0.803263   |  0.827223  | 1774.181830  | 980.865943 |
 |[thulac](https://github.com/yizhiru/thulac4j)     |  0.848839  |  0.901930   |  0.893082  | 1449.749131  | 939.832732 |
 
经过多次测试发现，thulac在linux上速度不是特别稳定，最快与jiba差不多

#### 开发者

- 建议使用idea打开或者导入java目录，把data目录拷贝到java目录，直接可以运行SegEvaluation调试。
- 可以打开stanford和其他分词器
- 评测自定义分词器：继承Seg类并实现segment方法，添加到evaluators即可。
 
## python

#### Requirement

    Python:3
    其他参见 requirements.txt
    
#### 步骤

    1. git clone https://github.com/tiandiweizun/nlp-evaluation.git
    2. cd nlp-evaluation
    3. pip3 install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
    4. cd python/indi.tiandi.nlp.evaluation
    5. python3 SegEvaluation.py   
    
#### 说明
1. python3 SegEvaluation.py 有3个参数，可以执行 python3 SegEvaluation.py -h 查看
</br>       -i 分词文件，默认为data/seg.data_big文件,每行一个句子，每个词用空格分开，可以指定自己的测试集
</br>       -o 分词结果存储路径，默认不存储
</br>       -n 最大读取分词文件行数，由于python速度太慢，建议设置
</br>       -c 需要评估的分词器名称，用英文逗号隔开，默认pkuseg，jieba，thulac

2. [pynlpir](https://github.com/tsroten/pynlpir)存在bug(把词语拼接起来和原始句子不一样)，[pyltp](https://github.com/HIT-SCIR/pyltp)在windows上不易安装，这里都没有进行测试

##### 测试效果 

总行数:2533709  总字符数:28374490

|segmentor|precision| recall | f1  |  speed(字符/ms)_windows   | speed(字符/ms)_linux   |
| --| -- | ------ | --- | --- |--- |
|[pkuseg](https://github.com/lancopku/pkuseg-python) |  0.890170  |  0.886405  | 0.888284  |  34.077104 |  19.826954  |
|[jieba](https://github.com/fxsjy/jieba)             |  0.855293  |  0.808204  | 0.831082  | 169.651694 | 104.554222  |
|[thulac](https://github.com/thunlp/THULAC-Python)   |  0.848839  |  0.883031  | 0.865597  |  28.831738 |  16.565779  |
|[pyltp](https://github.com/HIT-SCIR/pyltp)          |  0.894885  |  0.908761  | 0.901770  |  --------- |  52.371131  |

#### 开发者

- 建议使用pycharm打开python目录，即可运行
- 如果需要使用pynlpir，需要修改pynlpir_path的安装目录
- 如果需要使用pyltp，需要修改ltp_data_dir的模型分词目录
- 评测自定义分词器：只要实现segment方法和向evaluators追加即可。

## 总结
- 性能：java 远高于python，至少差了一个数量级。
- 效果：对于jieba和thulac，在python和java上表现的不同，需要更多的时间去寻找原因，且java的thulac4j非官方提供。
- 数据：默认数据集来源于[cws_evaluation](https://github.com/ysc/cws_evaluation)，该项目为评估中文分词的性能与效果，对于效果该项目采用的是行完美率这个指标，但是对于长句，这个指标会变的不合适，如果不同算法的错误率不一样，但是如果有一个错的词，会导致整个句子都是错的，不能很好的区分算法的precision
