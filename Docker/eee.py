import io
from PIL import Image

imageFileObj = open("examples/1.jpg", "rb")
imageBinaryBytes = imageFileObj.read()
imageStream = io.BytesIO(imageBinaryBytes)
print(imageStream.getvalue())
imageFile = Image.open(imageStream)
print("imageFile.size= " + str(imageFile.size))
