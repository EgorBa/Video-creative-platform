from sklearn.cluster import KMeans
from PIL import Image, ImageDraw, ImageFont
from functools import cmp_to_key
import colorsys
import numpy as np


def compare(item1, item2):
    v1 = item1[0][1] + item1[0][2]
    v2 = item2[0][1] + item2[0][2]
    if v1 < v2:
        return -1
    elif v1 > v2:
        return 1
    else:
        return 0


def get_colors_by_logo(path):
    imageFile = Image.open(path)
    imageFile = imageFile.convert('RGBA')
    a = []
    (W, H) = imageFile.size
    for w in range(W):
        for h in range(H):
            has_all_zero = True
            for j in range(4):
                has_all_zero = has_all_zero and (imageFile.getpixel((w, h))[j] == 0)
            if not has_all_zero:
                a.append(imageFile.getpixel((w, h)))
    X = np.array(a)
    kmeans = KMeans(n_clusters=3, random_state=0).fit(X)
    colors = []
    for j in range(3):
        rgb_colors = list(map(int, kmeans.cluster_centers_[j][:3]))
        hsv_colors = colorsys.rgb_to_hsv(rgb_colors[0] / 255, rgb_colors[1] / 255, rgb_colors[2] / 255)
        hsv_colors = [hsv_colors[0] * 255, hsv_colors[1] * 255, hsv_colors[2] * 255]
        colors.append((hsv_colors, rgb_colors))
    colors = sorted(colors, key=cmp_to_key(compare))
    hsv_colors = colors[-1][0]
    new_hsv_colors = [(hsv_colors[0] + 180) % 360, hsv_colors[1], hsv_colors[2]]
    new_rgb_colors = colorsys.hsv_to_rgb(new_hsv_colors[0] / 360, new_hsv_colors[1] / 360, new_hsv_colors[2] / 360)
    new_rgb_colors = tuple(map(lambda x: int(x * 255), new_rgb_colors))
    rgb_colors = tuple(colors[-1][1])
    return (rgb_colors, new_rgb_colors)


def test():
    for i in range(8):
        (rgb_colors, new_rgb_colors) = get_colors_by_logo("logos/" + str(i) + ".png")
        background = Image.new('RGB', (256, 256), new_rgb_colors)
        path = "logos/colors_with_text/" + str(i) + ".png"
        draw = ImageDraw.Draw(background)
        font = ImageFont.truetype("21158.ttf", size=160)
        draw.text((0, 0), 'TEXT', font=font, fill=rgb_colors)
        background.save(path)
