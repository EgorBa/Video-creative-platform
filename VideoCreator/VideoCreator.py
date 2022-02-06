import cv2
from moviepy.editor import *
from PIL import ImageFile, Image, ImageDraw, ImageFont
from random import randrange
import threading

ImageFile.LOAD_TRUNCATED_IMAGES = True
W = 720
H = 1280
frameSize = (W, H)


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
    return 0


def clean_res(paths):
    for path in paths:
        th = threading.Thread(target=os.remove, args=(path,))
        th.start()


# This picture shows how the elements will be placed depending on the coordinates.
# Your text will have the maximum possible font.
# Your image will have the maximum possible size.
# (0,0)------------------------------------> W
# |     (x1,y1)
# |        ------------------------
# |        |                      |
# |        |      YOUR TEXT       |
# |        |                      |
# |        ------------------------
# |                            (x2,y2)
# |
# | (x3,y3)
# |    --------------------------------
# |    |                              |
# |    |                              |
# |    |        YOUR IMAGE            |
# |    |                              |
# |    |                              |
# |    --------------------------------
# |                                (x4,y4)
# v
# H


# Generate one video with text and image.
# Returns path to this video.
# animation_type can be "simple", "move" and "scale"
def generate_one_video(video_length, x1=0, y1=0, x2=0, y2=0, x3=0, y3=0, x4=0, y4=0, text="", path_to_image="",
                       animation_type="simple"):
    if (path_to_image == "" and text == "") \
            or (text != "" and (x1 == x2 or y1 == y2)) \
            or (path_to_image != "" and (x3 == x4 or y3 == y4)) \
            or video_length <= 0:
        return ""

    if animation_type == "simple":
        background = Image.new('RGBA', frameSize, (255, 0, 0, 255))
        if path_to_image != "":
            image = Image.open(path_to_image)
            (w, h) = image.size
            (w, h) = find_image_size(w, h, x4 - x3, y4 - y3)
            image = image.resize((int(w), int(h)))
            background.paste(image, (int((x3 + x4 - w) / 2), int((y3 + y4 - h) / 2)), mask=image)
        if text != "":
            draw = ImageDraw.Draw(background)
            size_t = find_max_font_size("21158.ttf", text, x2 - x1, y2 - y1)
            font = ImageFont.truetype("21158.ttf", size=size_t)
            (w, h) = text_size(text, font)
            draw.text((int((x1 + x2 - w) / 2), int((y1 + y2 - h) / 2)), text, font=font, fill=('#1C0606'))
        out_image_path = "prepared/" + str(randrange(1000000)) + ".png"
        background.save(out_image_path, format="png")
        out_video_path = 'videos/output_video_' + str(randrange(1000000)) + '.avi'
        out = cv2.VideoWriter(out_video_path, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'), 1 / video_length, frameSize)
        img = cv2.imread(out_image_path)
        out.write(img)
        out.release()
        clean_res([out_image_path])
    if animation_type == "move" and text != "":
        background = Image.new('RGBA', frameSize, (255, 0, 0, 255))
        if path_to_image != "":
            image = Image.open(path_to_image)
            (w, h) = image.size
            (w, h) = find_image_size(w, h, x4 - x3, y4 - y3)
            image = image.resize((int(w), int(h)))
            background.paste(image, (int((x3 + x4 - w) / 2), int((y3 + y4 - h) / 2)), mask=image)
        out_image_path = "prepared/" + str(randrange(1000000)) + ".png"
        background.save(out_image_path, format="png")
        out_image_video_path = 'videos/output_video_' + str(randrange(1000000)) + '.avi'
        out = cv2.VideoWriter(out_image_video_path, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'), 1 / video_length, frameSize)
        img = cv2.imread(out_image_path)
        out.write(img)
        out.release()

        size_t = find_max_font_size("21158.ttf", text, x2 - x1, y2 - y1) - 5
        my_text = TextClip(text, font="21158.ttf", color='white', fontsize=size_t)
        txt_col = my_text.on_color(size=(W + my_text.w, my_text.h + 5), color=(0, 0, 0), pos=(6, 'center'),
                                   col_opacity=0.6)
        font = ImageFont.truetype("21158.ttf", size=size_t)
        (w, h) = text_size(text, font)
        txt_mov = txt_col.set_pos(lambda t: (max(x1, int(x2 * (1 - t))), int((y1 + y2 - h) / 2)))
        txt_mov = txt_mov.set_start(0).set_end(video_length)

        out_video_path = 'videos/output_video_' + str(randrange(1000000)) + '.mp4'
        video = CompositeVideoClip([VideoFileClip(out_image_video_path), txt_mov])
        video.write_videofile(out_video_path, fps=25)
        clean_res([out_image_path, out_image_video_path])
    if animation_type == "scale" and text != "" and video_length > 1:
        count_frames = 60
        pictures = []
        if path_to_image != "":
            image = Image.open(path_to_image)
            (image_w, image_h) = image.size
            (image_w, image_h) = find_image_size(image_w, image_h, x4 - x3, y4 - y3)
            image = image.resize((int(image_w), int(image_h)))
        for j in range(1, count_frames + 1):
            background = Image.new('RGBA', frameSize, (255, 0, 0, 255))
            if path_to_image != "":
                background.paste(image, (int((x3 + x4 - image_w) / 2), int((y3 + y4 - image_h) / 2)), mask=image)
            draw = ImageDraw.Draw(background)
            f_size = find_max_font_size("21158.ttf", text, x2 - x1, y2 - y1)
            size_t = min(int(f_size * ((j * (2 * count_frames - j)) / count_frames ** 2)), f_size)
            font = ImageFont.truetype("21158.ttf", size=size_t)
            (w, h) = text_size(text, font)
            draw.text((int((x1 + x2 - w) / 2), int((y1 + y2 - h) / 2)), text, font=font, fill=('#1C0606'))
            out_path = "prepared/" + str(randrange(1000000)) + ".png"
            pictures.append(out_path)
            background.save(out_path, format="png")
        out_video_path = 'videos/output_video_' + str(randrange(1000000)) + '.avi'
        out = cv2.VideoWriter(out_video_path, cv2.VideoWriter_fourcc('M', 'J', 'P', 'G'), count_frames, frameSize)
        for p in pictures:
            img = cv2.imread(p)
            out.write(img)
        img = cv2.imread(pictures[-1])
        for i in range(int((video_length - 1) * count_frames)):
            out.write(img)
        out.release()
        clean_res(pictures)


# paths = ["cutouts/cutout_1.png",
#          "cutouts/cutout_2.png",
#          "cutouts/cutout_3.png"]
# texts = ["Крутой пылесос",
#          "Классная сковорода",
#          "Удобные кроссовки",
#          "Сайт.ру"]
# generate_video("first_variant", paths, texts)

generate_one_video(4, x1=0, y1=0, x2=W, y2=int(H / 3), x3=0, y3=int(H / 3), x4=W, y4=H, text="Крутой пылесос",
                   path_to_image="cutouts/cutout_1.png", animation_type="scale")
