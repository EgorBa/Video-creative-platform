from rembg.bg import remove
import numpy as np
import io
from PIL import ImageFile, Image
import datetime
import os

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
ImageFile.LOAD_TRUNCATED_IMAGES = True

count = 5
size = 11
m = {}
for i in range(1, size + 1):
    m[str(i)] = []

for j in range(count):
    print(j)
    for i in range(1, size + 1):
        filename = str(i)
        before = datetime.datetime.now()
        f = np.fromfile("examples/" + filename + ".jpg")
        result = remove(f)
        img = Image.open(io.BytesIO(result)).convert("RGBA")
        img.save("cutouts/cutout_" + filename + ".png")
        after = datetime.datetime.now()
        m[filename].append(after - before)

for k in m.keys():
    avg = 0
    for v in m[k]:
        avg = avg + v.seconds + v.microseconds * 10e-7
    print(avg / count)
