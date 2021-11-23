from pymatting import cutout
import datetime

count = 1
m = {'1': [], '2': [], '3': [], '4': [], '5': []}

for j in range(count):
    print(j)
    for i in range(1, 6):
        filename = str(i)
        # print("processing : " + filename + ".jpg")
        before = datetime.datetime.now()
        cutout(
            # input image path
            "examples/" + filename + ".jpg",
            # input trimap path
            "trimaps/trimap_" + filename + ".png",
            # output cutout path
            "cutouts/cutout_" + filename + ".png")
        after = datetime.datetime.now()
        m[filename].append(after - before)
        # print(str((after - before).seconds) + " s. " + str((after - before).microseconds))

for k in m.keys():
    avg = 0
    for v in m[k]:
        avg = avg + v.seconds + v.microseconds * 10e-7
    print(avg / count)
