import sys

from PIL import Image, ImageDraw

for i in range(1, 6):
    filename = str(i)
    image = Image.open("examples/" + filename + ".jpg")  # Открываем изображение.
    draw = ImageDraw.Draw(image)  # Создаем инструмент для рисования.
    width = image.size[0]  # Определяем ширину.
    height = image.size[1]  # Определяем высоту.
    pix = image.load()  # Выгружаем значения пикселей.

    print('Size: %s x %s' % (height, width))

    for i in range(width):
        for j in range(height):
            a = pix[i, j][0]
            b = pix[i, j][1]
            c = pix[i, j][2]
            S = (a + b + c) // 3
            if (a + b + c) >= 760:
                draw.point((i, j), (0, 0, 0))
            elif (a + b + c) >= 745:
                draw.point((i, j), (128, 128, 128))
            else:
                draw.point((i, j), (256, 256, 256))

    image.save("trimaps/trimap_" + filename + ".png", "PNG")
    # image.show()
