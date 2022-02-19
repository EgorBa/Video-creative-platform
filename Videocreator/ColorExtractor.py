from PIL import Image

for i in range(8):
    imageFile = Image.open("logos/" + str(i) + ".png")
    imageFile = imageFile.convert('RGBA')
    (W, H) = imageFile.size
    if len(imageFile.getpixel((0, 0))) == 4:
        pixel_sum = []
        count_non_zero = 0
        for j in imageFile.getpixel((0, 0)):
            pixel_sum.append(j)
            count_non_zero += 1
        for w in range(W):
            for h in range(H):
                has_all_zero = True
                for j in range(len(pixel_sum)):
                    has_all_zero = has_all_zero and (imageFile.getpixel((w, h))[j] == 0)
                if not has_all_zero:
                    count_non_zero += 1
                    for j in range(len(pixel_sum)):
                        pixel_sum[j] += imageFile.getpixel((w, h))[j]
        pixel_avg = []
        for j in pixel_sum:
            pixel_avg.append(int(j / count_non_zero))
        background = Image.new('RGBA', (64, 64), (pixel_avg[0], pixel_avg[1], pixel_avg[2], pixel_avg[3]))
        background.save("logos/colors/" + str(i) + ".png")
