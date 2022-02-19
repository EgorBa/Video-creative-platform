import cv2
from moviepy.editor import *
from PIL import ImageFile, Image, ImageDraw, ImageFont
from random import randrange
import threading

ImageFile.LOAD_TRUNCATED_IMAGES = True
W = 720
H = 1280

MAX_HEIGHT_IM = 960
MAX_WIDTH_IM = 680

MAX_HEIGHT_TEXT = 310
MAX_WIDTH_TEXT = 680

frameSize = (W, H)


def text_size(text, font):
    width = font.getmask(text).getbbox()[2]
    height = font.getmask(text).getbbox()[3]
    return (width, height)


def normalize_size(w, h):
    max_h = MAX_HEIGHT_IM
    max_w = MAX_WIDTH_IM
    r = w / h
    if w > max_w:
        w = max_w
        h = w / r
    if h > max_h:
        h = max_h
        w = h * r
    return (w, h)


def find_font_size(font_path, text):
    was_OK = False
    for i in range(0, 1000):
        font = ImageFont.truetype(font_path, size=i)
        (w_text, h_text) = text_size(text, font)
        if was_OK and (w_text > MAX_WIDTH_TEXT or h_text > MAX_HEIGHT_TEXT):
            return i - 1
        was_OK = w_text <= MAX_WIDTH_TEXT and h_text <= MAX_HEIGHT_TEXT
    return 0


def clean_res(paths):
    for path in paths:
        th = threading.Thread(target=os.remove, args=(path,))
        th.start()


def generate_video(video_title, paths, texts):
    pictures = []
    videos = []
    frame_size = 30
    video_len = 4

    len_paths = len(paths)
    len_texts = len(texts)

    if len_paths > len_texts:
        print("paths should <= tests")
        exit(0)

    for i in range(len_paths):
        for j in range(1, frame_size + 1):
            path = paths[i]

            image = Image.open(path)
            (w, h) = image.size
            (w, h) = normalize_size(w, h)

            w = int(w)
            h = int(h)
            image = image.resize((w, h))

            x = int((W - MAX_WIDTH_IM) / 2 + (MAX_WIDTH_IM - w) / 2)
            y = int((H - MAX_HEIGHT_IM) + (MAX_HEIGHT_IM - h) / 2)

            text_img = Image.new('RGBA', (W, H), (255, 0, 0, 255))
            text_img.paste(image, (x, y), mask=image)

            draw = ImageDraw.Draw(text_img)
            text = texts[i]
            f_size = find_font_size("21158.ttf", text)
            size_t = min(int(f_size * ((j * (2 * frame_size - j)) / frame_size ** 2)), f_size)
            font = ImageFont.truetype("21158.ttf", size=size_t)

            (w_text, h_text) = text_size(text, font)

            x1 = int((W - MAX_WIDTH_IM) / 2 + (MAX_WIDTH_IM - w_text) / 2)
            y1 = int((H - MAX_HEIGHT_IM - h_text) / 2)

            draw.text((x1, y1), text, font=font, fill=('#1C0606'))

            out_path = "prepared/" + str(randrange(1000000)) + ".png"
            pictures.append(out_path)
            text_img.save(out_path, format="png")

    for i in range(len_paths, len_texts):
        for j in range(0, frame_size):
            text_img = Image.new('RGBA', (W, H), (255, 0, 0, 255))
            draw = ImageDraw.Draw(text_img)
            text = texts[i]
            size_t = find_font_size("21158.ttf", text)
            font = ImageFont.truetype("21158.ttf", size=size_t)

            (w_text, h_text) = text_size(text, font)

            x1 = int((W - MAX_WIDTH_IM) / 2 + (MAX_WIDTH_IM - w_text) / 2)
            y1 = int((H - h_text) / 2)

            draw.text((x1, y1), text, font=font, fill=('#1C0606'))

            out_path = "prepared/" + str(randrange(1000000)) + ".png"
            pictures.append(out_path)
            text_img.save(out_path, format="png")

    for i in range(len_texts):
        out_path = 'videos/output_video_' + str(randrange(1000000)) + '.avi'
        videos.append(out_path)
        out = cv2.VideoWriter(out_path, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'), frame_size, frameSize)
        for j in range(frame_size * video_len):
            index = frame_size * i + min(j, frame_size - 1)
            path = pictures[index]
            img = cv2.imread(path)
            out.write(img)
        out.release()

    # for path in pictures:
    #     out_path = 'videos/output_video_' + str(randrange(1000000)) + '.avi'
    #     videos.append(out_path)
    #     out = cv2.VideoWriter(out_path, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'), frame_size, frameSize)
    #     img = cv2.imread(path)
    #     # for j in range(video_len):
    #     out.write(img)
    #     out.release()

    length = len(videos)
    clips = []
    for i in range(length):
        clip = VideoFileClip(videos[i])
        if i == 0:
            clips.append(clip.set_start(i * video_len).crossfadeout(1))
        else:
            if i == length - 1:
                clips.append(clip.set_start(i * video_len).crossfadein(1))
            else:
                clips.append(clip.set_start(i * video_len).crossfadeout(1).crossfadein(1))

    video = CompositeVideoClip(clips)
    video.write_videofile(video_title + ".mp4", fps=25)
    clean_res(pictures)
    clean_res(videos)

paths = ["cutouts/cutout_1.png",
         "cutouts/cutout_2.png",
         "cutouts/cutout_3.png"]
texts = ["Крутой пылесос",
         "Классная сковорода",
         "Удобные кроссовки",
         "Сайт.ру"]
generate_video("third_variant", paths, texts)
