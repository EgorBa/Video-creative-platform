# pip install transformers sentencepiece
import torch
from transformers import AutoTokenizer, AutoModel
import numpy as np
import pandas as pd

# tokenizer = AutoTokenizer.from_pretrained("cointegrated/rubert-tiny")
# model = AutoModel.from_pretrained("cointegrated/rubert-tiny")
#
#
# # model.cuda()  # uncomment it if you have a GPU
#
# def embed_bert_cls(text, model, tokenizer):
#     t = tokenizer(text, padding=True, truncation=True, return_tensors='pt')
#     with torch.no_grad():
#         model_output = model(**{k: v.to(model.device) for k, v in t.items()})
#     embeddings = model_output.last_hidden_state[:, 0, :]
#     embeddings = torch.nn.functional.normalize(embeddings)
#     return embeddings[0].cpu().numpy()
#
#
# x = embed_bert_cls('привет мир', model, tokenizer)
# y = embed_bert_cls('пока мир', model, tokenizer)
# z = embed_bert_cls('белый голубь мира', model, tokenizer)
# w = embed_bert_cls('картофель', model, tokenizer)
# print(np.linalg.norm(x - y))
# print(np.linalg.norm(x - z))
# print(np.linalg.norm(x - w))

xl = pd.ExcelFile("faces_labels.xlsx").parse("Лист1").to_numpy()
print(xl[0][3])
