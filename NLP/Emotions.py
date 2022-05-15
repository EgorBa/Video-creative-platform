import os

import torch
from transformers import AutoTokenizer, AutoModel
import numpy as np
import pandas as pd

tokenizer = AutoTokenizer.from_pretrained("cointegrated/rubert-tiny")
model = AutoModel.from_pretrained("cointegrated/rubert-tiny")


def embed_bert_cls(text, model, tokenizer):
    t = tokenizer(text, padding=True, truncation=True, return_tensors='pt')
    with torch.no_grad():
        model_output = model(**{k: v.to(model.device) for k, v in t.items()})
    embeddings = model_output.last_hidden_state[:, 0, :]
    embeddings = torch.nn.functional.normalize(embeddings)
    return embeddings[0].cpu().numpy()


def get_emotion(text):
    xl = pd.ExcelFile("faces_labels.xlsx").parse("Лист1").to_numpy()
    x = embed_bert_cls(text, model, tokenizer)
    min_norm = (1, 100000)
    for idx, s in enumerate(xl):
        norm = 0
        for index in range(1, len(s)):
            y = embed_bert_cls(str(s[index]), model, tokenizer)
            n = np.linalg.norm(x - y)
            norm += n
        if min_norm[1] > norm:
            min_norm = (idx + 1, norm)
    return min_norm


def get_emotion_path(text):
    (index, _) = get_emotion(text)
    return "faces/" + str(index) + ".png"

