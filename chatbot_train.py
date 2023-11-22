# import math
# import numpy as np
# import pandas as pd
# import random
# import re
# import torch
# import urllib.request
# from torch.utils.data import DataLoader, Dataset
# from transformers import PreTrainedTokenizerFast 
# from transformers import BertTokenizer


# import urllib.request

# # urllib.request.urlretrieve(
# #     "https://raw.githubusercontent.com/songys/Chatbot_data/master/ChatbotData.csv",
# #     filename="ChatBotData.csv",
# # )

# Chatbot_Data = pd.read_csv("ChatBotData.csv") # head(정보) body(내용)
# #tabuler data table 형식으로 된 데이터 보고 타뷸러 엑셀
# #처리 하는 라이브러리가 pandas
# # Test 용으로 300개 데이터만 처리한다.
# Chatbot_Data = Chatbot_Data[:300]

# #print(Chatbot_Data.head())
# #print(Chatbot_Data['A']) #행을 뽑을떄에는 []로 뽑는다.
# #print(Chatbot_Data.iloc[0]) # 0번쨰 열이 출력됨, 
# #print(Chatbot_Data.loc[])
# #print(Chatbot_Data.describe()) # 정보를 확인할때, 
# #Chatbot_Data.drop
# #Chatbot_Data.dropna
# #+ 단어 만 들어가야해요
BOS = "</s>"# 시작을 알리는 기호
EOS = "</s>" # 끝 을 알리는 기호
PAD = "<pad>" # 스페이스 바
MASK = "<unused0>"# 마스크 

Q_TKN = "<usr>"
A_TKN = "<sys>"
SENT = '<unused1>'

# #길이를 일정하게 정해 주는 것이 positional encoding -> </s>나는<pad>바보다.</s><unused0><unused0><unused0> </s>나는<pad>공부를<pad>매우<pad>잘한다.</s>
# # 허깅페이스 transformers 에 등록된 사전 학습된 koGTP2 토크나이저를 가져온다.
# koGPT2_TOKENIZER = PreTrainedTokenizerFast.from_pretrained("skt/kogpt2-base-v2", bos_token=BOS, eos_token=EOS, unk_token="<unk>", pad_token=PAD, mask_token=MASK,)

# # 챗봇 데이터를 처리하는 클래스를 만든다.


# train_set = ChatbotDataset(Chatbot_Data, max_len=40)

# #윈도우 환경에서 num_workers 는 무조건 0으로 지정, 리눅스에서는 2
# train_dataloader = DataLoader(train_set, batch_size=32, num_workers=0, shuffle=True, collate_fn=collate_batch,)

# print("start")
# for batch_idx, samples in enumerate(train_dataloader):
#     token_ids, mask, label = samples
#     print("token_ids ====> ", token_ids)
#     print("mask =====> ", mask)
#     print("label =====> ", label)
# print("end")
# sequence = "A Titan RTX has 24GB of VRAM"
# tokenizer = BertTokenizer.from_pretrained("bert-base-cased")
# #tokenized_sequence = tokenizer.tokenize(sequence)
# tokenized_sequence = koGPT2_TOKENIZER.tokenize(sequence)
# print(tokenized_sequence)
# print(koGPT2_TOKENIZER.convert_tokens_to_ids(tokenized_sequence))

# turn = Chatbot_Data.iloc[0]["Q"]
# print(turn)
# q = re.sub(r"([?.!,])", r" ", turn)
# print(q)
# q_toked = koGPT2_TOKENIZER.tokenize(q)
# print(q_toked)
# ids=koGPT2_TOKENIZER.convert_tokens_to_ids(q_toked)
# print(ids)
import numpy as np
import pandas as pd
import torch
from torch.utils.data import DataLoader, Dataset
from transformers.optimization import AdamW, get_cosine_schedule_with_warmup
from transformers import PreTrainedTokenizerFast, GPT2LMHeadModel
import re

Q_TKN = "<usr>"
A_TKN = "<sys>"
BOS = '</s>'
EOS = '</s>'
MASK = '<unused0>'
SENT = '<unused1>'
PAD = '<pad>'

koGPT2_TOKENIZER = PreTrainedTokenizerFast.from_pretrained("skt/kogpt2-base-v2",
            bos_token=BOS, eos_token=EOS, unk_token='<unk>',
            pad_token=PAD, mask_token=MASK) 
model = GPT2LMHeadModel.from_pretrained('skt/kogpt2-base-v2')

import urllib.request

urllib.request.urlretrieve(
    "https://raw.githubusercontent.com/songys/Chatbot_data/master/ChatbotData.csv",
    filename="ChatBotData.csv",
)
Chatbot_Data = pd.read_csv("ChatBotData.csv")
# Test 용으로 300개 데이터만 처리한다.
Chatbot_Data.head()

class ChatbotDataset(Dataset):
    def __init__(self, chats, max_len=40):  # 데이터셋의 전처리를 해주는 부분
        self._data = chats
        self.max_len = max_len
        self.q_token = Q_TKN
        self.a_token = A_TKN
        self.sent_token = SENT
        self.eos = EOS
        self.mask = MASK
        self.tokenizer = koGPT2_TOKENIZER

    def __len__(self):  # chatbotdata 의 길이를 리턴한다.
        return len(self._data)

    def __getitem__(self, idx):  # 로드한 챗봇 데이터를 차례차례 DataLoader로 넘겨주는 메서드
        turn = self._data.iloc[idx]
        q = turn["Q"]  # 질문을 가져온다.
        q = re.sub(r"([?.!,])", r" ", q)  # 구둣점들을 제거한다.# re 라는 파이썬 기본 라이브러리 인데, 정규표현식? delete *.png

        a = turn["A"]  # 답변을 가져온다.
        a = re.sub(r"([?.!,])", r" ", a)  # 구둣점들을 제거한다.

        q_toked = self.tokenizer.tokenize(self.q_token + q + self.sent_token)
        q_len = len(q_toked)

        a_toked = self.tokenizer.tokenize(self.a_token + a + self.eos)
        a_len = len(a_toked)
        #outlier? 결측치 뭐했어? 밥먹어 인데, q에 해당하는 단어만을 학습하기 위해서 긴 문장은 잘라서 넣는다... 물론 아래 전처리 과정을 거치지 않아도 돼요 
        #질문의 길이가 최대길이보다 크면
        if q_len > self.max_len:
            a_len = self.max_len - q_len        #답변의 길이를 최대길이 - 질문길이
            if a_len <= 0:       #질문의 길이가 너무 길어 질문만으로 최대 길이를 초과 한다면
                q_toked = q_toked[-(int(self.max_len / 2)) :]   #질문길이를 최대길이의 반으로 
                q_len = len(q_toked)
                a_len = self.max_len - q_len              #답변의 길이를 최대길이 - 질문길이
            a_toked = a_toked[:a_len]
            a_len = len(a_toked)

        #질문의 길이 + 답변의 길이가 최대길이보다 크면
        if q_len + a_len > self.max_len:
            a_len = self.max_len - q_len        #답변의 길이를 최대길이 - 질문길이
            if a_len <= 0:       #질문의 길이가 너무 길어 질문만으로 최대 길이를 초과 한다면
                q_toked = q_toked[-(int(self.max_len / 2)) :]   #질문길이를 최대길이의 반으로 
                q_len = len(q_toked)
                a_len = self.max_len - q_len              #답변의 길이를 최대길이 - 질문길이
            a_toked = a_toked[:a_len]
            a_len = len(a_toked)
        # label data의 정답지가 label / => supervised learning  chatbot supervised learning
        # 답변 labels = [mask, mask, ...., mask, ..., <bos>,..답변.. <eos>, <pad>....]
        labels = [self.mask,] * q_len + a_toked[1:]

        # mask = 질문길이 0 + 답변길이 1 + 나머지 0
        mask = [0] * q_len + [1] * a_len + [0] * (self.max_len - q_len - a_len)
        # 답변 labels을 index 로 만든다.
        labels_ids = self.tokenizer.convert_tokens_to_ids(labels)
        # 최대길이만큼 PADDING # pad 길이를 맞춰준다.
        while len(labels_ids) < self.max_len:
            labels_ids += [self.tokenizer.pad_token_id]

        # 질문 + 답변을 index 로 만든다.    
        token_ids = self.tokenizer.convert_tokens_to_ids(q_toked + a_toked)
        # 최대길이만큼 PADDING
        while len(token_ids) < self.max_len:
            token_ids += [self.tokenizer.pad_token_id]

        #질문+답변, 마스크, 답변
        return (token_ids, np.array(mask), labels_ids)

def collate_batch(batch):
    data = [item[0] for item in batch]
    mask = [item[1] for item in batch]
    label = [item[2] for item in batch]
    return torch.LongTensor(data).to(device), torch.LongTensor(mask).to(device), torch.LongTensor(label).to(device)



device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
train_set = ChatbotDataset(Chatbot_Data, max_len=40)
#윈도우 환경에서 num_workers 는 무조건 0으로 지정, 리눅스에서는 2
train_dataloader = DataLoader(train_set, batch_size=32, num_workers=0, shuffle=True, collate_fn=collate_batch,)

model.to(device)
model.train()
learning_rate = 3e-5
criterion = torch.nn.CrossEntropyLoss(reduction="none")
optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

epoch = 10
Sneg = -1e18


print ("start")
for epoch in range(epoch):
    for batch_idx, samples in enumerate(train_dataloader):

        optimizer.zero_grad()
        token_ids, mask, label = samples
        out = model(token_ids)
        out = out.logits      #Returns a new tensor with the logit of the elements of input
        mask_3d = mask.unsqueeze(dim=2).repeat_interleave(repeats=out.shape[2], dim=2)
        mask_out = torch.where(mask_3d == 1, out, Sneg * torch.ones_like(out))
        loss = criterion(mask_out.transpose(2, 1), label)
        # 평균 loss 만들기 avg_loss[0] / avg_loss[1] <- loss 정규화
        avg_loss = loss.sum() / mask.sum()
        avg_loss.backward()
        # 학습 끝
        optimizer.step()
print ("end")

torch.save(model, 'model.pt')
