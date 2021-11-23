from PIL import Image, ImageDraw

for i in range(1, 6):
    filename = str(i)
    image = Image.open("cutouts/cutout_" + filename + ".png")
    draw = ImageDraw.Draw(image)
    width = image.size[0]
    height = image.size[1]
    text_img = Image.new('RGBA', (width, height), (255, 0, 0, 255))
    text_img.paste(image, (0, 0), mask=image)
    text_img.save("cutouts/cutout_" + filename + ".png", format="png")
