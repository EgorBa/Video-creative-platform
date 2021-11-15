from pymatting import cutout

for i in range(1, 6):
    filename = str(i)
    print("processing : " + filename + ".jpg")
    cutout(
        # input image path
        "examples/" + filename + ".jpg",
        # input trimap path
        "trimaps/trimap_" + filename + ".png",
        # output cutout path
        "cutouts/cutout_" + filename + ".png")
