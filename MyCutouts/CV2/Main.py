import cv2
import numpy as np
import datetime
import os

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"


def show(img, gray, mask, result):
    cv2.imshow("INPUT", img)
    cv2.imshow("GRAY", gray)
    cv2.imshow("MASK", mask)
    cv2.imshow("RESULT", result)
    cv2.waitKey(0)
    cv2.destroyAllWindows()


count = 100
size = 11
m = {}
for i in range(1, size + 1):
    m[str(i)] = []

for j in range(count):
    print(j)
    for i in range(1, size + 1):
        filename = str(i)
        before = datetime.datetime.now()
        # load image
        img = cv2.imread("examples/" + filename + ".jpg")

        # convert to gray
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        # threshold input image as mask
        mask = cv2.threshold(gray, 250, 255, cv2.THRESH_BINARY)[1]

        # negate mask
        mask = 255 - mask

        # apply morphology to remove isolated extraneous noise
        # use borderconstant of black since foreground touches the edges
        kernel = np.ones((3, 3), np.uint8)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)

        # anti-alias the mask -- blur then stretch
        # blur alpha channel
        mask = cv2.GaussianBlur(mask, (0, 0), sigmaX=2, sigmaY=2, borderType=cv2.BORDER_DEFAULT)

        # linear stretch so that 127.5 goes to 0, but 255 stays 255
        mask = (2 * (mask.astype(np.float32)) - 255.0).clip(0, 255).astype(np.uint8)

        # put mask into alpha channel
        result = img.copy()
        result = cv2.cvtColor(result, cv2.COLOR_BGR2BGRA)
        result[:, :, 3] = mask

        # save resulting masked image
        cv2.imwrite("cutouts/cutout_" + filename + ".png", result)

        after = datetime.datetime.now()
        m[filename].append(after - before)

        # display result, though it won't show transparency
        # show(img, gray, mask, result)

for k in m.keys():
    avg = 0
    for v in m[k]:
        avg = avg + v.seconds + v.microseconds * 10e-7
    print(avg / count)