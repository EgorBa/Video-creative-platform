from pymatting import cutout
import datetime

count = 1
size = 11
m = {}
for i in range(1, size + 1):
    m[str(i)] = []

for j in range(count):
    print(j)
    for i in range(1, size + 1):
        filename = str(i)
        try:
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
        except Exception:
            print("Can't cutout : " + filename)

for k in m.keys():
    avg = 0
    for v in m[k]:
        avg = avg + v.seconds + v.microseconds * 10e-7
    print(avg / count)
