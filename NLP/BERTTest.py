# pip install transformers sentencepiece
import torch
from PIL import ImageFile, Image, ImageDraw, ImageFont
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

# xl = pd.ExcelFile("faces_labels.xlsx").parse("Лист1").to_numpy()
# print(xl[0][3])
from NLP.Emotions import get_emotion_path


def text_size(text, font):
    width = font.getmask(text).getbbox()[2]
    height = font.getmask(text).getbbox()[3]
    return (width, height)


def find_image_size(w, h, max_w, max_h):
    r = w / h
    if w > max_w:
        w = max_w
        h = w / r
    if h > max_h:
        h = max_h
        w = h * r
    return (w, h)


def find_max_font_size(font_path, text, max_w, max_h):
    was_OK = False
    for i in range(0, 1000):
        font = ImageFont.truetype(font_path, size=i)
        (w_text, h_text) = text_size(text, font)
        if was_OK and (w_text > max_w or h_text > max_h):
            return i - 1
        was_OK = w_text <= max_w and h_text <= max_h
    return 1000


(rgb_colors, new_rgb_colors) = ((255, 255, 255), (0, 0, 0))
W = 720
H = 1280
frameSize = (W, H)

x1 = 30
y1 = 0
x2 = W - 30
y2 = int(H / 3)
x3 = 0
y3 = int(H / 3)
x4 = W
y4 = H

texts = ["Крутые кроссовки", "Классная сковорода", "Хорошие врачи",
         "Высокое качество", "Счастливые дети", "Молодые учителя", "Большие скидки"
         ]
count = 1
for text in texts:
    background = Image.new('RGB', frameSize, rgb_colors)
    path_to_image = get_emotion_path(text)
    out_image_path = "examples/" + str(count) + ".png"
    image = Image.open(path_to_image)
    (w, h) = image.size
    (w, h) = find_image_size(w, h, x4 - x3, y4 - y3)
    image = image.resize((int(w), int(h)))
    background.paste(image, (int((x3 + x4 - w) / 2), int((y3 + y4 - h) / 2)), mask=image)
    draw = ImageDraw.Draw(background)
    size_t = find_max_font_size("21158.ttf", text, x2 - x1, y2 - y1)
    font = ImageFont.truetype("21158.ttf", size=size_t)
    (w, h) = text_size(text, font)
    draw.text((int((x1 + x2 - w) / 2), int((y1 + y2 - h) / 2)), text, font=font, fill=new_rgb_colors)
    background.save(out_image_path, format="png")
    count += 1
