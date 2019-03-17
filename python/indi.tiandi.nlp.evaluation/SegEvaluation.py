import argparse
import jieba
import os
import pkuseg
import pynlpir
import thulac
import time
from snownlp import SnowNLP


class Seg:
    def __init__(self):
        pass

    def segment(self, sentence):
        pass


# 中科院分词，python版本有bug和demo页面不一致
class pynlpir_impl:
    def __init__(self):
        # 通过代码自动更新lience
        pynlpir_impl.update_pynlpir_lience()
        pynlpir.open()

    @staticmethod
    def update_pynlpir_lience():
        import requests
        # pynlpir 分词授权下载地址
        # https://github.com/NLPIR-team/NLPIR/tree/master/License
        url = 'https://raw.githubusercontent.com/NLPIR-team/NLPIR/master/License/license%20for%20a%20month/NLPIR-ICTCLAS%E5%88%86%E8%AF%8D%E7%B3%BB%E7%BB%9F%E6%8E%88%E6%9D%83/NLPIR.user'
        r = requests.get(url)
        # 拷贝到pynlpir相应的目录
        pynlpir_path = "D:/Anaconda3/Lib/site-packages/pynlpir/Data"
        with open(os.path.join(pynlpir_path, "NLPIR.user"), "wb") as code:
            code.write(r.content)

    def segment(self, sentence):
        return pynlpir.segment(sentence, pos_tagging=False)


# 北大分词
class pkuseg_impl(Seg):
    def __init__(self):
        self.pku_seg = pkuseg.pkuseg()

    def segment(self, sentence):
        return self.pku_seg.cut(sentence)


# 结巴分词
class jieba_impl(Seg):
    def segment(self, sentence):
        return jieba.lcut(sentence)


class snownlp_impl(Seg):
    def segment(self, sentence):
        return SnowNLP(sentence).words


# 清华分词
class thulac_impl(Seg):
    def __init__(self):
        self.thu1 = thulac.thulac(seg_only=True)  # 默认模式

    def segment(self, sentence):
        return self.thu1.cut(sentence, text=True).split()  # 进行一句话分词


# 哈工大分词
class pyltp_impl(Seg):

    def __init__(self):
        ltp_data_dir = '/home/work/tiandi/ltp.model/ltp_data'  # ltp模型目录的路径，由于cws文件比较大，需要自行下载 http://model.scir.yunfutech.com/model/ltp_data_v3.4.0.zip
        cws_model_path = os.path.join(ltp_data_dir, 'cws.model')  # 分词模型路径，模型名称为`cws.model`
        from pyltp import Segmentor
        self.segmentor = Segmentor()
        self.segmentor.load(cws_model_path)

    def segment(self, sentence):
        return self.segmentor.segment(sentence);


test_sentence = "这是一个测试句子"


# 分词评估器
class Evaluator:
    def __init__(self, seg_tool, name=None):
        '''
        :param seg_tool: 分词器
        :param name: 分词器名称（可以直接重构到分词器里面去）
        '''
        if not name:
            name = seg_tool.__name__
        self.name = name
        time_start = time.time()
        print("%s 初始化开始" % self.name)
        self.seg = seg_tool()
        self.time = 0
        self.init = False
        result = self.seg.segment(test_sentence)
        # print(result)
        time_end = time.time()
        time_cost = (time_end - time_start) * 1000
        if "".join(result) != test_sentence:
            print("%s 初始化错误,句子:%s,分词结果:%s" % (self.name, test_sentence, result))
        else:
            self.init = True
            print("%s 初始化结束,耗时:%d ms" % (self.name, time_cost))


# 用BMES标记分词结果
def get_ner(terms):
    temp_gold = []
    for term in terms:
        if len(term) == 1:
            temp_gold.append("S-Null")
        else:
            term_item = ["M-Null"] * len(term)
            term_item[0] = "B-Null"
            term_item[-1] = "E-Null"
            temp_gold.extend(term_item)
    return temp_gold


# 评估各个分词器
def evaluate(input, output, max_line_count, include):
    # 分词文件目录
    if len(input) == 0:
        # 项目root目录
        root = os.getcwd()[:os.getcwd().index("python")]
        input = os.path.join(root, "data/seg.data_big")
    print("读入分词文件地址:" + input)
    write_result = False
    if len(output) > 0:
        print("分词结果写入地址:" + output)
        write_result = True
    max_line_count = int(max_line_count)
    if max_line_count > 0:
        print("最大读取行数:" + str(max_line_count))

    evaluators = []
    for name in include.split(","):
        evaluators.append(Evaluator(globals()[name + "_impl"], name))
    # evaluators.append(Evaluator(pynlpir_impl))
    # evaluators.append(Evaluator(pkuseg_impl))
    # evaluators.append(Evaluator(jieba_impl))
    # evaluators.append(Evaluator(snownlp_impl))
    # evaluators.append(Evaluator(thulac_impl))
    # evaluators.append(Evaluator(pyltp_impl))
    time_start = time.time()
    print("读入分词文件开始")
    with open(input, encoding="utf-8") as f:
        lines = f.readlines()
    time_end = time.time()
    time_cost = (time_end - time_start) * 1000
    print("读取文件结束,耗时:%d ms" % (time_cost))

    gold = []
    test = []
    char_count = 0
    # max_line_count = 100000
    if max_line_count <= 0:
        max_line_count = len(lines)
    line_count = 0
    for line in lines:
        gold.append(line.strip().split())
        test.append("".join(gold[-1]))
        char_count += len(test[-1])
        line_count += 1
        if line_count > max_line_count:
            break
    print("总行数:%d\t总字符数:%d" % (line_count, char_count))
    calcScore = True
    for item in evaluators:
        print()
        print("%s 评测开始" % item.name)
        if not item.init:
            print("%s 初始化错误,跳过" % (item.name))
            continue
        if write_result:
            file = open(os.path.join(output, item.name), mode="w", encoding="utf-8")
        time_start = time.time()
        right_num = 0
        gold_num = 0
        predict_num = 0

        for i in range(line_count):
            line = test[i]
            predict = item.seg.segment(line)

            if (calcScore):
                # temp_gold = get_ner(gold[i])
                # temp_predict = get_ner(predict)
                if len("".join(predict)) != len(line):
                    print(item.name + "\t" + line + "\t")
                    continue
                # accuracy, precision, recall, f_measure, right_num, golden_num, predict_num = get_ner_fmeasure([temp_gold], [temp_predict])
                right_num_local, golden_num_local, predict_num_local = calc_score(gold[i], predict)

                right_num += right_num_local
                gold_num += golden_num_local
                predict_num += predict_num_local

                # if (right_num != right_num_local or golden_num != golden_num_local or predict_num != predict_num_local):
                #     print("badcase:" % line)

                # print("gold_num = ", golden_num, " pred_num = ", predict_num, " right_num = ", right_num)
                # print()
            if write_result:
                file.write(" ".join(predict) + "\n")

        time_end = time.time()
        item.time = (time_end - time_start) * 1000
        precision = 0
        recall = 0
        f = 0
        if write_result:
            file.close()
        if predict_num != 0:
            precision = right_num * 1.0 / predict_num
        if gold_num != 0:
            recall = right_num * 1.0 / gold_num
        if precision + recall > 0:
            f = 2 * precision * recall / (precision + recall)

        print("precision:%f \t recall:%f \t f1:%f" % (precision, recall, f))
        if item.time == 0:
            print("耗时太少,速度无法评估")
        else:
            print("耗时:%d ms,\t速度:%f 字符/毫秒" % (item.time, char_count * 1.0 / item.time))


def update(offset, index, terms):
    offset += len(terms[index])
    index += 1
    return offset, index


def calc_score(gold, predict):
    gold_offset = 0
    predict_offset = 0

    gold_term_index = 0
    predict_term_index = 0

    right = 0
    total = len(gold)
    right_and_wrong = len(predict)
    while (gold_term_index < len(gold) or predict_term_index < len(predict)):
        if gold_offset == predict_offset:
            if gold[gold_term_index] == predict[predict_term_index]:
                right += 1
            gold_offset, gold_term_index = update(gold_offset, gold_term_index, gold)
            predict_offset, predict_term_index = update(predict_offset, predict_term_index, predict)
        elif gold_offset < predict_offset:
            gold_offset, gold_term_index = update(gold_offset, gold_term_index, gold)
        else:
            predict_offset, predict_term_index = update(predict_offset, predict_term_index, predict)
    return right, total, right_and_wrong


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='中文分词对比测试')
    parser.add_argument('-i', help='file to segment, default using the file in nlp-evaluation/data/seg.data_big',
                        default="")
    parser.add_argument('-o', help='path to save the result, default is not saving', default="")
    parser.add_argument("-n", help='maximum number of read rows, default reading all', default="-1")
    parser.add_argument("-c", help='segmentor to evaluate', default="pkuseg,jieba,thulac")
    args = parser.parse_args()
    evaluate(args.i, args.o, args.n, args.c)
